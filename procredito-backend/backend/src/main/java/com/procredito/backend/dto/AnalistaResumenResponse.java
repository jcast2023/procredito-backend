package com.procredito.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalistaResumenResponse {

    private Long analistaId;
    private String analistaNombre;
    private String analistaUsername;

    private Long totalClientes;
    private Long totalSolicitudes;
    private BigDecimal montoTotalSolicitado;
    private BigDecimal montoTotalDesembolsado;
    private Long solicitudesPendientes;
    private Long solicitudesAprobadas;
    private Long solicitudesRechazadas;
    private Long solicitudesDesembolsadas;
}