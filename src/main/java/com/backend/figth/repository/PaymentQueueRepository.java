package com.backend.figth.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.figth.entity.PaymentQueue;

import jakarta.transaction.Transactional;

@Repository
public interface PaymentQueueRepository extends JpaRepository<PaymentQueue, Long> {
  @Modifying
  @Transactional
  @Query(value = """
        WITH locked_rows AS (
            SELECT id
            FROM payment_queue
            WHERE queue_status = 'Q'
            ORDER BY id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
        )
        UPDATE payment_queue pq
        SET queue_status = 'P',
        updated_at = :updatedAt
        FROM locked_rows
        WHERE pq.id = locked_rows.id
        RETURNING pq.*
      """, nativeQuery = true)
  List<PaymentQueue> batchReader(@Param("limit") Integer limit, @Param("updatedAt") LocalDateTime updatedAt);

  @Modifying
  @Transactional
  @Query(value = "UPDATE payment_queue SET queue_status = :status WHERE id IN (:ids)", nativeQuery = true)
  void batchUpdate(@Param("status") String status, @Param("ids") List<Long> id);

  @Modifying
  @Transactional
  @Query(value = """
      UPDATE payment_queue
      SET queue_status = 'Q',
      updated_at = :updatedAt
      WHERE queue_status = 'P'
      AND updated_at < :cutoffTime
      """, nativeQuery = true)
  void updateStaleProcessingRecords(@Param("cutoffTime") LocalDateTime cutoffTime,
      @Param("updatedAt") LocalDateTime updatedAt);

}