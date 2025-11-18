package com.gymflow.api.controller;

import com.gymflow.api.core.Role;
import com.gymflow.api.service.JwtService;
import com.gymflow.api.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String username = authentication.getName(); // comes from JWT subject
        var user = userService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getRole()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegistrationRequest request) {

        if (userService.findByUsername(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }

        userService.create(
                request.username().trim(),
                request.email().trim(),
                request.phone().trim(),
                request.password(),
                Role.USER);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        System.out.println("Auth: " + auth);

        var accessToken = jwtService.generateAccessToken(req.username(), Map.of());

        LoginResponse response = new LoginResponse(
                accessToken,
                jwtService.getAccessExpirationMs()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(Authentication auth) {
        String username = auth.getName();

        String newAccess = jwtService.generateAccessToken(username, Map.of());
        LoginResponse response = new LoginResponse(
                newAccess,
                jwtService.getAccessExpirationMs()
        );
        return ResponseEntity.ok(response);
    }

    public record RegistrationRequest(@NotBlank(message = "Username required")
                                      @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
                                      @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Only letters, digits, '.', '-', '_' allowed")
                                      String username,
                                      @Email @NotBlank String email,
                                      @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
                                      @NotBlank(message = "Phone is required") String phone,
                                      @NotBlank @Size(min = 6) String password) {}

    public record LoginRequest(@NotBlank String username,
                               @NotBlank String password) {}

    public record LoginResponse(String accessToken,
                                long expiresIn) {}

    public record UserResponse(UUID id,
                               String username,
                               String phone,
                               String email,
                               Role role) {}

}
