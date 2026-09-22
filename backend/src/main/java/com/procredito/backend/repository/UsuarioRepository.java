package com.procredito.backend.repository;

import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String resetToken);
    List<Usuario> findByRol(Rol rol);
}