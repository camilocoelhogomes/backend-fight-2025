package com.backend.figth.repository;

import com.backend.figth.dto.PaymentSummaryQueryDTO;
import com.backend.figth.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

        @Query(value = "SELECT p.payment_service as paymentService, COUNT(p.id) as totalRequests, SUM(p.amount) as totalAmount FROM payments p WHERE p.requested_at BETWEEN :fromDate AND :toDate GROUP BY p.payment_service", nativeQuery = true)
        List<PaymentSummaryQueryDTO> getPaymentSummaryByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        @Query(value = "SELECT * FROM payments p WHERE p.correlationid in :correlationId", nativeQuery = true)
        List<Payment> getPaymentsByCorrelationId(@Param("correlationId") List<UUID> correlationId);
}