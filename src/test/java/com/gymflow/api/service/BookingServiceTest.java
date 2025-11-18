package com.gymflow.api.service;

import com.gymflow.api.core.Booking;
import com.gymflow.api.core.BookingStatus;
import com.gymflow.api.repository.BookingRepository;
import com.gymflow.api.repository.entity.BookingEntity;
import com.gymflow.api.repository.mapper.BookingEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        BookingEntityMapper mapper = Mappers.getMapper(BookingEntityMapper.class);
        bookingService = new BookingService(repository, mapper);
    }

    @Test
    void getZeroBookings() {
        // given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // when
        var bookings = bookingService.getAllBookings();

        // then
        assertThat(bookings).isEmpty();
    }

    @Test
    void getBookings() {
        //given
        UUID bookingId = UUID.randomUUID();
        BookingEntity bookingEntity = new BookingEntity(bookingId, UUID.randomUUID(), UUID.randomUUID(), BookingStatus.PENDING, null, null, Instant.now(), Instant.now());
        when(repository.findAll()).thenReturn(List.of(bookingEntity));

        // when
        var bookings = bookingService.getAllBookings();

        // then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.getFirst()).extracting(Booking::getId, Booking::getStatus).containsExactly(bookingId, BookingStatus.PENDING);
    }

    @Test
    void createNewBooking() {
        // given
        UUID classSessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        when(repository.save(argThat(e -> e.getGymflowUserId().equals(userId) && e.getClassSessionId().equals(classSessionId)))).thenReturn(new BookingEntity(bookingId, classSessionId, userId, BookingStatus.PENDING, null, null, Instant.now(), null));

        // when
        UUID resultId = bookingService.createBooking(classSessionId, userId);

        // then
        assertThat(resultId).isEqualTo(bookingId);
    }

    @Test
    void addStripeSessionIdToBooking() {
        // given
        UUID bookingId = UUID.randomUUID();
        String stripeSessionId = "sess_12345";
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        bookingService.addStripeSessionId(bookingId, stripeSessionId);

        // then
        verify(bookingEntity).setStripeSessionId(stripeSessionId);
        verify(repository).save(bookingEntity);
    }

    @Test
    void addStripeSessionIdToNonexistentBookingThrows() {
        // given
        UUID bookingId = UUID.randomUUID();
        String stripeSessionId = "sess_12345";
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        // when
        Exception ex = assertThrows(IllegalArgumentException.class, () -> bookingService.addStripeSessionId(bookingId, stripeSessionId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Booking not found");
    }

    @Test
    void setBookingToFailed() {
        // given
        UUID bookingId = UUID.randomUUID();
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(bookingEntity.getStatus()).thenReturn(BookingStatus.PENDING);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        bookingService.failBooking(bookingId);

        // then
        verify(bookingEntity).setStatus(BookingStatus.FAILED);
        verify(repository).save(bookingEntity);
    }

    @Test
    void setBookingToFailedButNotExists() {
        // given
        UUID bookingId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        // when
        Exception ex = assertThrows(IllegalArgumentException.class, () -> bookingService.failBooking(bookingId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Booking not found");
    }

    @Test
    void setBookingToFailedButInvalidStatus() {
        // given
        UUID bookingId = UUID.randomUUID();
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(bookingEntity.getStatus()).thenReturn(BookingStatus.CONFIRMED);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        Exception ex = assertThrows(IllegalStateException.class, () -> bookingService.failBooking(bookingId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Only bookings with status PENDING can be failed");
    }

    @Test
    void confirmBookingSuccessfully() {
        // given
        UUID bookingId = UUID.randomUUID();
        String paymentIntentId = "pi_12345";
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(bookingEntity.getStatus()).thenReturn(BookingStatus.PENDING);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        bookingService.confirmBooking(bookingId, paymentIntentId);

        // then
        verify(bookingEntity).setStatus(BookingStatus.CONFIRMED);
        verify(bookingEntity).setStripePaymentIntentId(paymentIntentId);
        verify(bookingEntity).setConfirmedAt(any(Instant.class));
        verify(repository).save(bookingEntity);
    }

    @Test
    void confirmBookingButNotExists() {
        // given
        UUID bookingId = UUID.randomUUID();
        String paymentIntentId = "pi_12345";
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        // when
        Exception ex = assertThrows(IllegalArgumentException.class, () -> bookingService.confirmBooking(bookingId, paymentIntentId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Booking not found");
    }

    @Test
    void confirmBookingButInvalidStatus() {
        // given
        UUID bookingId = UUID.randomUUID();
        String paymentIntentId = "pi_12345";
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(bookingEntity.getStatus()).thenReturn(BookingStatus.FAILED);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        Exception ex = assertThrows(IllegalStateException.class, () -> bookingService.confirmBooking(bookingId, paymentIntentId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Only bookings with status PENDING can be confirmed");
    }

    @Test
    void deleteBookingSuccessfully() {
        // given
        UUID bookingId = UUID.randomUUID();
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        bookingService.deleteBooking(bookingId);

        // then
        verify(repository).delete(bookingEntity);
    }

    @Test
    void deleteBookingButNotExists() {
        // given
        UUID bookingId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        // when
        Exception ex = assertThrows(IllegalArgumentException.class, () -> bookingService.deleteBooking(bookingId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Booking not found");
    }

    @Test
    void cancelBooking() {
        // given
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(bookingEntity.getGymflowUserId()).thenReturn(userId);
        when(bookingEntity.getStatus()).thenReturn(BookingStatus.CONFIRMED);
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        bookingService.cancelBooking(bookingId, userId);

        // then
        verify(bookingEntity).setStatus(BookingStatus.CANCELLED);
        verify(repository).save(bookingEntity);
    }

    @Test
    void cancelBookingButNotExists() {
        // given
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        // when
        Exception ex = assertThrows(IllegalArgumentException.class, () -> bookingService.cancelBooking(bookingId, userId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Booking not found");
    }

    @Test
    void cancelBookingButUserMismatch() {
        // given
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BookingEntity bookingEntity = mock(BookingEntity.class);
        when(bookingEntity.getGymflowUserId()).thenReturn(UUID.randomUUID());
        when(repository.findById(bookingId)).thenReturn(Optional.of(bookingEntity));

        // when
        Exception ex = assertThrows(IllegalArgumentException.class, () -> bookingService.cancelBooking(bookingId, userId));

        // then
        assertThat(ex.getMessage()).isEqualTo("Booking does not belong to the user");
    }
}