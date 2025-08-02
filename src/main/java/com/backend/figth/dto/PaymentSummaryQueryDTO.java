package com.backend.figth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryQueryDTO {
  @Id
  @Column(name = "payment_service")
  private Character paymentService;
  @Column(name = "total_requests")
  private Long totalRequests;
  @Column(name = "total_amount")
  private BigDecimal totalAmount;
}