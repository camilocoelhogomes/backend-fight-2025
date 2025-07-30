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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProcessorClient paymentProcessorClient;
    private final PaymentRepository paymentRepository;

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        log.info("Processing payment with correlationId: {} and amount: {}",
                request.getCorrelationId(), request.getAmount());

        var requestTime = Instant.now();

        // Cria a requisição para o payment processor
        PaymentProcessorRequestDTO processorRequest = new PaymentProcessorRequestDTO(
                request.getCorrelationId(),
                request.getAmount(),
                requestTime);

        // Chama o payment processor via Feign
        PaymentProcessorResponseDTO processorResponse = paymentProcessorClient.processPayment(processorRequest);

        log.info("Payment processor response: {}", processorResponse.getMessage());

        // Persiste o pagamento no banco de dados após sucesso
        Payment payment = new Payment();
        payment.setCorrelationId(UUID.fromString(request.getCorrelationId()));
        payment.setAmount(request.getAmount());
        payment.setRequestedAt(LocalDateTime.ofInstant(requestTime, ZoneId.systemDefault()));
        payment.setPaymentService("D");

        paymentRepository.save(payment);
        log.info("Payment saved to database with correlationId: {}", request.getCorrelationId());

        return new PaymentResponseDTO(
                processorResponse.getMessage(),
                "SUCCESS");
    }
}