package com.gymflow.api.service;

import com.gymflow.api.core.GymflowUser;
import com.gymflow.api.core.Role;
import com.gymflow.api.repository.UserRepository;
import com.gymflow.api.repository.entity.GymflowUserEntity;
import com.gymflow.api.repository.mapper.GymflowUserEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        GymflowUserEntityMapper mapper = Mappers.getMapper(GymflowUserEntityMapper.class);
        userService = new UserService(userRepository, passwordEncoder, mapper);
    }

    @Test
    void findByPrincipalExists() {
        // given
        UUID userId = UUID.randomUUID();
        String username = "johnDoe";
        String email = "john.doe@email.com";
        String phone = "1234567890";
        String password = "password";
        Role role = Role.USER;

        when(userRepository.findByUsername("johnDoe"))
                .thenReturn(Optional.of(new GymflowUserEntity(userId, username, email, phone, password, role)));

        // when
        Optional<GymflowUser> result = userService.findByUsername("johnDoe");

        // then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals(username, result.get().getUsername());
        assertEquals(email, result.get().getEmail());
        assertEquals(phone, result.get().getPhone());
        assertEquals(role, result.get().getRole());
    }


    @Test
    void findByPrincipalNotExist() {
        // given
        when(userRepository.findByUsername("johnDoe"))
                .thenReturn(Optional.empty());

        // when
        Optional<GymflowUser> result = userService.findByUsername("johnDoe");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void createForExistingUsernameFails() {        // given
        // given
        when(userRepository.findByUsername("johnDoe"))
                .thenReturn(Optional.of(new GymflowUserEntity()));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.create("johnDoe", "john.doe@email.com", "1234567890", "password", Role.USER));

        // then
        assertThat(exception.getMessage().contains("User with username johnDoe already exists"));
    }

    @Test
    void createNewUser() {
        // given
        UUID createdEntityId = UUID.randomUUID();
        when(userRepository.findByUsername("johnDoe")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(new GymflowUserEntity(createdEntityId, "johnDoe", "john.doe@email.com", "1234567890", new BCryptPasswordEncoder().encode("password"), Role.USER));

        // when
        GymflowUser created = userService.create("johnDoe", "john.doe@email.com", "1234567890", "password", Role.USER);

        // then
        verify(userRepository).findByUsername("johnDoe");
        verify(userRepository).save(argThat(user -> user.getUsername().equals("johnDoe") && new BCryptPasswordEncoder().matches("password", user.getPassword())));

        assertThat(created.getId()).isEqualTo(createdEntityId);
        assertThat(created.getUsername()).isEqualTo("johnDoe");
        assertThat(created.getEmail()).isEqualTo("john.doe@email.com");
        assertThat(created.getPhone()).isEqualTo("1234567890");
        assertThat(created.getRole()).isEqualTo(Role.USER);
    }
}