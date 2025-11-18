package com.gymflow.api.service;

import com.gymflow.api.core.Booking;
import com.gymflow.api.core.BookingStatus;
import com.gymflow.api.repository.BookingRepository;
import com.gymflow.api.repository.entity.BookingEntity;
import com.gymflow.api.repository.mapper.BookingEntityMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingEntityMapper bookingEntityMapper;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(bookingEntityMapper::from)
                .toList();
    }

    public UUID createBooking(UUID classSessionId, UUID userId) {
        BookingEntity bookingEntity = new BookingEntity();
        bookingEntity.setClassSessionId(classSessionId);
        bookingEntity.setGymflowUserId(userId);
        return bookingRepository.save(bookingEntity).getId();
    }

    public void addStripeSessionId(UUID bookingId, String stripeSessionId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        entity.setStripeSessionId(stripeSessionId);
        bookingRepository.save(entity);
    }

    public void failBooking(UUID bookingId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (entity.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only bookings with status PENDING can be failed");
        }
        entity.setStatus(BookingStatus.FAILED);
        bookingRepository.save(entity);
    }

    public void confirmBooking(UUID bookingId, String stripePaymentIntentId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (entity.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only bookings with status PENDING can be confirmed");
        }
        entity.setStatus(BookingStatus.CONFIRMED);
        entity.setConfirmedAt(Instant.now());
        entity.setStripePaymentIntentId(stripePaymentIntentId);
        bookingRepository.save(entity);
    }

    public Booking getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .map(bookingEntityMapper::from)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    public List<Booking> getUserBookings(UUID userId) {
        return bookingRepository.findByGymflowUserId(userId)
                .stream()
                .map(bookingEntityMapper::from)
                .toList();
    }

    public void cancelBooking(UUID bookingId, UUID userId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!entity.getGymflowUserId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to the user");
        }
        if (entity.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only bookings with status CONFIRMED can be cancelled");
        }
        entity.setStatus(BookingStatus.CANCELLED);
        // TODO: Consider automated refund process here - NOT IMPLEMENTED YET
        bookingRepository.save(entity);
    }

    public void deleteBooking(UUID bookingId) {
        BookingEntity entity = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        bookingRepository.delete(entity);
    }
}
