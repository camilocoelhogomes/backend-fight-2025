package com.backend.figth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryQueryDTO {
  private String paymentService;
  private Long totalRequests;
  private BigDecimal totalAmount;
}