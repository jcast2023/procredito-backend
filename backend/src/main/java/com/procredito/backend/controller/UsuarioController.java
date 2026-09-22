package com.procredito.backend.controller;

import com.procredito.backend.dto.UsuarioResponse;
import com.procredito.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/analistas")
    @Operation(
            summary = "Listar analistas",
            description = "Solo ADMIN. Devuelve la lista de analistas disponibles para asignar clientes."
    )
    public ResponseEntity<List<UsuarioResponse>> listarAnalistas() {
        return ResponseEntity.ok(usuarioService.listarAnalistas());
    }
}