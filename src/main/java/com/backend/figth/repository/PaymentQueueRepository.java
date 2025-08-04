package com.backend.figth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.figth.entity.PaymentQueue;

@Repository
public interface PaymentQueueRepository extends JpaRepository<PaymentQueue, Long> {

  @Query(value = "SELECT * FROM payment_queue FOR UPDATE SKIP LOCKED LIMIT :limit", nativeQuery = true)
  List<PaymentQueue> batchReader(@Param("limit") Integer limit);

}