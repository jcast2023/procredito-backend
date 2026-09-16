package com.procredito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequest {

    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 20)
    private String documento;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 150)
    private String nombres;

    @Size(max = 20)
    private String telefono;

    @Size(max = 200)
    private String direccion;

    @Size(max = 100)
    private String tipoNegocio;
}