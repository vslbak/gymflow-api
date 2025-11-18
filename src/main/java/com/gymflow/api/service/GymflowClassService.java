package com.gymflow.api.service;

import com.gymflow.api.core.ClassSession;
import com.gymflow.api.core.GymflowClass;
import com.gymflow.api.repository.ClassSessionRepository;
import com.gymflow.api.repository.GymflowClassRepository;
import com.gymflow.api.repository.entity.ClassSessionEntity;
import com.gymflow.api.repository.entity.GymflowClassEntity;
import com.gymflow.api.repository.mapper.GymflowClassEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GymflowClassService {

    private final GymflowClassRepository classRepository;
    private final ClassSessionRepository sessionRepository;
    private final GymflowClassEntityMapper mapper;

    public ClassSession getClassSessionById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .map(mapper::from)
                .orElseThrow(() -> new IllegalArgumentException("Class session not found"));
    }

    public void reserveClassSessionSpot(UUID sessionId) {
        Optional<ClassSessionEntity> entity = sessionRepository.findById(sessionId);
        if (entity.isEmpty()) {
            throw new IllegalArgumentException("Class session not found");
        }
        entity.get().setSpotsLeft(entity.get().getSpotsLeft() - 1);
        sessionRepository.save(entity.get());
    }


    public List<GymflowClass> getClasses() {
        return classRepository.findAll()
                .stream()
                .map(mapper::from)
                .toList();
    }

    public List<ClassSession> getClassSessions(UUID classId) {
        return sessionRepository.findAllByGymflowClassId(classId)
                .stream()
                .map(mapper::from)
                .filter(s -> s.getDate().isAfter(LocalDate.now().plusDays(1))) // tomorrow onwards
                .toList();
    }


    public void createClass(GymflowClass classData) {
        Optional<GymflowClassEntity> entityOpt = classRepository.findByNameEquals((classData.getName()));
        if (entityOpt.isPresent()) {
            throw new IllegalArgumentException("Class name already exists");
        }

        GymflowClassEntity entity = new GymflowClassEntity();
        mapper.updateEntityFromCore(classData, entity);
        classRepository.save(entity);
    }

    public void updateClass(UUID classId, GymflowClass updatedClass) {
        Optional<GymflowClassEntity> entityOpt = classRepository.findById(classId);
        if (entityOpt.isEmpty()) {
            throw new IllegalArgumentException("Class not found");
        }
        GymflowClassEntity entity = entityOpt.get();
        mapper.updateEntityFromCore(updatedClass, entity);

        classRepository.save(entity);
    }

}
