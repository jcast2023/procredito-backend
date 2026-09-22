package com.procredito.backend.service;

import com.procredito.backend.dto.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    /**
     * Lista solo los usuarios con rol ANALISTA.
     * Solo el ADMIN puede ejecutar esta acción.
     */
    List<UsuarioResponse> listarAnalistas();
}