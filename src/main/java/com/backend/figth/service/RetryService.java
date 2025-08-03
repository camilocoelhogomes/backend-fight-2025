package com.backend.figth.service;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RetryService {

  private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Logger log = LoggerFactory.getLogger(RetryService.class);

  @Value("${retry.maxAttempts:3}")
  private int maxAttempts;

  public <T> T retry(Callable<T> callable) throws Exception {
    CompletableFuture<T> future = new CompletableFuture<>();
    retry(callable, future, maxAttempts, 1);
    return future.join();
  }

  private <T> void retry(Callable<T> callable, CompletableFuture<T> future, int maxAttempts,
      int attempt) {
    try {
      log.info("Executing retry attempt {} of {}", attempt, maxAttempts);
      T result = callable.call();
      future.complete(result);
    } catch (Exception e) {
      log.error("Error during retry attempt {} of {}: {}", attempt, maxAttempts, e.getMessage());

      if (attempt >= maxAttempts) {
        log.error("All {} retry attempts failed", maxAttempts);
        future.completeExceptionally(e);
        return;
      }
      // Retry exponencial: 1s, 2s, 4s, 8s, etc.
      long delayMs = (long) Math.pow(2, attempt - 1) * 1000;
      log.info("Scheduling retry attempt {} in {} ms", attempt + 1, delayMs);

      // Usar ScheduledExecutor para agendar próxima tentativa
      executor.schedule(() -> retry(callable, future, maxAttempts, attempt + 1), delayMs, TimeUnit.MILLISECONDS);

    }
  }
}
