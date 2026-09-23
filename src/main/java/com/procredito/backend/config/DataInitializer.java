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
            crearUsuarioSiNoExiste(
                    "admin",
                    "admin123",
                    "Administrador del Sistema",
                    Rol.ADMIN
            );

            crearUsuarioSiNoExiste(
                    "analista1",
                    "analista123",
                    "Ana Torres (Analista)",
                    Rol.ANALISTA
            );

            crearUsuarioSiNoExiste(
                    "analista2",
                    "analista123",
                    "Carlos Ramírez (Analista)",
                    Rol.ANALISTA
            );
        };
    }

    private void crearUsuarioSiNoExiste(String username, String password, String nombre, Rol rol) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .nombreCompleto(nombre)
                    .rol(rol)
                    .activo(true)
                    .build());
            log.info("✅ Usuario {} creado -> {} / {}", rol, username, password);
        } else {
            log.info("ℹ️ Usuario {} ya existe, no se crea de nuevo", username);
        }
    }
}