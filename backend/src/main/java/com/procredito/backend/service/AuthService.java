package com.procredito.backend.service;

import com.procredito.backend.dto.AuthResponse;
import com.procredito.backend.dto.LoginRequest;
import com.procredito.backend.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
