package com.procredito.backend.controller;

import com.procredito.backend.dto.CambioEstadoRequest;
import com.procredito.backend.dto.SolicitudRequest;
import com.procredito.backend.dto.SolicitudResponse;
import com.procredito.backend.enums.EstadoSolicitud;
import com.procredito.backend.service.SolicitudCreditoService;
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
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Crédito", description = "Gestión de solicitudes de microcréditos")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudCreditoController {

    private final SolicitudCreditoService solicitudService;

    @PostMapping
    @Operation(summary = "Crear solicitud", description = "Registra una nueva solicitud con cálculo automático de cuota")
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas las solicitudes", description = "Obtiene todas las solicitudes registradas")
    public ResponseEntity<List<SolicitudResponse>> listarTodas() {
        return ResponseEntity.ok(solicitudService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener solicitud por ID", description = "Busca una solicitud por su ID")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar por estado", description = "Filtra solicitudes por estado (PENDIENTE, APROBADO, RECHAZADO, DESEMBOLSADO)")
    public ResponseEntity<List<SolicitudResponse>> listarPorEstado(@PathVariable EstadoSolicitud estado) {
        return ResponseEntity.ok(solicitudService.listarPorEstado(estado));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado", description = "Cambia el estado de una solicitud existente")
    public ResponseEntity<SolicitudResponse> cambiarEstado(@PathVariable Long id,
                                                           @Valid @RequestBody CambioEstadoRequest request) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, request));
    }
}