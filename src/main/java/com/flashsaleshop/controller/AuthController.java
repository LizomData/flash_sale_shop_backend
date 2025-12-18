package com.flashsaleshop.controller;

import com.flashsaleshop.dto.AuthResponse;
import com.flashsaleshop.dto.LoginRequest;
import com.flashsaleshop.dto.RegisterRequest;
import com.flashsaleshop.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StoreService storeService;

    public AuthController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return storeService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return storeService.login(request);
    }
}

