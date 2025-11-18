package com.gymflow.api.repository.entity;

import com.gymflow.api.core.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "gymflow_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GymflowUserEntity {

    public GymflowUserEntity(String username, String email, String phone, String password, Role role) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
