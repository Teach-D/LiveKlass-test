package com.LiveKlass.LiveKlass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LiveKlassApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveKlassApplication.class, args);
	}

}
