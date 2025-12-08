package com.backend.Backend.controller;

import com.backend.Backend.model.User;
import com.backend.Backend.repo.UserRepo;
import com.backend.Backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")

public class AuthController {

    @Autowired
    private  AuthService auth;

    @Autowired
    private  UserRepo repo;

    @Autowired
    private PasswordEncoder encoder;

    // ============================
    // SIGNUP
    // ============================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        return ResponseEntity.ok(auth.signup(user));
    }

    // ============================
    // LOGIN (NO JWT)
    // ============================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) User request) {

        if (request == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }

        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password required");
        }

        User user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return ResponseEntity.ok(
                Map.of(
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "status", "LOGIN_SUCCESS"
                )
        );
    }

    // ============================
    // ME (NO TOKEN)
    // ============================
    @GetMapping("/me/{username}")
    public ResponseEntity<?> me(@PathVariable String username) {

        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
}

