package com.backend.Backend.service;

import com.backend.Backend.model.User;
import com.backend.Backend.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service

public class AuthService {

    @Autowired
    private  UserRepo repo;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private  JWTService jwt;


    public String signup(User user) {
        if (repo.existsByUsername(user.getUsername()))
            throw new RuntimeException("Username already taken");

        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
        return "Signup successful";
    }

    public String login(User request) {
        User user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username"));

        if (!encoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid password");

        return jwt.generate(user);
    }

    public User me(String username) {
        return repo.findByUsername(username).orElse(null);
    }
}

