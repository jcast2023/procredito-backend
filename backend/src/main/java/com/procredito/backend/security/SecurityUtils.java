package com.procredito.backend.security;

import com.procredito.backend.entity.Usuario;
import com.procredito.backend.exception.BusinessException;
import com.procredito.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para obtener el usuario autenticado en cualquier parte del código.
 * Se usa en los servicios para filtrar datos según el rol del usuario.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve el usuario autenticado de la petición actual.
     * @throws BusinessException si no hay usuario autenticado.
     */
    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("No hay usuario autenticado");
        }

        String username = authentication.getName();

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado: " + username));
    }

    /**
     * Devuelve true si el usuario autenticado es ADMIN.
     */
    public boolean esAdmin() {
        return getUsuarioAutenticado().getRol().name().equals("ADMIN");
    }
}