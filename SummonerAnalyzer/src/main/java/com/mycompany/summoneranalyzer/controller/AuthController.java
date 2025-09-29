/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.summoneranalyzer.controller;

import com.mycompany.summoneranalyzer.dto.impl.*;
import com.mycompany.summoneranalyzer.entity.impl.User;
import com.mycompany.summoneranalyzer.repository.impl.UserRepository;
import com.mycompany.summoneranalyzer.servis.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder,
                          AuthenticationManager authManager, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.authManager = authManager; this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (users.findByEmail(req.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User u = new User();
        u.setEmail(req.getEmail());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setRole(req.getRole() == null ? com.mycompany.summoneranalyzer.entity.impl.enums.Role.USER : req.getRole());
        users.save(u);

        String token = jwt.generateToken(u.getEmail(),
                java.util.Map.of("role", "ROLE_"+u.getRole().name(), "uid", u.getId()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, u.getId(), u.getEmail(), u.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        // ako ne baci exception, autentifikovan je
        User u = users.findByEmail(req.getEmail());
        String token = jwt.generateToken(u.getEmail(),
                java.util.Map.of("role", "ROLE_"+u.getRole().name(), "uid", u.getId()));
        return ResponseEntity.ok(new AuthResponse(token, u.getId(), u.getEmail(), u.getRole()));
    }
}
