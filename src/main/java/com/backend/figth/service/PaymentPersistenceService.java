package com.backend.figth.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentDLQ;
import com.backend.figth.repository.PaymentDLQRepository;
import com.backend.figth.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPersistenceService {

  private final PaymentRepository paymentRepository;
  private final PaymentDLQRepository paymentDLQRepository;

  @Async("dbExecutor")
  public CompletableFuture<Payment> persistPayment(Payment payment) {
    log.debug("Persisting payment with correlationId: {}", payment.getCorrelationId());

    try {
      Payment savedPayment = paymentRepository.save(payment);
      log.debug("Payment persisted successfully: {}", savedPayment.getCorrelationId());
      return CompletableFuture.completedFuture(savedPayment);
    } catch (Exception e) {
      log.error("Error persisting payment: {}", payment.getCorrelationId(), e);
      return CompletableFuture.failedFuture(e);
    }
  }

  @Async("dbExecutor")
  public CompletableFuture<PaymentDLQ> persistPaymentDLQ(PaymentDLQ payment) {
    log.debug("Persisting DLQ payment with correlationId: {}", payment.getCorrelationId());

    try {
      PaymentDLQ savedPayment = paymentDLQRepository.save(payment);
      log.debug("DLQ payment persisted successfully: {}", savedPayment.getCorrelationId());
      return CompletableFuture.completedFuture(savedPayment);
    } catch (Exception e) {
      log.error("Error persisting DLQ payment: {}", payment.getCorrelationId(), e);
      return CompletableFuture.failedFuture(e);
    }
  }
}