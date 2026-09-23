package com.procredito.backend.service;

import com.procredito.backend.dto.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    void solicitarReset(ResetPasswordRequest request);
    void confirmarReset(ResetPasswordConfirmRequest request);

}
