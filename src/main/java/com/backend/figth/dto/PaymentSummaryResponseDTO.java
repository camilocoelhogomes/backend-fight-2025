package com.backend.figth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResponseDTO {
  private PaymentSummaryDTO defaultService;
  private PaymentSummaryDTO fallback;
}