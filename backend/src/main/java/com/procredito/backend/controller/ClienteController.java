package com.procredito.backend.controller;

import com.procredito.backend.dto.ClienteRequest;
import com.procredito.backend.dto.ClienteResponse;
import com.procredito.backend.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "CRUD de microempresarios")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Crear cliente", description = "Registra un nuevo microempresario")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Obtiene todos los clientes registrados")
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Busca un cliente por su ID")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente por su ID")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/existe-dni")
    @Operation(summary = "Verificar si un DNI ya existe", description = "Retorna true si el DNI ya está registrado")
    public ResponseEntity<Boolean> existeDni(@RequestParam String dni,
                                             @RequestParam(required = false) Long excluirId) {
        return ResponseEntity.ok(clienteService
                .existeDni(dni, excluirId));
    }

}