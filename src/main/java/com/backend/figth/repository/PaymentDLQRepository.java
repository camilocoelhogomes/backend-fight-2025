package com.backend.figth.repository;

import com.backend.figth.entity.PaymentDLQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentDLQRepository extends JpaRepository<PaymentDLQ, Long> {
}