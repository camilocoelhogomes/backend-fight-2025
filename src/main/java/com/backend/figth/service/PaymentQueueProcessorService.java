package com.backend.figth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.figth.dto.PaymentBatchProcessorDTO;
import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;
import com.backend.figth.repository.PaymentQueueRepository;
import com.backend.figth.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentQueueProcessorService {

  private final PaymentQueueRepository paymentQueueRepository;
  private final PaymentService paymentService;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final PaymentRepository paymentRepository;

  @Value("${payment.queue.processor.batch.size:20}")
  private Integer batchSize;

  @Transactional
  public Double processQueue() throws InterruptedException, ExecutionException {
    // 1. Pegar as filas
    var paymentQueues = paymentQueueRepository.batchReader(batchSize);
    // 2. Processar as filas
    List<PaymentBatchProcessorDTO> paymentBatchProcessorDTOs = paymentQueues.stream()
        .map(i -> paymentService.processPayment(i))
        .map(i -> i.join())
        .collect(Collectors.toList());
    List<Payment> payments = new ArrayList<>();
    List<PaymentQueue> deletedPaymentQueues = new ArrayList<>();
    // 3. Processar as filas de fallback
    var paymentFallback = paymentBatchProcessorDTOs.stream()
        .peek(p -> {
          if (Objects.nonNull(p.getPayment())) {
            payments.add(p.getPayment());
            deletedPaymentQueues.add(p.getPaymentQueue());
          }
        })
        .filter(p -> Objects.isNull(p.getPayment()))
        .map(PaymentBatchProcessorDTO::getPaymentQueue)
        .map(paymentService::processPaymentFallback)
        .collect(Collectors.toList());
    // 4. Salvar as filas
    var saveDefualt = CompletableFuture.runAsync(() -> {
      this.paymentRepository.saveAll(payments);
    }, executorService);
    // 5. Salvar as filas de fallback
    List<PaymentBatchProcessorDTO> fallBackProcess = paymentFallback.stream()
        .map(i -> i.join())
        .collect(Collectors.toList());
    // 6. Salvar as filas de fallback
    List<Payment> fallBackPayments = fallBackProcess.stream().map(i -> {
      if (Objects.nonNull(i.getPayment())) {
        deletedPaymentQueues.add(i.getPaymentQueue());
        return i.getPayment();
      }
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toList());

    var saveFallBack = CompletableFuture.runAsync(() -> {
      this.paymentRepository.saveAll(fallBackPayments);
    }, executorService);

    CompletableFuture.allOf(saveDefualt, saveFallBack).get();
    paymentQueueRepository.deleteAll(deletedPaymentQueues);
    return (double) deletedPaymentQueues.size() / paymentQueues.size();

  }

  @PostMapping("/process")
  public void run() {
    try {
      while (true) {
        CompletableFuture.runAsync(() -> {
          try {
            this.processQueue();
          } catch (Exception e) {
            log.error("Error processing queue", e);
          }
        }, executorService).get();
      }
    } catch (Exception e) {
      log.error("Error processing queue", e);
    }
  }
}
