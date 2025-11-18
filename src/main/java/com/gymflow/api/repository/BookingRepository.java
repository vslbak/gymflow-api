package com.gymflow.api.repository;

import com.gymflow.api.repository.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
    List<BookingEntity> findByGymflowUserId(UUID gymflowUserId);
}
