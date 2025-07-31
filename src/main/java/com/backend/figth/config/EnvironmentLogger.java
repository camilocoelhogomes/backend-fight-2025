package com.backend.figth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EnvironmentLogger implements CommandLineRunner {

  @Value("${PAYMENT_PROCESSOR_HOST:localhost}")
  private String paymentProcessorHost;

  @Value("${PAYMENT_PROCESSOR_PORT:8001}")
  private String paymentProcessorPort;

  @Value("${payment.processor.url}")
  private String paymentProcessorUrl;

  @Value("${DB_HOST:localhost}")
  private String dbHost;

  @Value("${DB_PORT:5432}")
  private String dbPort;

  @Override
  public void run(String... args) throws Exception {
    log.info("=== Environment Variables Check ===");
    log.info("PAYMENT_PROCESSOR_HOST: {}", paymentProcessorHost);
    log.info("PAYMENT_PROCESSOR_PORT: {}", paymentProcessorPort);
    log.info("payment.processor.url: {}", paymentProcessorUrl);
    log.info("DB_HOST: {}", dbHost);
    log.info("DB_PORT: {}", dbPort);
    log.info("==================================");
  }
}