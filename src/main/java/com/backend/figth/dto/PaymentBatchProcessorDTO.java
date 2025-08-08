package com.backend.figth.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;

import lombok.Getter;

@Getter
public class PaymentBatchProcessorDTO {

  private final PaymentQueue paymentQueue;
  private Payment payment;

  public PaymentBatchProcessorDTO(PaymentQueue paymentQueue) {
    this.paymentQueue = paymentQueue;
  }

  public void setPayment(String paymentService, LocalDateTime requestTime) {
    if (Objects.nonNull(this.payment)) {
      return;
    }
    var payment = new Payment();
    payment.setCorrelationId(UUID.fromString(paymentQueue.getStoredData().getCorrelationId()));
    payment.setAmount(paymentQueue.getStoredData().getAmount());
    payment.setRequestedAt(requestTime);
    payment.setPaymentService(paymentService);
    this.payment = payment;
  }

  public UUID getCorrelationId() {
    return UUID.fromString(this.paymentQueue.getStoredData().getCorrelationId());
  }

}
