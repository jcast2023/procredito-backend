package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.UsuarioRequest;
import com.procredito.backend.dto.UsuarioResponse;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.exception.BusinessException;
import com.procredito.backend.repository.UsuarioRepository;
import com.procredito.backend.security.SecurityUtils;
import com.procredito.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    // LISTAR ANALISTAS
    // ============================================================
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

    // ============================================================
    // LISTAR TODOS
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede listar usuarios");
        }
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede ver usuarios");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado con ID: " + id));
        return mapToResponse(usuario);
    }

    // ============================================================
    // CREAR
    // ============================================================
    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede crear usuarios");
        }

        // Validar username único
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El username '" + request.getUsername() + "' ya está en uso.");
        }

        // Validar email único
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("El email '" + request.getEmail() + "' ya está registrado.");
        }

        // Validar password obligatorio al crear
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria al crear un usuario.");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .rol(request.getRol())
                .activo(true)
                .build();

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede editar usuarios");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado con ID: " + id));

        // Validar email único (excepto el mismo usuario)
        usuarioRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("El email '" + request.getEmail() + "' ya está registrado por otro usuario.");
                });

        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());

        // Solo cambiar password si viene con valor
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ============================================================
    // CAMBIAR ACTIVO (soft delete)
    // ============================================================
    @Override
    @Transactional
    public UsuarioResponse cambiarActivo(Long id, boolean activo) {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede cambiar el estado de usuarios");
        }

        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();

        // Un admin NO puede desactivarse a sí mismo
        if (usuarioActual.getId().equals(id) && !activo) {
            throw new BusinessException("No puede desactivar su propia cuenta.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(activo);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    // ============================================================
    // MAP TO RESPONSE
    // ============================================================
    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .rol(usuario.getRol().name())
                .activo(usuario.isActivo())
                .build();
    }
}