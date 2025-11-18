package com.gymflow.api.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "gymflow_class")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymflowClassEntity {

    public GymflowClassEntity(UUID id) { // Constructor for referencing existing entities
        this.id = id;
    }

    public GymflowClassEntity(String name, String instructor, Duration duration, int totalSpots, String imageUrl, String category, String description, String level, String location, LocalTime classTime, Set<DayOfWeek> days, BigDecimal price, List<String> whatToBring) {
        this.name = name;
        this.instructor = instructor;
        this.duration = duration;
        this.totalSpots = totalSpots;
        this.imageUrl = imageUrl;
        this.category = category;
        this.description = description;
        this.level = level;
        this.location = location;
        this.classTime = classTime;
        this.days = days;
        this.price = price;
        this.whatToBring = whatToBring;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String instructor;

    @Column(columnDefinition = "INTERVAL")
    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    private Duration duration;

    @Column(nullable = false)
    private int totalSpots;

    private String imageUrl;
    private String category;
    private String description;
    private String level;
    private String location;

    @Column(nullable = false)
    private LocalTime classTime;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Set<DayOfWeek> days = new HashSet<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> whatToBring = new ArrayList<>();
}
