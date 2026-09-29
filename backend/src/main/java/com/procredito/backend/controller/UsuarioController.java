package com.procredito.backend.controller;

import com.procredito.backend.dto.UsuarioRequest;
import com.procredito.backend.dto.UsuarioResponse;
import com.procredito.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (solo ADMIN)")
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

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Solo ADMIN.")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Solo ADMIN.")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Solo ADMIN. Crea un usuario ADMIN o ANALISTA.")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Solo ADMIN. Si password viene vacío, no se cambia.")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    @Operation(
            summary = "Activar o desactivar usuario",
            description = "Solo ADMIN. Soft delete: no se borra, solo se marca como inactivo."
    )
    public ResponseEntity<UsuarioResponse> cambiarActivo(@PathVariable Long id,
                                                         @RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarActivo(id, activo));
    }
}