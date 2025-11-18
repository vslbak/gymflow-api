package com.gymflow.api.repository;

import com.gymflow.api.repository.entity.GymflowClassEntity;
import com.gymflow.api.repository.entity.GymflowUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GymflowClassRepository extends JpaRepository<GymflowClassEntity, UUID> {
    Optional<GymflowClassEntity> findByNameEquals(String name);
}
