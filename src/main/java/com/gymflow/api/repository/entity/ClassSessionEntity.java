package com.gymflow.api.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "class_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSessionEntity {

    public ClassSessionEntity(UUID classId, int spotsLeft, LocalDate date) {
        this.gymflowClass = new GymflowClassEntity(classId);
        this.spotsLeft = spotsLeft;
        this.date = date;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "gymflow_class_id")
    private GymflowClassEntity gymflowClass;

    @Column(nullable = false)
    private int spotsLeft;

    @Column
    private LocalDate date;
}