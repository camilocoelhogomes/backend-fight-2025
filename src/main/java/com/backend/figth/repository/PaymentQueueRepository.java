package com.backend.figth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.figth.entity.PaymentQueue;

@Repository
public interface PaymentQueueRepository extends JpaRepository<PaymentQueue, Long> {
}