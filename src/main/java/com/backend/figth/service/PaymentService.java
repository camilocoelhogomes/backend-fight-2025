package com.backend.figth.service;

import com.backend.figth.client.PaymentProcessorClient;
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
import io.github.resilience4j.retry.annotation.Retry;
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

	@Async("taskExecutor") // Virtual threads para I/O não-bloqueante
	public CompletableFuture<PaymentResponseDTO> processPaymentAsync(PaymentRequestDTO request) {
		log.info("Starting async payment processing for correlationId: {} and amount: {}",
				request.getCorrelationId(), request.getAmount());

		var requestTime = Instant.now();

		try {
			// Passo 1: Criar requisição para o payment processor
			PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
					request.getCorrelationId(),
					request.getAmount(),
					requestTime);

			log.info("Calling payment processor for correlationId: {}", request.getCorrelationId());
			// Passo 2: Chamar payment processor (virtual thread - I/O não-bloqueante)
			PaymentProcessorResponseDTO processorResponse = callPaymentProcessor(processorRequest);

			log.info("Payment processor response received for correlationId: {} - Message: {}",
					request.getCorrelationId(), processorResponse.getMessage());

			// Passo 3: Persistir no banco de dados (thread pool - I/O bloqueante)
			log.info("Saving payment to database for correlationId: {}", request.getCorrelationId());

			Payment payment = new Payment();
			payment.setCorrelationId(UUID.fromString(request.getCorrelationId()));
			payment.setAmount(request.getAmount());
			payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.of("UTC")));
			payment.setPaymentService("D");

			// Usar thread pool para persistência
			CompletableFuture<Payment> persistenceFuture = paymentPersistenceService.persistPayment(payment);

			// Aguardar persistência com timeout
			Payment savedPayment = persistenceFuture.get(10, TimeUnit.SECONDS);

			log.info("Payment successfully saved to database for correlationId: {}",
					request.getCorrelationId());

			log.info("Async payment processing completed successfully for correlationId: {}",
					request.getCorrelationId());

			return CompletableFuture.completedFuture(new PaymentResponseDTO(processorResponse.getMessage(),
					"SUCCESS"));

		} catch (Exception e) {
			log.warn("Error during async payment processing for correlationId: {}",
					request.getCorrelationId(), e);

			// Capturar falha na DLQ usando thread pool
			saveToDLQ(request, requestTime, e);

			return CompletableFuture.completedFuture(new PaymentResponseDTO(e.getMessage(), "FAILED"));
		}
	}

	@Retry(name = "payment-processor")
	public PaymentProcessorResponseDTO callPaymentProcessor(PaymentProcessorRequestDTO request) {
		return paymentProcessorClient.processPayment(request);
	}

	private void saveToDLQ(PaymentRequestDTO request, Instant requestTime, Exception error) {
		try {
			log.info("Saving failed payment to DLQ for correlationId: {}", request.getCorrelationId());

			PaymentDLQ dlqEntry = new PaymentDLQ();
			dlqEntry.setCorrelationId(UUID.fromString(request.getCorrelationId()));
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