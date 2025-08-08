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
      @Value("${spring.datasource.hikari.maximum-pool-size}") int maxPoolSize,
      @Value("${process.strategy:default}") String strategy) {
    if ("queue".equals(strategy)) {
      return Executors.newFixedThreadPool(maxPoolSize - 2);
    }
    return Executors.newFixedThreadPool(maxPoolSize - 1);
  }

  @Bean(name = "dbExecutorReader")
  public Executor dbExecutorReader(
      @Value("${spring.datasource.hikari.maximum-pool-size}") int maxPoolSize) {
    return Executors.newSingleThreadExecutor();
  }
}