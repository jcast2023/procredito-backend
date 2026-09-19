package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.*;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.repository.UsuarioRepository;
import com.procredito.backend.security.JwtService;
import com.procredito.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JavaMailSender mailSender;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El username ya está en uso");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .rol(request.getRol())
                .activo(true)
                .build();


        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .build();
    }

    @Override
    public void solicitarReset(ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElse(null);

        if (usuario != null) {
            String token = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            usuario.setResetToken(token);
            usuario.setResetTokenExpiracion(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);

            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(usuario.getEmail());
            mensaje.setSubject("Restablecimiento de contraseña - ProCrédito");
            mensaje.setText("Hola " + usuario.getNombreCompleto() + ",\n\n"
                    + "Has solicitado restablecer tu contraseña.\n"
                    + "Tu código de verificación es: " + token + "\n\n"
                    + "Este código expira en 15 minutos.\n"
                    + "Si no solicitaste este cambio, ignora este correo.");
            mailSender.send(mensaje);
        }
    }



    @Override
    public void confirmarReset(ResetPasswordConfirmRequest request) {
        Usuario usuario = usuarioRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (usuario.getResetTokenExpiracion() == null
                || usuario.getResetTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado. Solicite uno nuevo.");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiracion(null);
        usuarioRepository.save(usuario);
    }
}