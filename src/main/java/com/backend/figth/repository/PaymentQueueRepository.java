package com.backend.figth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.figth.entity.PaymentQueue;

@Repository
public interface PaymentQueueRepository extends JpaRepository<PaymentQueue, Long> {

  @Query(value = "SELECT * FROM payment_queue WHERE queue_status = 'Q' ORDER BY id ASC FOR UPDATE SKIP LOCKED LIMIT :limit", nativeQuery = true)
  List<PaymentQueue> batchReader(@Param("limit") Integer limit);

  @Modifying
  @Transactional(propagation = Propagation.REQUIRED)
  @Query(value = "UPDATE payment_queue SET queue_status = :status WHERE id IN (:ids)", nativeQuery = true)
  void batchUpdate(@Param("status") String status, @Param("ids") List<Long> id);

}