package com.procredito.backend.controller;

import com.procredito.backend.dto.DashboardResponse;
import com.procredito.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Resumen general de la cartera de créditos")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    @Operation(summary = "Obtener resumen general",
            description = "Devuelve totales y promedios de clientes y solicitudes de crédito")
    public ResponseEntity<DashboardResponse> obtenerResumen() {
        return ResponseEntity.ok(dashboardService.obtenerResumen());
    }
}