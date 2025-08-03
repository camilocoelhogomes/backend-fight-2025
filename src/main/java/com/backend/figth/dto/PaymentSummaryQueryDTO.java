package com.backend.figth.dto;

import java.math.BigDecimal;

public record PaymentSummaryQueryDTO(
    Character paymentService,
    Long totalRequests,
    BigDecimal totalAmount) {
}