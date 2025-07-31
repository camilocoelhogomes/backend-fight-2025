package com.backend.figth.controller;

import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.dto.PaymentResponseDTO;
import com.backend.figth.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping
  public CompletableFuture<ResponseEntity<PaymentResponseDTO>> createPayment(
      @RequestBody PaymentRequestDTO request) {
    log.info("Received payment request with correlationId: {}", request.getCorrelationId());

    return this.paymentService
        .processPaymentAsync(request)
        .thenApply(v -> ResponseEntity.ok(new PaymentResponseDTO()));

  }
}