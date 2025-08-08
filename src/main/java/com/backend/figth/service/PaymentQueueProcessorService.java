package com.backend.figth.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import com.backend.figth.dto.PaymentBatchProcessorDTO;
import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;
import com.backend.figth.repository.PaymentQueueRepository;
import com.backend.figth.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentQueueProcessorService {

  private final PaymentQueueRepository paymentQueueRepository;
  private final PaymentService paymentService;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final PaymentRepository paymentRepository;
  private final JdbcTemplate jdbcTemplate;

  @Value("${payment.queue.processor.batch.size:20}")
  private Integer batchSize;

  @PostConstruct
  public void startProcessing() {
    log.info("Starting payment queue processor...");
    CompletableFuture.runAsync(() -> {
      try {
        this.run();
      } catch (Exception e) {
        log.error("Error in payment queue processor", e);
      }
    }, executorService);
  }

  public void run() {
    while (true) {
      try {
        this.processQueue();
      } catch (Exception e) {
        log.error("Error processing queue", e);
      }
    }
  }

  public void processQueue() throws InterruptedException {
    List<PaymentQueue> paymentQueues = this.getPaymentQueues();
    if (paymentQueues.isEmpty()) {
      log.info("No payment queues to process");
      Thread.sleep(1000);
      return;
    }
    List<PaymentBatchProcessorDTO> paymentBatchProcessorDTOs = paymentQueues.stream().map(paymentQueue -> {
      var paymentBatchProcessorDTO = new PaymentBatchProcessorDTO();
      paymentBatchProcessorDTO.setPaymentQueue(paymentQueue);
      return paymentBatchProcessorDTO;
    }).collect(Collectors.toList());
    CompletableFuture
        .allOf(paymentBatchProcessorDTOs.stream()
            .map(paymentService::processPayment)
            .toArray(CompletableFuture[]::new))
        .join();
    List<Payment> payments = getPayments(paymentBatchProcessorDTOs);
    this.persistPayments(payments);
    CompletableFuture
        .allOf(paymentBatchProcessorDTOs.stream()
            .filter(paymentBatchProcessorDTO -> paymentBatchProcessorDTO.getPayment() == null)
            .map(paymentService::processPaymentFallback)
            .toArray(CompletableFuture[]::new))
        .join();
    List<Payment> paymentsFallback = paymentBatchProcessorDTOs.stream()
        .map(PaymentBatchProcessorDTO::getPayment)
        .filter(Objects::nonNull)
        .filter(payment -> "F".equals(payment.getPaymentService()))
        .collect(Collectors.toList());
    this.persistPayments(paymentsFallback);
    List<Long> paymentQueueIdsCommited = new ArrayList<>();
    List<Long> paymentQueueIdsFailed = new ArrayList<>();
    paymentBatchProcessorDTOs.forEach(paymentBatchProcessorDTO -> {
      if (paymentBatchProcessorDTO.getPayment() != null) {
        paymentQueueIdsCommited.add(paymentBatchProcessorDTO.getPaymentQueue().getId());
        return;
      }
      paymentQueueIdsFailed.add(paymentBatchProcessorDTO.getPaymentQueue().getId());
    });
    this.paymentQueueRepository.batchUpdate("C", paymentQueueIdsCommited);
    this.paymentQueueRepository.batchUpdate("Q", paymentQueueIdsFailed);
    if (paymentBatchProcessorDTOs.size() > 0 &&
        (paymentQueueIdsCommited.size()) / (double) paymentBatchProcessorDTOs.size() < 0.5) {
      log.error("Payment queue processor is not working properly, sleeping for 1 second");
      Thread.sleep(1000);
    }

  }

  @Transactional
  private List<Payment> getPayments(List<PaymentBatchProcessorDTO> paymentBatchProcessorDTOs) {
    List<Payment> payments = paymentBatchProcessorDTOs.stream()
        .map(PaymentBatchProcessorDTO::getPayment)
        .filter(Objects::nonNull)
        .filter(payment -> "D".equals(payment.getPaymentService()))
        .collect(Collectors.toList());
    return payments;
  }

  private void persistPayments(List<Payment> payments) {
    this.jdbcTemplate.batchUpdate(
        "INSERT INTO payments (correlationid, amount, payment_service, requested_at) VALUES (?, ?, ?, ?)",
        new BatchPreparedStatementSetter() {
          @Override
          public void setValues(PreparedStatement ps, int i) throws SQLException {
            Payment payment = payments.get(i);
            ps.setObject(1, payment.getCorrelationId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getPaymentService());
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(payment.getRequestedAt()));
          }

          @Override
          public int getBatchSize() {
            return payments.size();
          }
        });

  }

  @Transactional
  private List<PaymentQueue> getPaymentQueues() {
    var paymentQueues = this.paymentQueueRepository.batchReader(this.batchSize);
    this.paymentQueueRepository.batchUpdate("P",
        paymentQueues.stream().map(PaymentQueue::getId).collect(Collectors.toList()));
    return paymentQueues;
  }

}
