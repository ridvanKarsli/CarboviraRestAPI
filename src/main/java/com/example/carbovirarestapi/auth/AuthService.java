package com.example.carbovirarestapi.auth;

import com.example.carbovirarestapi.auth.dto.AuthResponse;
import com.example.carbovirarestapi.auth.dto.LoginRequest;
import com.example.carbovirarestapi.auth.dto.RegisterRequest;

/** Kayıt ve giriş akışları. */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
