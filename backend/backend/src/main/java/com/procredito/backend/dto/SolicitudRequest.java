package com.procredito.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SolicitudRequest {

    @NotNull(message = "El id del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "100.00", message = "El monto mínimo es 100")
    private BigDecimal montoSolicitado;

    @NotNull(message = "La tasa de interés es obligatoria")
    @DecimalMin(value = "0.1")
    @DecimalMax(value = "20.0")
    private BigDecimal tasaInteres;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1)
    @Max(value = 60)
    private Integer plazoMeses;
}