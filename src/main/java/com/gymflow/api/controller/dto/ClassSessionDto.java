package com.gymflow.api.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ClassSessionDto(UUID id, GymflowClassDto gymflowClass, int spotsLeft, LocalDate date) {}
