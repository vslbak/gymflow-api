package com.gymflow.api.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Booking {
    private UUID id;
    private UUID gymflowUserId;
    private UUID classSessionId;
    private BookingStatus status;
    private String stripeSessionId;
    private String stripePaymentIntentId;
    private Instant createdAt;
    private Instant confirmedAt;
}
