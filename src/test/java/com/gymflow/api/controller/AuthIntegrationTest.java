package com.gymflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.api.core.Role;
import com.gymflow.api.repository.UserRepository;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.liquibase.enabled", () -> true);
        r.add("gymflow.registration.enabled", () -> true);
    }

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void register_success() throws Exception {
        // when
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     {
                                       "username": "john",
                                       "email": "john@example.com",
                                       "phone": "+306900000000",
                                       "password": "strongpw"
                                     }
                                """))
                .andExpect(status().isCreated());

        // then
        var saved = userRepository.findByUsername("john");

        assertThat(saved).isPresent();
        assertThat(saved.get().getUsername()).isEqualTo("john");
        assertThat(saved.get().getEmail()).isEqualTo("john@example.com");
        assertThat(saved.get().getPhone()).isEqualTo("+306900000000");
        assertThat(saved.get().getRole()).isEqualTo(Role.USER);
        assertThat(saved.get().getPassword()).isNotBlank().startsWith("$2"); // BCrypt
    }

    @Test
    void register_conflict() throws Exception {
        // given
        userRepository.save(
                new GymflowUserEntity(
                        "john",
                        "john@example.com",
                        "306900000000",
                        "$2$whatever",
                        Role.USER
                )
        );

        // when
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     {
                                       "username": "john",
                                       "email": "john@example.com",
                                       "phone": "+306900000000",
                                       "password": "whatever"
                                     }
                                """))
                .andExpect(status().isConflict())
                .andReturn();

        // then
        var found = userRepository.findByUsername("john");
        assertThat(found).isPresent();
    }

    @Test
    void login_success() throws Exception {
        // given
        var encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String hashed = encoder.encode("mypassword");

        userRepository.save(
                new GymflowUserEntity(
                        "john",
                        "john@example.com",
                        "306900000000",
                        hashed,
                        Role.USER
                )
        );

        // when
        String response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     {
                                       "username": "john",
                                       "password": "mypassword"
                                     }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        AuthController.LoginResponse body = objectMapper.readValue(response, AuthController.LoginResponse.class );
        assertThat(body.accessToken()).isNotBlank();
        assertThat(body.expiresIn()).isGreaterThan(0L);
    }

    @Test
    void me_success() throws Exception {
        // given
        userRepository.save(
                new GymflowUserEntity(
                        "john",
                        "john@example.com",
                        "+306900000000",
                        "ignored",
                        Role.USER
                )
        );

        String jwt = jwtService.generateAccessToken("john", Map.of());

        // when
        var response = mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        AuthController.UserResponse userResponse = objectMapper.readValue(response, AuthController.UserResponse.class);

        assertThat(userResponse.username()).isEqualTo("john");
        assertThat(userResponse.email()).isEqualTo("john@example.com");
        assertThat(userResponse.phone()).isEqualTo("+306900000000");
        assertThat(userResponse.role()).isEqualTo(Role.USER);
        assertThat(userResponse.id()).isNotNull();

    }
}
