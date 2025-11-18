package com.gymflow.api.repository.entity;

import com.gymflow.api.core.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gymflow_booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID classSessionId;

    @Column(nullable = false)
    private UUID gymflowUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column
    private String stripeSessionId;

    @Column
    private String stripePaymentIntentId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant confirmedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (status == null) status = BookingStatus.PENDING;
    }
}
