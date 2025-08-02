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

    @Query("SELECT new com.backend.figth.dto.PaymentSummaryQueryDTO(p.paymentService, COUNT(p), SUM(p.amount)) " +
            "FROM Payment p " +
            "WHERE p.requestedAt BETWEEN :fromDate AND :toDate " +
            "GROUP BY p.paymentService")
    List<PaymentSummaryQueryDTO> getPaymentSummaryByDateRange(@Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}