package com.backend.figth.controller;

import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.dto.PaymentResponseDTO;
import com.backend.figth.dto.PaymentSummaryResponseDTO;
import com.backend.figth.service.PaymentService;
import com.backend.figth.service.PaymentSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentSummaryService paymentSummaryService;

  @PostMapping
  public ResponseEntity<PaymentResponseDTO> createPayment(
      @RequestBody PaymentRequestDTO request) {
    log.info("Received payment request with correlationId: {}", request.getCorrelationId());

    // Dispara o processamento assíncrono sem aguardar o resultado
    this.paymentService.processPaymentAsync(request);

    // Retorna imediatamente com status 202 Accepted
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new PaymentResponseDTO("Payment request accepted for processing", "ACCEPTED"));
  }

  @GetMapping("/summary")
  public ResponseEntity<PaymentSummaryResponseDTO> getPaymentSummary(
      @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

    log.info("Getting payment summary from {} to {}", from, to);

    PaymentSummaryResponseDTO summary = paymentSummaryService.getPaymentSummary(from, to);

    return ResponseEntity.ok(summary);
  }
}