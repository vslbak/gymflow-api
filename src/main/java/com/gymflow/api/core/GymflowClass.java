package com.gymflow.api.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GymflowClass {
    private UUID id;
    private String name;
    private String instructor;
    private LocalTime classTime;
    private Set<DayOfWeek> days;
    private Duration duration;
    private int totalSpots;
    private String imageUrl;
    private String category;
    private String description;
    private String level;
    private String location;
    private BigDecimal price;
    private List<String> whatToBring;
}
