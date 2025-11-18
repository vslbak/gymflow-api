package com.gymflow.api.controller.dto;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record GymflowClassDto(UUID id, String name, String description, String instructor, LocalTime classTime, Set<DayOfWeek> days, Duration duration, int totalSpots, String imageUrl, String category, String level, String location, BigDecimal price, List<String> whatToBring) {}
