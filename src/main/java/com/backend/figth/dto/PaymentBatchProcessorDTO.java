package com.backend.figth.dto;

import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBatchProcessorDTO {
  private PaymentQueue paymentQueue;
  private Payment payment;
}
