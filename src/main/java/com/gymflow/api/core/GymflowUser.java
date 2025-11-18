package com.gymflow.api.core;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GymflowUser {
    private UUID id;
    private String username;
    private String email;
    private String phone;
    private Role role;
}
