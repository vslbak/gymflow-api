package com.gymflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.api.core.*;
import com.gymflow.api.repository.*;
import com.gymflow.api.repository.entity.*;
import com.gymflow.api.service.*;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private GymflowClassRepository classRepo;
    @Autowired
    private ClassSessionRepository sessionRepo;
    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StripePaymentService stripePaymentService;

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
        bookingRepo.deleteAll();
        sessionRepo.deleteAll();
        classRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void createSession_requiresAuth() throws Exception {
        mvc.perform(post("/booking/create-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                               "classSession": "00000000-0000-0000-0000-000000000000",
                               "className": "Spin",
                               "amount": 10.00
                            }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSession_success() throws Exception {
        // user
        var user = userRepo.save(new GymflowUserEntity(
                "john", "john@example.com", "690", "ignored", Role.USER
        ));
        String jwt = jwtService.generateAccessToken("john", Map.of());

        // class + session
        var cls = classRepo.save(new GymflowClassEntity(
                "Spin", "Bob", Duration.ofMinutes(60), 10, null, null, null, null, null, LocalTime.NOON,
                new HashSet<>(), new BigDecimal("10.00"), new ArrayList<>()
        ));
        var session = sessionRepo.save(new ClassSessionEntity(
                cls.getId(), 10, LocalDate.now().plusDays(1)
        ));

        // mock stripe
        Session stripeSession = new Session();
        stripeSession.setId("sess_123");
        stripeSession.setUrl("https://stripe.test/checkout");
        Mockito.when(stripePaymentService.createCheckoutSession(any(), any(), any()))
                .thenReturn(stripeSession);

        // payload
        var json = """
                {
                    "classSession": "%s",
                    "className": "Spin",
                    "amount": 10.00
                }
                """.formatted(session.getId());

        // execute
        var resp = mvc.perform(post("/booking/create-session")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var body = objectMapper.readValue(resp, BookingController.CreateSessionResponse.class);

        assertThat(body.url()).isEqualTo("https://stripe.test/checkout");

        // booking should exist
        assertThat(bookingRepo.findAll()).hasSize(1);
    }

    @Test
    void getUserBookings_success() throws Exception {
        // user
        var user = userRepo.save(new GymflowUserEntity(
                "john", "john@example.com", "690", "ignored", Role.USER
        ));
        String jwt = jwtService.generateAccessToken("john", Map.of());

        // class + session + booking
        var cls = classRepo.save(new GymflowClassEntity(
                "Yoga", "Alice", Duration.ofMinutes(80), 10, null, null, null, null, null, LocalTime.NOON,
                new HashSet<>(), new BigDecimal("10.00"), new ArrayList<>()
        ));
        var sess = sessionRepo.save(new ClassSessionEntity(
                cls.getId(), 5, LocalDate.now().plusDays(3)
        ));
        bookingRepo.save(new BookingEntity(null, sess.getId(), user.getId(), BookingStatus.PENDING, null, null, Instant.now(), Instant.now().plus(Duration.ofMinutes(30))));

        var json = mvc.perform(get("/booking/user/bookings")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<?> arr = objectMapper.readValue(json, List.class);
        assertThat(arr).hasSize(1);
    }

    @Test
    void deleteBooking_forbiddenForUser() throws Exception {
        userRepo.save(new GymflowUserEntity(
                "john", "john@example.com", "690", "ignored", Role.USER
        ));
        String jwt = jwtService.generateAccessToken("john", Map.of());

        mvc.perform(delete("/booking/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBooking_adminSuccess() throws Exception {
        userRepo.save(new GymflowUserEntity(
                "admin", "admin@example.com", "690", "ignored", Role.ADMIN
        ));
        var user = userRepo.save(new GymflowUserEntity(
                "jack", "jack@example.com", "690", "ignored", Role.USER
        ));


        String jwt = jwtService.generateAccessToken("admin", Map.of("role","ADMIN"));
        // class + session + booking
        var cls = classRepo.save(new GymflowClassEntity(
                "Yoga", "Alice", Duration.ofMinutes(80), 10, null, null, null, null, null, LocalTime.NOON,
                new HashSet<>(), new BigDecimal("10.00"), new ArrayList<>()
        ));
        var sess = sessionRepo.save(new ClassSessionEntity(
                cls.getId(), 5, LocalDate.now().plusDays(3)
        ));

        UUID id = bookingRepo.save(new BookingEntity(null, sess.getId(), user.getId(), BookingStatus.PENDING, null, null, Instant.now() ,Instant.now().plusSeconds(60))).getId();

        mvc.perform(delete("/booking/" + id)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        assertThat(bookingRepo.findById(id)).isEmpty();
    }
}
