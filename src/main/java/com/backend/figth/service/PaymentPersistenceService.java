package com.backend.figth.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;
import com.backend.figth.repository.PaymentQueueRepository;
import com.backend.figth.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPersistenceService {

  private final PaymentRepository paymentRepository;
  private final PaymentQueueRepository paymentQueueRepository;

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
  public CompletableFuture<PaymentQueue> persistPaymentQueue(PaymentQueue payment) {

    try {
      PaymentQueue savedPayment = paymentQueueRepository.save(payment);
      log.debug("DLQ payment persisted successfully: {}", savedPayment.getId());
      return CompletableFuture.completedFuture(savedPayment);
    } catch (Exception e) {
      log.error("Error persisting DLQ payment: {}", payment.getId(), e);
      return CompletableFuture.failedFuture(e);
    }
  }
}