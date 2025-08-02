package com.backend.figth.client;

import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentProcessorResponseDTO;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-processor")
public interface PaymentProcessorClient {

  @Retry(name = "payment-processor", fallbackMethod = "processPaymentFallback")
  @PostMapping("/payments")
  PaymentProcessorResponseDTO processPayment(@RequestBody PaymentProcessorRequestDTO request);

  // Método fallback que será chamado se retry estiver desabilitado ou falhar
  default PaymentProcessorResponseDTO processPaymentFallback(PaymentProcessorRequestDTO request, Exception e) {
    // Retorna uma resposta de erro padrão
    return new PaymentProcessorResponseDTO("Payment processor unavailable", "FAILED");
  }
}