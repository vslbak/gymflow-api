package com.gymflow.api.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ClassSession {
    private UUID id;
    private GymflowClass gymflowClass;
    private LocalDate date;
    private int spotsLeft;
}
