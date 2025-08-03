package com.backend.figth.client;

import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentProcessorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-processor-fallback")
public interface PaymentProcessorFallbackClient {

  @PostMapping("/payments")
  PaymentProcessorResponseDTO processPayment(@RequestBody PaymentProcessorRequestDTO request);
}