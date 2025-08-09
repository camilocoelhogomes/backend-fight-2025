package com.backend.figth.service;

import com.backend.figth.client.PaymentProcessorClient;
import com.backend.figth.client.PaymentProcessorFallbackClient;
import com.backend.figth.dto.PaymentBatchProcessorDTO;
import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentProcessorClient paymentProcessorClient;
	private final PaymentPersistenceService paymentPersistenceService;
	private final PaymentProcessorFallbackClient paymentProcessorFallbackClient;

	@Async("taskExecutor")
	public CompletableFuture<Void> saveToQueue(PaymentRequestDTO request) {
		try {
			log.debug("Saving payment to queue for correlationId: {}", request.getCorrelationId());
			var paymentQueue = new PaymentQueue();
			paymentQueue.setStoredData(request);
			paymentQueue.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
			paymentQueue.setUpdatedAt(LocalDateTime.now(ZoneId.of("UTC")));
			paymentQueue.setQueueStatus("Q");
			var dlqFuture = paymentPersistenceService.persistPaymentQueue(paymentQueue);
			dlqFuture.get(); // Timeout para DLQ

			log.debug("Failed payment successfully saved to DLQ for correlationId: {}",
					request.getCorrelationId());
		} catch (Exception dlqError) {
			log.error("Failed to save payment to DLQ for correlationId: {}",
					request.getCorrelationId(), dlqError);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Async("taskExecutor")
	public CompletableFuture<Void> processPayment(PaymentBatchProcessorDTO paymentBatchProcessorDTO) {
		var paymentQueue = paymentBatchProcessorDTO.getPaymentQueue();
		try {
			var requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					paymentQueue.getStoredData().getCorrelationId(),
					paymentQueue.getStoredData().getAmount(),
					requestTime);

			log.debug("Calling payment processor for correlationId: {}",
					paymentQueue.getStoredData().getCorrelationId());

			paymentProcessorClient.processPayment(processorRequest);
			paymentBatchProcessorDTO.setPayment("D", LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));

		} catch (Exception e) {
			log.error("Error processing payment for correlationId: {}", paymentQueue.getStoredData().getCorrelationId(), e);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Async("taskExecutor")
	public CompletableFuture<Void> processPaymentFallback(PaymentBatchProcessorDTO paymentBatchProcessorDTO) {
		var paymentQueue = paymentBatchProcessorDTO.getPaymentQueue();
		if (paymentBatchProcessorDTO.getPayment() != null) {
			return CompletableFuture.completedFuture(null);
		}
		try {
			var requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					paymentQueue.getStoredData().getCorrelationId(),
					paymentQueue.getStoredData().getAmount(),
					requestTime);

			log.debug("Calling payment processor for correlationId: {}",
					paymentQueue.getStoredData().getCorrelationId());

			paymentProcessorFallbackClient.processPayment(processorRequest);
			paymentBatchProcessorDTO.setPayment("F", LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));

		} catch (Exception e) {
			log.error("Error processing payment for correlationId: {}", paymentQueue.getStoredData().getCorrelationId(), e);
		}
		return CompletableFuture.completedFuture(null);

	}

}