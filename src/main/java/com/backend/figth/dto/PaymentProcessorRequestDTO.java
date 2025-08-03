package com.backend.figth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class PaymentProcessorRequestDTO {
  private String correlationId;
  private BigDecimal amount;
  private Instant requestedAt;
}