package com.backend.figth.repository;

import com.backend.figth.dto.PaymentSummaryQueryDTO;
import com.backend.figth.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

        @Query(value = "SELECT p.payment_service, COUNT(p.id) as total_requests, SUM(p.amount) as total_amount FROM payments p WHERE p.requested_at BETWEEN :fromDate AND :toDate GROUP BY p.payment_service", nativeQuery = true)
        List<PaymentSummaryQueryDTO> getPaymentSummaryByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
}