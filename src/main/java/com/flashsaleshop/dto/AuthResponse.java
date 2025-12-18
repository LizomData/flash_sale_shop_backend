package com.flashsaleshop.dto;

import com.flashsaleshop.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserProfile user;
}

