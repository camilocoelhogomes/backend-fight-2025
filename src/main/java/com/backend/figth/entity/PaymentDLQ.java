package com.backend.figth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_dlq")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDLQ {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "correlationid", nullable = false)
  private UUID correlationId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "partition_key", nullable = false)
  private Integer partitionKey;

  @Column(name = "processed", nullable = false)
  private Boolean processed;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}