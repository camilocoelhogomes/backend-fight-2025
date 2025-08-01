package com.backend.figth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Value("${payment.processor.url}")
  private String paymentProcessorUrl;

  @Bean
  public FeignClientProperties.FeignClientConfiguration paymentProcessorFeignConfig() {
    FeignClientProperties.FeignClientConfiguration config = new FeignClientProperties.FeignClientConfiguration();
    config.setUrl(paymentProcessorUrl);
    return config;
  }
}