package com.backend.figth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean(name = "dbExecutor")
  public Executor dbExecutor(
      @Value("${spring.datasource.hikari.maximum-pool-size}") int maxPoolSize) {
    return Executors.newFixedThreadPool(maxPoolSize);
  }
}