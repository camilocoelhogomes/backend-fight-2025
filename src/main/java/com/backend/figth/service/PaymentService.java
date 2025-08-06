package com.backend.figth.service;

import com.backend.figth.client.PaymentProcessorClient;
import com.backend.figth.client.PaymentProcessorFallbackClient;
import com.backend.figth.dto.PaymentBatchProcessorDTO;
import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentProcessorResponseDTO;
import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.dto.PaymentResponseDTO;
import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentQueue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentProcessorClient paymentProcessorClient;
	private final PaymentPersistenceService paymentPersistenceService;
	private final PaymentProcessorFallbackClient paymentProcessorFallbackClient;

	@Async("taskExecutor") // Virtual threads para I/O não-bloqueante
	public CompletableFuture<PaymentResponseDTO> processPaymentAsync(PaymentRequestDTO request) {
		log.info("Starting async payment processing for correlationId: {} and amount: {}",
				request.getCorrelationId(), request.getAmount());

		try {
			Payment payment = processPayment(request);

			log.info("Payment processed successfully for correlationId: {}", request.getCorrelationId());

			// Passo 2: Persistir no banco de dados (thread pool - I/O bloqueante)
			log.info("Saving payment to database for correlationId: {}", request.getCorrelationId());

			// Usar thread pool para persistência
			paymentPersistenceService.persistPayment(payment).get();

			log.info("Payment successfully saved to database for correlationId: {}",
					request.getCorrelationId());

			log.info("Async payment processing completed successfully for correlationId: {}",
					request.getCorrelationId());

			return CompletableFuture.completedFuture(new PaymentResponseDTO("Payment processed successfully",
					"SUCCESS"));

		} catch (Exception e) {
			log.warn("Error during async payment processing for correlationId: {}",
					request.getCorrelationId(), e);

			// Capturar falha na DLQ usando thread pool
			saveToQueue(request);

			return CompletableFuture.completedFuture(new PaymentResponseDTO(e.getMessage(), "FAILED"));
		}
	}

	public Payment processPayment(PaymentRequestDTO request) {
		var paymentService = "D";
		// Gerar nova data a cada tentativa
		log.info("=== Processing payment for correlationId: {} ===",
				request.getCorrelationId());
		PaymentProcessorResponseDTO processorResponse = null;
		Instant requestTime = null;
		try {
			requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					request.getCorrelationId(),
					request.getAmount(),
					requestTime);

			log.info("Calling payment processor for correlationId: {}", request.getCorrelationId());

			// Passo 2: Chamar payment processor
			processorResponse = paymentProcessorClient.processPayment(processorRequest);
			// Passo 3: Criar entidade Payment com a data atual da tentativa
			Payment payment = new Payment();
			payment.setCorrelationId(UUID.fromString(request.getCorrelationId()));
			payment.setAmount(request.getAmount());
			payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));
			payment.setPaymentService(paymentService);

			log.info("Payment entity created for correlationId: {} with timestamp: {}",
					request.getCorrelationId(), payment.getRequestedAt());

			return payment;
		} catch (Exception e) {
			requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					request.getCorrelationId(),
					request.getAmount(),
					requestTime);
			paymentService = "F";
			processorResponse = paymentProcessorFallbackClient.processPayment(processorRequest);
		}
		// Passo 3: Criar entidade Payment com a data atual da tentativa
		Payment payment = new Payment();
		payment.setCorrelationId(UUID.fromString(request.getCorrelationId()));
		payment.setAmount(request.getAmount());
		payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));
		payment.setPaymentService(paymentService);

		log.info("Payment entity created for correlationId: {} with timestamp: {}",
				request.getCorrelationId(), payment.getRequestedAt());
		log.info("Payment processor response received for correlationId: {} - Message: {}",
				request.getCorrelationId(), processorResponse.getMessage());

		return payment;

	}

	public void saveToQueue(PaymentRequestDTO request) {
		try {
			log.info("Saving payment to queue for correlationId: {}", request.getCorrelationId());
			var paymentQueue = new PaymentQueue();
			paymentQueue.setStoredData(request);
			paymentQueue.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
			paymentQueue.setQueueStatus("Q");
			var dlqFuture = paymentPersistenceService.persistPaymentQueue(paymentQueue);
			dlqFuture.get(5, TimeUnit.SECONDS); // Timeout para DLQ

			log.info("Failed payment successfully saved to DLQ for correlationId: {}",
					request.getCorrelationId());
		} catch (Exception dlqError) {
			log.error("Failed to save payment to DLQ for correlationId: {}",
					request.getCorrelationId(), dlqError);
		}
	}

	@Async("taskExecutor")
	public CompletableFuture<PaymentBatchProcessorDTO> processPayment(PaymentQueue paymentQueue) {
		PaymentBatchProcessorDTO paymentBatchProcessorDTO = new PaymentBatchProcessorDTO();
		paymentBatchProcessorDTO.setPaymentQueue(paymentQueue);
		try {
			var requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					paymentQueue.getStoredData().getCorrelationId(),
					paymentQueue.getStoredData().getAmount(),
					requestTime);

			log.info("Calling payment processor for correlationId: {}",
					paymentQueue.getStoredData().getCorrelationId());

			// Passo 2: Chamar payment processor
			var processorResponse = paymentProcessorClient.processPayment(processorRequest);
			Payment payment = new Payment();
			payment.setCorrelationId(UUID.fromString(paymentQueue.getStoredData().getCorrelationId()));
			payment.setAmount(paymentQueue.getStoredData().getAmount());
			payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));
			payment.setPaymentService("D");

			paymentBatchProcessorDTO.setPayment(payment);
		} catch (Exception e) {
			log.error("Error processing payment for correlationId: {}", paymentQueue.getStoredData().getCorrelationId(), e);
		}
		return CompletableFuture.completedFuture(paymentBatchProcessorDTO);
	}

	@Async("taskExecutor")
	public CompletableFuture<PaymentBatchProcessorDTO> processPaymentFallback(PaymentQueue paymentQueue) {
		PaymentBatchProcessorDTO paymentBatchProcessorDTO = new PaymentBatchProcessorDTO();
		paymentBatchProcessorDTO.setPaymentQueue(paymentQueue);
		try {
			var requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					paymentQueue.getStoredData().getCorrelationId(),
					paymentQueue.getStoredData().getAmount(),
					requestTime);

			log.info("Calling payment processor for correlationId: {}",
					paymentQueue.getStoredData().getCorrelationId());

			// Passo 2: Chamar payment processor
			var processorResponse = paymentProcessorFallbackClient.processPayment(processorRequest);
			Payment payment = new Payment();
			payment.setCorrelationId(UUID.fromString(paymentQueue.getStoredData().getCorrelationId()));
			payment.setAmount(paymentQueue.getStoredData().getAmount());
			payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));
			payment.setPaymentService("F");

			paymentBatchProcessorDTO.setPayment(payment);
		} catch (Exception e) {
			log.error("Error processing payment for correlationId: {}", paymentQueue.getStoredData().getCorrelationId(), e);
		}
		return CompletableFuture.completedFuture(paymentBatchProcessorDTO);
	}
}