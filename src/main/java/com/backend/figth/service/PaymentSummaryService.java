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
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSummaryService {

  private final PaymentRepository paymentRepository;

  @Async("dbExecutor")
  public CompletableFuture<PaymentSummaryResponseDTO> getPaymentSummary(LocalDateTime fromDate, LocalDateTime toDate) {
    try {
      log.info("Getting payment summary from {} to {}", fromDate, toDate);

      List<PaymentSummaryQueryDTO> results = paymentRepository.getPaymentSummaryByDateRange(fromDate, toDate);

      PaymentSummaryDTO defaultService = new PaymentSummaryDTO(0L, BigDecimal.ZERO);
      PaymentSummaryDTO fallback = new PaymentSummaryDTO(0L, BigDecimal.ZERO);

      for (PaymentSummaryQueryDTO result : results) {
        if ('D' == result.paymentService()) {
          defaultService = new PaymentSummaryDTO(result.totalRequests(), result.totalAmount());
        } else if ('F' == result.paymentService()) {
          fallback = new PaymentSummaryDTO(result.totalRequests(), result.totalAmount());
        }
      }

      return CompletableFuture.completedFuture(new PaymentSummaryResponseDTO(defaultService, fallback));
    } catch (Exception e) {
      log.error("Error getting payment summary from {} to {}", fromDate, toDate, e);
      throw e;
    }
  }
}