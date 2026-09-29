package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.AuthResponse;
import com.procredito.backend.dto.LoginRequest;
import com.procredito.backend.dto.RegisterRequest;
import com.procredito.backend.dto.ResetPasswordConfirmRequest;
import com.procredito.backend.dto.ResetPasswordRequest;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.repository.UsuarioRepository;
import com.procredito.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthServiceImpl.
 * Verifica login, register y flujo de recuperación de contraseña.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl - Tests unitarios")
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthServiceImpl authService;

    // ============================================================
    // Datos de prueba
    // ============================================================
    private Usuario usuarioAdmin;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        usuarioAdmin = Usuario.builder()
                .id(1L)
                .username("admin")
                .email("admin@procredito.com")
                .password("$2a$10$encodedPassword")
                .nombreCompleto("Administrador del Sistema")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();

        userDetails = new User(
                "admin",
                "$2a$10$encodedPassword",
                true, true, true, true,
                Collections.emptyList()
        );
    }

    // ============================================================
    // TESTS DE LOGIN
    // ============================================================

    @Test
    @DisplayName("Login: credenciales válidas → retorna token JWT")
    void login_conCredencialesValidas_retornaToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioAdmin));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt.token.fake");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.fake");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getNombreCompleto()).isEqualTo("Administrador del Sistema");
        assertThat(response.getRol()).isEqualTo(Rol.ADMIN);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(userDetails);
    }

    @Test
    @DisplayName("Login: usuario no encontrado tras autenticación → lanza excepción")
    void login_conUsuarioInexistente_lanzaExcepcion() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("noexiste");
        request.setPassword("whatever");

        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(jwtService, never()).generateToken(any());
    }

    // ============================================================
    // TESTS DE REGISTER
    // ============================================================

    @Test
    @DisplayName("Register: usuario nuevo → crea y retorna token")
    void register_conUsuarioNuevo_creaYRetornaToken() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevo");
        request.setPassword("password123");
        request.setEmail("nuevo@procredito.com");
        request.setNombreCompleto("Nuevo Usuario");
        request.setRol(Rol.ANALISTA);

        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedNewPassword");
        when(userDetailsService.loadUserByUsername("nuevo")).thenReturn(
                new User("nuevo", "$2a$10$hashedNewPassword", Collections.emptyList())
        );
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("new.jwt.token");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new.jwt.token");
        assertThat(response.getUsername()).isEqualTo("nuevo");
        assertThat(response.getRol()).isEqualTo(Rol.ANALISTA);

        // Verificar que se encriptó el password
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Register: password se encripta con BCrypt antes de guardar")
    void register_encriptaPasswordAntesDeGuardar() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("nuevo");
        request.setPassword("passwordPlano");
        request.setEmail("nuevo@procredito.com");
        request.setNombreCompleto("Nuevo");
        request.setRol(Rol.ANALISTA);

        when(usuarioRepository.existsByUsername("nuevo")).thenReturn(false);
        when(passwordEncoder.encode("passwordPlano")).thenReturn("$2a$10$HASHED");
        when(userDetailsService.loadUserByUsername("nuevo")).thenReturn(
                new User("nuevo", "$2a$10$HASHED", Collections.emptyList())
        );
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token");

        // When
        authService.register(request);

        // Then
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertThat(guardado.getPassword()).isEqualTo("$2a$10$HASHED");
        assertThat(guardado.getPassword()).isNotEqualTo("passwordPlano"); // No en texto plano
        assertThat(guardado.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Register: username duplicado → lanza excepción")
    void register_conUsernameDuplicado_lanzaExcepcion() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("admin");
        request.setPassword("password");
        request.setEmail("otro@procredito.com");
        request.setNombreCompleto("Otro");
        request.setRol(Rol.ANALISTA);

        when(usuarioRepository.existsByUsername("admin")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El username ya está en uso");

        verify(usuarioRepository, never()).save(any());
    }

    // ============================================================
    // TESTS DE SOLICITAR RESET
    // ============================================================

    @Test
    @DisplayName("Solicitar reset: email existe → genera token y envía email")
    void solicitarReset_conEmailExistente_enviaEmail() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("admin@procredito.com");

        when(usuarioRepository.findByEmail("admin@procredito.com"))
                .thenReturn(Optional.of(usuarioAdmin));

        // When
        authService.solicitarReset(request);

        // Then
        verify(usuarioRepository, times(1)).save(usuarioAdmin);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        assertThat(usuarioAdmin.getResetToken()).isNotNull();
        assertThat(usuarioAdmin.getResetToken()).hasSize(32);
        assertThat(usuarioAdmin.getResetTokenExpiracion()).isNotNull();
        assertThat(usuarioAdmin.getResetTokenExpiracion()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Solicitar reset: email NO existe → no hace nada (seguridad)")
    void solicitarReset_conEmailInexistente_noHaceNada() {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("noexiste@procredito.com");

        when(usuarioRepository.findByEmail("noexiste@procredito.com"))
                .thenReturn(Optional.empty());

        // When
        authService.solicitarReset(request);

        // Then
        verify(usuarioRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // ============================================================
    // TESTS DE CONFIRMAR RESET
    // ============================================================

    @Test
    @DisplayName("Confirmar reset: token válido → cambia password")
    void confirmarReset_conTokenValido_cambiaPassword() {
        // Given
        usuarioAdmin.setResetToken("abc123def456");
        usuarioAdmin.setResetTokenExpiracion(LocalDateTime.now().plusMinutes(10));

        ResetPasswordConfirmRequest request = new ResetPasswordConfirmRequest();
        request.setToken("abc123def456");
        request.setNuevaPassword("nuevaPassword123");

        when(usuarioRepository.findByResetToken("abc123def456"))
                .thenReturn(Optional.of(usuarioAdmin));
        when(passwordEncoder.encode("nuevaPassword123")).thenReturn("$2a$10$NUEVO_HASH");

        // When
        authService.confirmarReset(request);

        // Then
        assertThat(usuarioAdmin.getPassword()).isEqualTo("$2a$10$NUEVO_HASH");
        assertThat(usuarioAdmin.getResetToken()).isNull();
        assertThat(usuarioAdmin.getResetTokenExpiracion()).isNull();

        verify(usuarioRepository, times(1)).save(usuarioAdmin);
    }

    @Test
    @DisplayName("Confirmar reset: token expirado → lanza excepción")
    void confirmarReset_conTokenExpirado_lanzaExcepcion() {
        // Given
        usuarioAdmin.setResetToken("abc123def456");
        usuarioAdmin.setResetTokenExpiracion(LocalDateTime.now().minusMinutes(5)); // expirado

        ResetPasswordConfirmRequest request = new ResetPasswordConfirmRequest();
        request.setToken("abc123def456");
        request.setNuevaPassword("nuevaPassword123");

        when(usuarioRepository.findByResetToken("abc123def456"))
                .thenReturn(Optional.of(usuarioAdmin));

        // When/Then
        assertThatThrownBy(() -> authService.confirmarReset(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El token ha expirado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Confirmar reset: token no existe → lanza excepción")
    void confirmarReset_conTokenInexistente_lanzaExcepcion() {
        // Given
        ResetPasswordConfirmRequest request = new ResetPasswordConfirmRequest();
        request.setToken("noexiste");
        request.setNuevaPassword("nuevaPassword123");

        when(usuarioRepository.findByResetToken("noexiste"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.confirmarReset(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido");
    }

    @Test
    @DisplayName("Confirmar reset: expiración nula → lanza excepción")
    void confirmarReset_conExpiracionNula_lanzaExcepcion() {
        // Given
        usuarioAdmin.setResetToken("abc123def456");
        usuarioAdmin.setResetTokenExpiracion(null); // sin expiración

        ResetPasswordConfirmRequest request = new ResetPasswordConfirmRequest();
        request.setToken("abc123def456");
        request.setNuevaPassword("nuevaPassword123");

        when(usuarioRepository.findByResetToken("abc123def456"))
                .thenReturn(Optional.of(usuarioAdmin));

        // When/Then
        assertThatThrownBy(() -> authService.confirmarReset(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El token ha expirado");
    }
}