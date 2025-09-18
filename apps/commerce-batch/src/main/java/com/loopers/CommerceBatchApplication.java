package com.loopers;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
@EnableRedisRepositories
public class CommerceBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommerceBatchApplication.class, args);
    }
}
