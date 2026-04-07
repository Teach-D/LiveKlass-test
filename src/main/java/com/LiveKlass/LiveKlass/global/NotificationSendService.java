package com.LiveKlass.LiveKlass.global;

import com.LiveKlass.LiveKlass.entity.Notification;
import com.LiveKlass.LiveKlass.enums.NotificationStatus;
import com.LiveKlass.LiveKlass.enums.SendTimeSlot;
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

        List<CompletableFuture<Void>> futures = pendingIds.stream()
                .map(notificationItemService::sendSingle)
                .toList();

        // 100ms 간격으로 CPU/메모리를 샘플링하며 완료 대기
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        while (!allFutures.isDone()) {
            sample(osBean, memoryBean, cpuSamples, memorySamples);
            try {
                allFutures.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ignored) {
                // 아직 진행 중, 다음 샘플링 계속
            } catch (Exception e) {
                log.error("[{}] 일부 알림 발송 실패: {}", slot, e.getMessage());
                break;
            }
        }
        // 완료 직후 최종 샘플
        sample(osBean, memoryBean, cpuSamples, memorySamples);

        long elapsedMs = System.currentTimeMillis() - startTime;
        double maxCpu = cpuSamples.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double avgCpu = cpuSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        long avgMemoryMb = (long) memorySamples.stream().mapToLong(Long::longValue).average().orElse(0) / (1024 * 1024);

        log.info("[{}] 알림 발송 완료 | 건수={} | 처리시간={}ms | CPU 최대={}% 평균={}% | 힙 메모리 평균={}MB",
                slot, pendingIds.size(), elapsedMs,
                String.format("%.2f", maxCpu), String.format("%.2f", avgCpu), avgMemoryMb);
    }

    public void sendNotification(List<Long> pendingIds) {
        List<CompletableFuture<Void>> futures = pendingIds.stream()
                .map(notificationItemService::sendSingle)
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("일부 알림 발송 실패: {}", e.getMessage());
        }
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
