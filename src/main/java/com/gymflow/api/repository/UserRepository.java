package com.gymflow.api.repository;

import com.gymflow.api.repository.entity.GymflowUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<GymflowUserEntity, UUID> {
    Optional<GymflowUserEntity> findByUsername(String username);
}
