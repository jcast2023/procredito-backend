package com.procredito.backend.controller;

import com.procredito.backend.dto.*;
import com.procredito.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Retorna un JWT válido por 24 horas")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con rol ADMIN o ANALISTA")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/solicitar-reset")
    public ResponseEntity<Map<String, String>> solicitarReset(@Valid @RequestBody ResetPasswordRequest request) {
        authService.solicitarReset(request);
        return ResponseEntity.ok(Map.of("mensaje",
                "Si el correo existe, recibirás un código de verificación."));
    }

    @PostMapping("/confirmar-reset")
    public ResponseEntity<Map<String, String>> confirmarReset(@Valid @RequestBody ResetPasswordConfirmRequest request) {
        authService.confirmarReset(request);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }

}