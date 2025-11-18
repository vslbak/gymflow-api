package com.gymflow.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.api.controller.dto.ClassSessionDto;
import com.gymflow.api.controller.dto.GymflowClassDto;
import com.gymflow.api.core.Role;
import com.gymflow.api.repository.GymflowClassRepository;
import com.gymflow.api.repository.ClassSessionRepository;
import com.gymflow.api.repository.UserRepository;
import com.gymflow.api.repository.entity.ClassSessionEntity;
import com.gymflow.api.repository.entity.GymflowClassEntity;
import com.gymflow.api.repository.entity.GymflowUserEntity;
import com.gymflow.api.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GymflowClassControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private GymflowClassRepository classRepo;
    @Autowired
    private ClassSessionRepository sessionRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.liquibase.enabled", () -> true);
    }

    @BeforeEach
    void cleanDb() {
        sessionRepo.deleteAll();
        classRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void getClasses_success() throws Exception {
        // given
        classRepo.save(new GymflowClassEntity(
                "Yoga",
                "Alice",
                Duration.ofMinutes(60),
                15,
                "imageurl",
                "Wellness",
                "Relaxing class",
                "Beginner",
                "Studio A",
                LocalTime.NOON,
                Set.of(DayOfWeek.MONDAY),
                new BigDecimal("15.00"),
                List.of("Yoga mat", "Water bottle")
        ));

        // when
        String response = mvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        List<GymflowClassDto> arr = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(arr).hasSize(1);
        assertThat(arr.getFirst().name()).isEqualTo("Yoga");
        assertThat(arr.getFirst().instructor()).isEqualTo("Alice");
        assertThat(arr.getFirst().duration()).isEqualTo(Duration.ofMinutes(60));
        assertThat(arr.getFirst().totalSpots()).isEqualTo(15);
        assertThat(arr.getFirst().imageUrl()).isEqualTo("imageurl");
        assertThat(arr.getFirst().category()).isEqualTo("Wellness");
        assertThat(arr.getFirst().description()).isEqualTo("Relaxing class");
        assertThat(arr.getFirst().level()).isEqualTo("Beginner");
        assertThat(arr.getFirst().location()).isEqualTo("Studio A");
        assertThat(arr.getFirst().classTime()).isEqualTo(LocalTime.NOON);
        assertThat(arr.getFirst().days()).isEqualTo(Set.of(DayOfWeek.MONDAY));
        assertThat(arr.getFirst().price()).isEqualTo(new BigDecimal("15.00"));
        assertThat(arr.getFirst().whatToBring()).containsExactly("Yoga mat", "Water bottle");
    }

    @Test
    void getSessions_success() throws Exception {
        // given
        UUID clsId = classRepo.save(new GymflowClassEntity(
                "Yoga",
                "Alice",
                Duration.ofMinutes(60),
                15,
                "imageurl",
                "Wellness",
                "Relaxing class",
                "Beginner",
                "Studio A",
                LocalTime.NOON,
                Set.of(DayOfWeek.MONDAY),
                new BigDecimal("15.00"),
                List.of("Yoga mat", "Water bottle")
        )).getId();

        sessionRepo.save(new ClassSessionEntity(clsId, 3, LocalDate.now().plusDays(1))); // filter this one out, it's tomorrow
        sessionRepo.save(new ClassSessionEntity(clsId, 5, LocalDate.now().plusDays(2)));

        // when
        String response = mvc.perform(get("/classes/" + clsId + "/sessions"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        List<ClassSessionDto> sessions = objectMapper.readValue(response, new TypeReference<>() {});
        assertThat(sessions).hasSize(1);
        assertThat(sessions.getFirst().spotsLeft()).isEqualTo(5);
        assertThat(sessions.getFirst().date()).isEqualTo(LocalDate.now().plusDays(2));
        assertThat(sessions.getFirst().gymflowClass().name()).isEqualTo("Yoga");
        assertThat(sessions.getFirst().gymflowClass().instructor()).isEqualTo("Alice");
        assertThat(sessions.getFirst().gymflowClass().duration()).isEqualTo(Duration.ofMinutes(60));
        assertThat(sessions.getFirst().gymflowClass().totalSpots()).isEqualTo(15);
        assertThat(sessions.getFirst().gymflowClass().imageUrl()).isEqualTo("imageurl");
        assertThat(sessions.getFirst().gymflowClass().category()).isEqualTo("Wellness");
        assertThat(sessions.getFirst().gymflowClass().description()).isEqualTo("Relaxing class");
        assertThat(sessions.getFirst().gymflowClass().level()).isEqualTo("Beginner");
        assertThat(sessions.getFirst().gymflowClass().location()).isEqualTo("Studio A");
        assertThat(sessions.getFirst().gymflowClass().classTime()).isEqualTo(LocalTime.NOON);
        assertThat(sessions.getFirst().gymflowClass().days()).isEqualTo(Set.of(DayOfWeek.MONDAY));
        assertThat(sessions.getFirst().gymflowClass().price()).isEqualTo(new BigDecimal("15.00"));
        assertThat(sessions.getFirst().gymflowClass().whatToBring()).containsExactly("Yoga mat", "Water bottle");

    }

    @Test
    void createClass_forbiddenForUser() throws Exception {
        // no JWT → 403
        mvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                                              {
                                "name": "Spin",
                                "instructor": "Bob",
                                "duration": 60,
                                "totalSpots": 10,
                                "category": "Cardio",
                                "description": "Intense ride",
                                "level": "Advanced",
                                "location": "Studio X",
                                "classTime": "18:00",
                                "days": ["MONDAY", "WEDNESDAY", "FRIDAY"],
                                "whatToBring": ["Towel", "Water"]
                                                              }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createClass_successForAdmin() throws Exception {
        // given
        userRepo.save(new GymflowUserEntity(
                "admin",
                "admin@example.com",
                "+300000000000",
                "ignored",
                Role.ADMIN
        ));
        String jwt = jwtService.generateAccessToken("admin", Map.of("role", "ADMIN"));

        // when
        mvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                                              {
                                "name": "Spin",
                                "instructor": "Bob",
                                "duration": 60,
                                "totalSpots": 10,
                                "category": "Cardio",
                                "description": "Intense ride",
                                "level": "Advanced",
                                "location": "Studio X",
                                "classTime": "18:00",
                                "days": ["MONDAY", "WEDNESDAY", "FRIDAY"],
                                "whatToBring": ["Towel", "Water"]
                                                              }
                                """))
                .andExpect(status().isCreated());

        // then
        var all = classRepo.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getName()).isEqualTo("Spin");
    }

    @Test
    void updateClass_forbiddenForUser() throws Exception {
        // given
        UUID clsId = classRepo.save(new GymflowClassEntity(
                "Yoga",
                "Alice",
                Duration.ofMinutes(60),
                15,
                "imageurl",
                "Wellness",
                "Relaxing class",
                "Beginner",
                "Studio A",
                LocalTime.NOON,
                Set.of(DayOfWeek.MONDAY),
                new BigDecimal("15.00"),
                List.of("Yoga mat", "Water bottle")
        )).getId();

        // when
        mvc.perform(put("/classes/" + clsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Boxing Updated",
                                  "totalSpots": 20
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateClass_successForAdmin() throws Exception {
        // given
        UUID clsId = classRepo.save(new GymflowClassEntity(
                "Yoga",
                "Alice",
                Duration.ofMinutes(60),
                15,
                "imageurl",
                "Wellness",
                "Relaxing class",
                "Beginner",
                "Studio A",
                LocalTime.NOON,
                Set.of(DayOfWeek.MONDAY),
                new BigDecimal("15.00"),
                List.of("Yoga mat", "Water bottle")
        )).getId();

        userRepo.save(new GymflowUserEntity(
                "admin",
                "admin@example.com",
                "+300000000",
                "ignored",
                Role.ADMIN
        ));
        String jwt = jwtService.generateAccessToken("admin", Map.of("role", "ADMIN"));

        // when
        mvc.perform(put("/classes/" + clsId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Boxing Extreme",
                                  "instructor": "Nikos",
                                  "totalSpots": 30
                                }
                                """))
                .andExpect(status().isOk());

        // then
        var updated = classRepo.findById(clsId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Boxing Extreme");
        assertThat(updated.getTotalSpots()).isEqualTo(30);
    }
}
