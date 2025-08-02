package com.backend.figth.client;

import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentProcessorResponseDTO;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-processor")
public interface PaymentProcessorClient {

  @Retry(name = "payment-processor")
  @PostMapping("/payments")
  PaymentProcessorResponseDTO processPayment(@RequestBody PaymentProcessorRequestDTO request);
}