package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.UsuarioResponse;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.exception.BusinessException;
import com.procredito.backend.repository.UsuarioRepository;
import com.procredito.backend.security.SecurityUtils;
import com.procredito.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarAnalistas() {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede listar analistas");
        }

        return usuarioRepository.findByRol(Rol.ANALISTA)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().name())
                .build();
    }
}