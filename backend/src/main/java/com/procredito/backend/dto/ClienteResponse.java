package com.procredito.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteResponse {
    private Long id;
    private String documento;
    private String nombres;
    private String telefono;
    private String direccion;
    private String tipoNegocio;
}
