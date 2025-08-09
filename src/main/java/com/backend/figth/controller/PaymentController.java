package com.backend.figth.controller;

import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.dto.PaymentResponseDTO;
import com.backend.figth.dto.PaymentSummaryResponseDTO;
import com.backend.figth.service.PaymentService;
import com.backend.figth.service.PaymentSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class PaymentController {

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private PaymentSummaryService paymentSummaryService;

  @Value("${process.strategy:default}")
  private String processStrategy;

  @PostMapping("/payments")
  public ResponseEntity<PaymentResponseDTO> createPayment(
      @RequestBody PaymentRequestDTO request) {
    log.debug("Received payment request with correlationId: {}", request.getCorrelationId());

    this.paymentService.saveToQueue(request);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(new PaymentResponseDTO("Payment request accepted for processing", "ACCEPTED"));

  }

  @GetMapping("/payments-summary")
  public ResponseEntity<PaymentSummaryResponseDTO> getPaymentSummary(
      @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
      throws InterruptedException, ExecutionException {

    log.debug("Getting payment summary from {} to {}", from, to);

    PaymentSummaryResponseDTO summary = paymentSummaryService.getPaymentSummary(from, to).get();
    return ResponseEntity.ok(summary);
  }
}