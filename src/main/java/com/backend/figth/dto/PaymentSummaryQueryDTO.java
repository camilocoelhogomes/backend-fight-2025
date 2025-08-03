package com.backend.figth.dto;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class PaymentSummaryQueryDTO {
    private Character paymentService;
    private Long totalRequests;
    private BigDecimal totalAmount;

    public PaymentSummaryQueryDTO(Character paymentService, Long totalRequests, BigDecimal totalAmount) {
        this.paymentService = paymentService;
        this.totalRequests = totalRequests;
        this.totalAmount = totalAmount;
    }
}