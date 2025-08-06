package com.backend.figth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.backend.figth.dto.PaymentRequestDTO;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQueue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "stored_data", columnDefinition = "jsonb", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private PaymentRequestDTO storedData;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "queue_status", nullable = false)
  private String queueStatus;

}