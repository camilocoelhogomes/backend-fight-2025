package com.backend.figth.service;

import com.backend.figth.dto.PaymentSummaryDTO;
import com.backend.figth.dto.PaymentSummaryQueryDTO;
import com.backend.figth.dto.PaymentSummaryResponseDTO;
import com.backend.figth.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSummaryService {

  private final PaymentRepository paymentRepository;

  @Async("dbExecutor")
  public PaymentSummaryResponseDTO getPaymentSummary(LocalDateTime fromDate, LocalDateTime toDate) {
    log.info("Getting payment summary from {} to {}", fromDate, toDate);

    List<PaymentSummaryQueryDTO> results = paymentRepository.getPaymentSummaryByDateRange(fromDate, toDate);

    PaymentSummaryDTO defaultService = new PaymentSummaryDTO(0L, BigDecimal.ZERO);
    PaymentSummaryDTO fallback = new PaymentSummaryDTO(0L, BigDecimal.ZERO);

    for (PaymentSummaryQueryDTO result : results) {
      if ("D".equals(result.getPaymentService())) {
        defaultService = new PaymentSummaryDTO(result.getTotalRequests(), result.getTotalAmount());
      } else if ("F".equals(result.getPaymentService())) {
        fallback = new PaymentSummaryDTO(result.getTotalRequests(), result.getTotalAmount());
      }
    }

    return new PaymentSummaryResponseDTO(defaultService, fallback);
  }
}