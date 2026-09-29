package com.procredito.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Long totalClientes;
    private Long totalSolicitudes;
    private BigDecimal montoTotalSolicitado;
    private BigDecimal montoTotalDesembolsado;
    private Map<String, Long> solicitudesPorEstado;
    private BigDecimal promedioMontoSolicitado;
    private BigDecimal promedioCuotaMensual;
}
