package com.gymflow.api.repository;

import com.gymflow.api.repository.entity.ClassSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClassSessionRepository extends JpaRepository<ClassSessionEntity, UUID> {
    List<ClassSessionEntity> findAllByGymflowClassId(UUID id);
    boolean existsByGymflowClassIdAndDate(UUID classId, LocalDate date);
}

