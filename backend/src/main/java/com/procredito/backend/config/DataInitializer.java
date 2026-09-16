package com.procredito.backend.config;

import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsuarios() {
        return args -> {
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                usuarioRepository.save(Usuario.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .nombreCompleto("Administrador del Sistema")
                        .rol(Rol.ADMIN)
                        .activo(true)
                        .build());
                log.info("✅ Usuario ADMIN creado -> admin / admin123");
            } else {
                log.info("ℹ️ Usuario admin ya existe, no se crea de nuevo");
            }

            if (usuarioRepository.findByUsername("analista").isEmpty()) {
                usuarioRepository.save(Usuario.builder()
                        .username("analista")
                        .password(passwordEncoder.encode("analista123"))
                        .nombreCompleto("Analista de Créditos")
                        .rol(Rol.ANALISTA)
                        .activo(true)
                        .build());
                log.info("✅ Usuario ANALISTA creado -> analista / analista123");
            } else {
                log.info("ℹ️ Usuario analista ya existe, no se crea de nuevo");
            }
        };
    }
}