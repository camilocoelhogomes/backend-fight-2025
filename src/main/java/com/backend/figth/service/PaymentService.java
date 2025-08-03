package com.backend.figth.service;

import com.backend.figth.client.PaymentProcessorClient;
import com.backend.figth.client.PaymentProcessorFallbackClient;
import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentProcessorResponseDTO;
import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.dto.PaymentResponseDTO;
import com.backend.figth.entity.Payment;
import com.backend.figth.entity.PaymentDLQ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
			saveToDLQ(request, Instant.now(), e);

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
		} catch (Exception e) {
			requestTime = Instant.now();
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					request.getCorrelationId(),
					request.getAmount(),
					requestTime);
			paymentService = "F";
			processorResponse = paymentProcessorFallbackClient.processPayment(processorRequest);
		}

		log.info("Payment processor response received for correlationId: {} - Message: {}",
				request.getCorrelationId(), processorResponse.getMessage());

		// Passo 3: Criar entidade Payment com a data atual da tentativa
		Payment payment = new Payment();
		payment.setCorrelationId(UUID.fromString(request.getCorrelationId()));
		payment.setAmount(request.getAmount());
		payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));
		payment.setPaymentService(paymentService);

		log.info("Payment entity created for correlationId: {} with timestamp: {}",
				request.getCorrelationId(), payment.getRequestedAt());

		return payment;
	}

	private void saveToDLQ(PaymentRequestDTO request, Instant requestTime, Exception error) {
		try {
			log.info("Saving failed payment to DLQ for correlationId: {}", request.getCorrelationId());

			PaymentDLQ dlqEntry = new PaymentDLQ();

			// Corrigir UUID inválido
			try {
				dlqEntry.setCorrelationId(UUID.fromString(request.getCorrelationId()));
			} catch (IllegalArgumentException e) {
				// Se UUID inválido, usar UUID aleatório
				dlqEntry.setCorrelationId(UUID.randomUUID());
				log.warn("Invalid UUID '{}', using random UUID: {}",
						request.getCorrelationId(), dlqEntry.getCorrelationId());
			}

			dlqEntry.setAmount(request.getAmount());
			dlqEntry.setPartitionKey(1);
			dlqEntry.setProcessed(false);
			dlqEntry.setCreatedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));

			// Usar thread pool para DLQ
			CompletableFuture<PaymentDLQ> dlqFuture = paymentPersistenceService.persistPaymentDLQ(dlqEntry);
			dlqFuture.get(5, TimeUnit.SECONDS); // Timeout para DLQ

			log.info("Failed payment successfully saved to DLQ for correlationId: {}",
					request.getCorrelationId());
		} catch (Exception dlqError) {
			log.error("Failed to save payment to DLQ for correlationId: {}",
					request.getCorrelationId(), dlqError);
		}
	}
}