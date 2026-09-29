package com.procredito.backend.dto;

import com.procredito.backend.enums.EstadoSolicitud;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudResponse {
    private Long id;
    private Long clienteId;
    private String clienteNombres;
    private String clienteDocumento;
    private BigDecimal montoSolicitado;
    private BigDecimal tasaInteres;
    private Integer plazoMeses;
    private BigDecimal cuotaMensual;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaActualizacion;
}