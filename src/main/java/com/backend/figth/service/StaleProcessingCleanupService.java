package com.backend.figth.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import com.backend.figth.repository.PaymentQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaleProcessingCleanupService {

  private final PaymentQueueRepository paymentQueueRepository;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Value("${payment.queue.stale.timeout.seconds:3}")
  private Integer timeoutSeconds;

  @PostConstruct
  public void startCleanupProcess() {
    log.debug("Starting stale processing cleanup service with timeout: {}s", timeoutSeconds);

    CompletableFuture.runAsync(() -> {
      try {
        this.run();
      } catch (Exception e) {
        log.error("Error in stale processing cleanup service", e);
      }
    }, executorService);
  }

  public void run() {
    while (true) {
      try {
        this.processStaleRecords();
        Thread.sleep(timeoutSeconds * 1000L);
      } catch (InterruptedException e) {
        log.error("Stale processing cleanup service interrupted", e);
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        log.error("Error in stale processing cleanup service", e);
      }
    }
  }

  private void processStaleRecords() {
    try {
      LocalDateTime cutoffTime = LocalDateTime.now().atZone(ZoneId.of("UTC"))
          .minusSeconds(this.timeoutSeconds).toLocalDateTime();
      LocalDateTime updatedAt = LocalDateTime.now().atZone(ZoneId.of("UTC")).toLocalDateTime();

      this.paymentQueueRepository
          .updateStaleProcessingRecords(cutoffTime, updatedAt);

    } catch (Exception e) {
      log.error("Error processing stale records", e);
    }
  }
}