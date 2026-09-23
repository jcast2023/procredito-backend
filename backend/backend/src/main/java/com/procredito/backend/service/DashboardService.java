package com.procredito.backend.service;

import com.procredito.backend.dto.AnalistaResumenResponse;
import com.procredito.backend.dto.DashboardResponse;

import java.util.List;

public interface DashboardService {

    /**
     * Resumen del dashboard según el rol del usuario autenticado:
     * - ADMIN: totales de todos.
     * - ANALISTA: solo sus totales.
     */
    DashboardResponse obtenerResumen();

    /**
     * Resumen por analista (solo ADMIN).
     * Devuelve una fila por cada analista con sus métricas individuales.
     */
    List<AnalistaResumenResponse> obtenerResumenPorAnalista();
}