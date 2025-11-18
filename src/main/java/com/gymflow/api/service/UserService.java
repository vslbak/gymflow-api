package com.gymflow.api.service;

import com.gymflow.api.core.Role;
import com.gymflow.api.repository.UserRepository;
import com.gymflow.api.core.GymflowUser;
import com.gymflow.api.repository.entity.GymflowUserEntity;
import com.gymflow.api.repository.mapper.GymflowUserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GymflowUserEntityMapper mapper;

    public Optional<GymflowUser> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(mapper::from);
    }

    public GymflowUser create(String username, String email, String phone, String password, Role role) {
        userRepository.findByUsername(username)
                .ifPresent(user -> {
                    throw new IllegalArgumentException("User with username " + username + " already exists");
                });
        String encodedPassword = passwordEncoder.encode(password);
        GymflowUserEntity entity = new GymflowUserEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPhone(phone);
        entity.setPassword(encodedPassword);
        entity.setRole(role);
        return mapper.from(userRepository.save(entity));
    }

}
