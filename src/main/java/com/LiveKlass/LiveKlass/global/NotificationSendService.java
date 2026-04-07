package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
import com.LiveKlass.LiveKlass.global.NotificationItemService.SendResult;
import com.LiveKlass.LiveKlass.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSendService {

    private final NotificationRepository notificationRepository;
    private final NotificationItemService notificationItemService;

    public void processScheduledNotifications(SendTimeSlot slot) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long startTime = System.currentTimeMillis();

        List<Long> pendingIds = notificationRepository
                .findAllBySendTimeSlotAndStatus(slot, NotificationStatus.PENDING)
                .stream()
                .map(Notification::getId)
                .toList();

        List<Double> cpuSamples = new ArrayList<>();
        List<Long> memorySamples = new ArrayList<>();

        List<CompletableFuture<SendResult>> futures = pendingIds.stream()
                .map(notificationItemService::sendSingle)
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        while (!allFutures.isDone()) {
            sample(osBean, memoryBean, cpuSamples, memorySamples);
            try {
                allFutures.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ignored) {
            } catch (Exception e) {
                log.error("[{}] 알림 발송 중 오류: {}", slot, e.getMessage());
                break;
            }
        }
        sample(osBean, memoryBean, cpuSamples, memorySamples);

        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        for (CompletableFuture<SendResult> future : futures) {
            SendResult result = future.getNow(new SendResult(0L, false));
            if (result.success()) successIds.add(result.id());
            else failedIds.add(result.id());
        }

        if (!successIds.isEmpty()) notificationRepository.updateStatusByIds(successIds, NotificationStatus.SUCCESS);
        if (!failedIds.isEmpty()) notificationRepository.updateStatusByIds(failedIds, NotificationStatus.FAILED);

        long elapsedMs = System.currentTimeMillis() - startTime;
        double maxCpu = cpuSamples.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double avgCpu = cpuSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        long avgMemoryMb = (long) memorySamples.stream().mapToLong(Long::longValue).average().orElse(0) / (1024 * 1024);

        log.info("[{}] 알림 발송 완료 | 건수={} | 성공={} | 실패={} | 처리시간={}ms | CPU 최대={}% 평균={}% | 힙 메모리 평균={}MB",
                slot, pendingIds.size(), successIds.size(), failedIds.size(), elapsedMs,
                String.format("%.2f", maxCpu), String.format("%.2f", avgCpu), avgMemoryMb);
    }

    private void sample(OperatingSystemMXBean osBean, MemoryMXBean memoryBean,
                        List<Double> cpuSamples, List<Long> memorySamples) {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOs) {
            double cpu = sunOs.getCpuLoad() * 100.0;
            if (cpu >= 0) cpuSamples.add(cpu);
        }
        memorySamples.add(memoryBean.getHeapMemoryUsage().getUsed());
    }
}
