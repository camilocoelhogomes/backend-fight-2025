package com.backend.figth.service;

import com.backend.figth.client.PaymentProcessorClient;
import com.backend.figth.dto.PaymentProcessorRequestDTO;
import com.backend.figth.dto.PaymentProcessorResponseDTO;
import com.backend.figth.dto.PaymentRequestDTO;
import com.backend.figth.dto.PaymentResponseDTO;
import com.backend.figth.entity.Payment;
import com.backend.figth.repository.PaymentRepository;
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
	private final PaymentRepository paymentRepository;

	@Async("taskExecutor")
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

			// Passo 2: Chamar payment processor (sequencial)
			PaymentProcessorResponseDTO processorResponse = paymentProcessorClient
					.processPayment(processorRequest);

			log.info("Payment processor response received for correlationId: {} - Message: {}",
					request.getCorrelationId(), processorResponse.getMessage());

			// Passo 3: Persistir no banco de dados (sequencial)
			log.info("Saving payment to database for correlationId: {}", request.getCorrelationId());

			Payment payment = new Payment();
			payment.setCorrelationId(UUID.fromString(request.getCorrelationId()));
			payment.setAmount(request.getAmount());
			payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.systemDefault()));
			payment.setPaymentService("D");

			paymentRepository.save(payment);

			log.info("Payment successfully saved to database for correlationId: {}",
					request.getCorrelationId());

			// Retornar resposta de sucesso
			PaymentResponseDTO response = new PaymentResponseDTO(
					processorResponse.getMessage(),
					"SUCCESS");

			log.info("Async payment processing completed successfully for correlationId: {}",
					request.getCorrelationId());

			return CompletableFuture.completedFuture(response);

		} catch (Exception e) {
			log.error("Error during async payment processing for correlationId: {}",
					request.getCorrelationId(), e);
			return CompletableFuture.failedFuture(e);
		}
	}
}