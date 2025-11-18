package com.gymflow.api.controller;

import com.gymflow.api.controller.dto.ClassSessionDto;
import com.gymflow.api.controller.mapper.GymflowClassDtoMapper;
import com.gymflow.api.core.ClassSession;
import com.gymflow.api.core.GymflowUser;
import com.gymflow.api.service.*;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final StripePaymentService stripePaymentService;
    private final UserService userService;
    private final BookingService bookingService;
    private final GymflowClassService gymflowClassService;
    private final GymflowClassDtoMapper gymflowClassDtoMapper;

    @PostMapping("/create-session")
    public ResponseEntity<CreateSessionResponse> createSession(@RequestBody CreateSessionDto req) throws StripeException {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        GymflowUser user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")); // should not happen

        UUID bookingId = bookingService.createBooking(req.classSession(), user.getId());
        Session session = stripePaymentService.createCheckoutSession(bookingId, req.className(), req.amount());
        bookingService.addStripeSessionId(bookingId, session.getId());

        return ResponseEntity.ok(new CreateSessionResponse(session.getUrl()));
    }

    @PostMapping("/payment/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {
        Optional<StripePaymentService.PaymentResponse> response = stripePaymentService.handleWebhookEvent(payload, sigHeader);
        if (response.isEmpty()) {
            return ResponseEntity.noContent().build(); // irrelevant event
        }
        switch (response.get().status()) {
            case SUCCESS -> {
                bookingService.confirmBooking(response.get().bookingId(), response.get().paymentIntentId());
                UUID sessionId = bookingService.getBookingById(response.get().bookingId()).getClassSessionId();
                // TODO add pessimistic lock
                gymflowClassService.reserveClassSessionSpot(sessionId);
            }
            case FAILURE -> bookingService.failBooking(response.get().bookingId());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<List<BookingDto>> getUserBookings(Authentication authentication) {
        GymflowUser user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<BookingDto> bookings = bookingService.getUserBookings(user.getId())
                .stream()
                .map(b -> {
                    ClassSessionDto cs = mapClassSession(b.getClassSessionId());
                    return new BookingDto(b.getId(), cs, b.getStatus().name(), b.getCreatedAt(), b.getConfirmedAt());
                })
                .toList();

        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/user/bookings/{booking-id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable("booking-id") UUID bookingId, Authentication authentication) {
        GymflowUser user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        bookingService.cancelBooking(bookingId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{booking-id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("booking-id") UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        List<BookingDto> bookings = bookingService.getAllBookings()
                .stream()
                .map(b -> {
                    ClassSessionDto cs = mapClassSession(b.getClassSessionId());
                    return new BookingDto(b.getId(), cs, b.getStatus().name(), b.getCreatedAt(), b.getConfirmedAt());
                })
                .toList();

        return ResponseEntity.ok(bookings);
    }

    private ClassSessionDto mapClassSession(UUID sessionId) {
        ClassSession session = gymflowClassService.getClassSessionById(sessionId);
        return gymflowClassDtoMapper.fromCore(session);
    }
    public record CreateSessionDto(UUID classSession, String className, BigDecimal amount) { }
    public record CreateSessionResponse(String url) { }
    public record BookingDto(UUID id, ClassSessionDto classSession, String status, Instant createdAt, Instant confirmedAt) {}

}
