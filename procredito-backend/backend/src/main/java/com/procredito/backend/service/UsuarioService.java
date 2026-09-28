package com.procredito.backend.service;

import com.procredito.backend.dto.UsuarioRequest;
import com.procredito.backend.dto.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    /**
     * Lista solo los usuarios con rol ANALISTA.
     * Solo el ADMIN puede ejecutar esta acción.
     */
    List<UsuarioResponse> listarAnalistas();

    /**
     * Lista TODOS los usuarios (ADMIN y ANALISTA).
     * Solo el ADMIN puede ejecutar esta acción.
     */
    List<UsuarioResponse> listarTodos();

    /**
     * Obtiene un usuario por ID.
     * Solo el ADMIN puede ejecutar esta acción.
     */
    UsuarioResponse obtenerPorId(Long id);

    /**
     * Crea un nuevo usuario.
     * Solo el ADMIN puede ejecutar esta acción.
     */
    UsuarioResponse crear(UsuarioRequest request);

    /**
     * Actualiza un usuario existente.
     * Solo el ADMIN puede ejecutar esta acción.
     * Si el password viene vacío, no se cambia.
     */
    UsuarioResponse actualizar(Long id, UsuarioRequest request);

    /**
     * Activa o desactiva un usuario (soft delete).
     * Solo el ADMIN puede ejecutar esta acción.
     * Un admin NO puede desactivarse a sí mismo.
     */
    UsuarioResponse cambiarActivo(Long id, boolean activo);
}