package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.CambioEstadoRequest;
import com.procredito.backend.dto.SolicitudRequest;
import com.procredito.backend.dto.SolicitudResponse;
import com.procredito.backend.entity.Cliente;
import com.procredito.backend.entity.SolicitudCredito;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.EstadoSolicitud;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.exception.BusinessException;
import com.procredito.backend.repository.ClienteRepository;
import com.procredito.backend.repository.SolicitudCreditoRepository;
import com.procredito.backend.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para SolicitudCreditoServiceImpl.
 * Usa Mockito para simular las dependencias (repositorio, SecurityUtils).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitudCreditoServiceImpl - Tests unitarios")
class SolicitudCreditoServiceImplTest {

    @Mock
    private SolicitudCreditoRepository solicitudRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private SolicitudCreditoServiceImpl solicitudService;

    // ============================================================
    // Datos de prueba comunes
    // ============================================================
    private Usuario analista;
    private Usuario admin;
    private Cliente clienteDelAnalista;
    private Cliente clienteDeOtroAnalista;

    @BeforeEach
    void setUp() {
        // Analista dueño de un cliente
        analista = Usuario.builder()
                .id(3L)
                .username("analista1")
                .nombreCompleto("Ana Torres")
                .rol(Rol.ANALISTA)
                .activo(true)
                .build();

        // Admin
        admin = Usuario.builder()
                .id(1L)
                .username("admin")
                .nombreCompleto("Administrador")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();

        // Cliente del analista
        clienteDelAnalista = Cliente.builder()
                .id(1L)
                .documento("12345678")
                .nombres("Juan Perez")
                .analista(analista)
                .build();

        // Cliente de otro analista
        Usuario otroAnalista = Usuario.builder()
                .id(4L)
                .username("analista2")
                .rol(Rol.ANALISTA)
                .build();

        clienteDeOtroAnalista = Cliente.builder()
                .id(2L)
                .documento("87654321")
                .nombres("Maria Lopez")
                .analista(otroAnalista)
                .build();
    }

    // ============================================================
    // TESTS DE CÁLCULO DE CUOTA (el corazón del negocio)
    // ============================================================

    @Test
    @DisplayName("Cuota: 5000 al 18% a 12 meses = 458.40")
    void calcularCuota_5000al18porciento12meses_debeSer458punto40() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDelAnalista));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> {
                    SolicitudCredito s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(1L);
        request.setMontoSolicitado(new BigDecimal("5000"));
        request.setTasaInteres(new BigDecimal("18"));
        request.setPlazoMeses(12);

        // When
        SolicitudResponse response = solicitudService.crear(request);

        // Then
        assertThat(response.getCuotaMensual())
                .isEqualByComparingTo(new BigDecimal("458.40"));
    }

    @Test
    @DisplayName("Cuota: 1000 al 24% a 6 meses = 178.53")
    void calcularCuota_1000al24porciento6meses_debeSer178punto53() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDelAnalista));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> {
                    SolicitudCredito s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(1L);
        request.setMontoSolicitado(new BigDecimal("1000"));
        request.setTasaInteres(new BigDecimal("24"));
        request.setPlazoMeses(6);

        // When
        SolicitudResponse response = solicitudService.crear(request);

        // Then
        // Cálculo: i = 24/100/12 = 0.02; (1.02)^6 = 1.1261624...
        // Cuota = 1000 * (0.02 * 1.1261624) / (0.1261624) = 178.53
        assertThat(response.getCuotaMensual())
                .isEqualByComparingTo(new BigDecimal("178.53"));
    }

    @Test
    @DisplayName("Cuota: tasa 0 → cuota = monto / plazo")
    void calcularCuota_tasaCero_debeSerMontoEntrePlazo() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDelAnalista));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> {
                    SolicitudCredito s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(1L);
        request.setMontoSolicitado(new BigDecimal("1200"));
        request.setTasaInteres(new BigDecimal("0"));
        request.setPlazoMeses(12);

        // When
        SolicitudResponse response = solicitudService.crear(request);

        // Then
        assertThat(response.getCuotaMensual())
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    // ============================================================
    // TESTS DE REGLAS DE NEGOCIO (segregación de funciones)
    // ============================================================

    @Test
    @DisplayName("Crear: ADMIN no puede crear solicitudes")
    void crear_conAdmin_lanzaExcepcion() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);

        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(1L);
        request.setMontoSolicitado(new BigDecimal("5000"));
        request.setTasaInteres(new BigDecimal("18"));
        request.setPlazoMeses(12);

        // When/Then
        assertThatThrownBy(() -> solicitudService.crear(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El administrador no puede crear solicitudes");

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear: ANALISTA con cliente suyo → crea solicitud")
    void crear_conAnalistaYClienteSuyo_creaSolicitud() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDelAnalista));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> {
                    SolicitudCredito s = inv.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(1L);
        request.setMontoSolicitado(new BigDecimal("5000"));
        request.setTasaInteres(new BigDecimal("18"));
        request.setPlazoMeses(12);

        // When
        SolicitudResponse response = solicitudService.crear(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(response.getClienteId()).isEqualTo(1L);

        verify(solicitudRepository, times(1)).save(any(SolicitudCredito.class));
    }

    @Test
    @DisplayName("Crear: ANALISTA con cliente ajeno → lanza excepción")
    void crear_conAnalistaYClienteAjeno_lanzaExcepcion() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(clienteRepository.findByIdAndAnalistaId(2L, 3L))
                .thenReturn(Optional.empty()); // El cliente no pertenece al analista

        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(2L);
        request.setMontoSolicitado(new BigDecimal("5000"));
        request.setTasaInteres(new BigDecimal("18"));
        request.setPlazoMeses(12);

        // When/Then
        assertThatThrownBy(() -> solicitudService.crear(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cliente no encontrado o no tiene permiso");

        verify(solicitudRepository, never()).save(any());
    }

    // ============================================================
    // TESTS DE TRANSICIONES DE ESTADO
    // ============================================================

    @Test
    @DisplayName("Cambiar estado: PENDIENTE → APROBADO")
    void cambiarEstado_dePendienteAAprobado_actualizaEstado() {
        // Given
        SolicitudCredito solicitud = SolicitudCredito.builder()
                .id(1L)
                .cliente(clienteDelAnalista)
                .montoSolicitado(new BigDecimal("5000"))
                .tasaInteres(new BigDecimal("18"))
                .plazoMeses(12)
                .cuotaMensual(new BigDecimal("458.40"))
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado(EstadoSolicitud.APROBADO);

        // When
        SolicitudResponse response = solicitudService.cambiarEstado(1L, request);

        // Then
        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.APROBADO);
        assertThat(response.getFechaActualizacion()).isNotNull();
    }

    @Test
    @DisplayName("Cambiar estado: PENDIENTE → RECHAZADO")
    void cambiarEstado_dePendienteARechazado_actualizaEstado() {
        // Given
        SolicitudCredito solicitud = SolicitudCredito.builder()
                .id(1L)
                .cliente(clienteDelAnalista)
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado(EstadoSolicitud.RECHAZADO);

        // When
        SolicitudResponse response = solicitudService.cambiarEstado(1L, request);

        // Then
        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADO);
    }

    @Test
    @DisplayName("Cambiar estado: APROBADO → DESEMBOLSADO")
    void cambiarEstado_deAprobadoADesembolsado_actualizaEstado() {
        // Given
        SolicitudCredito solicitud = SolicitudCredito.builder()
                .id(1L)
                .cliente(clienteDelAnalista)
                .estado(EstadoSolicitud.APROBADO)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(SolicitudCredito.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado(EstadoSolicitud.DESEMBOLSADO);

        // When
        SolicitudResponse response = solicitudService.cambiarEstado(1L, request);

        // Then
        assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.DESEMBOLSADO);
    }

    @Test
    @DisplayName("Cambiar estado: RECHAZADO → PENDIENTE (inválido)")
    void cambiarEstado_deRechazadoAPendiente_lanzaExcepcion() {
        // Given
        SolicitudCredito solicitud = SolicitudCredito.builder()
                .id(1L)
                .cliente(clienteDelAnalista)
                .estado(EstadoSolicitud.RECHAZADO)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado(EstadoSolicitud.PENDIENTE);

        // When/Then
        assertThatThrownBy(() -> solicitudService.cambiarEstado(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transición de estado no permitida");

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar estado: DESEMBOLSADO → PENDIENTE (estado terminal)")
    void cambiarEstado_deDesembolsadoAPendiente_lanzaExcepcion() {
        // Given
        SolicitudCredito solicitud = SolicitudCredito.builder()
                .id(1L)
                .cliente(clienteDelAnalista)
                .estado(EstadoSolicitud.DESEMBOLSADO)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado(EstadoSolicitud.PENDIENTE);

        // When/Then
        assertThatThrownBy(() -> solicitudService.cambiarEstado(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }

    @Test
    @DisplayName("Cambiar estado: PENDIENTE → DESEMBOLSADO (inválido, salta APROBADO)")
    void cambiarEstado_dePendienteADesembolsado_lanzaExcepcion() {
        // Given
        SolicitudCredito solicitud = SolicitudCredito.builder()
                .id(1L)
                .cliente(clienteDelAnalista)
                .estado(EstadoSolicitud.PENDIENTE)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        CambioEstadoRequest request = new CambioEstadoRequest();
        request.setNuevoEstado(EstadoSolicitud.DESEMBOLSADO);

        // When/Then
        assertThatThrownBy(() -> solicitudService.cambiarEstado(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transición de estado no permitida");
    }

    // ============================================================
    // TESTS DE FILTRADO POR ROL
    // ============================================================

    @Test
    @DisplayName("Listar: ADMIN ve todas las solicitudes")
    void listarTodas_conAdmin_devuelveTodas() {
        // Given
        SolicitudCredito s1 = SolicitudCredito.builder().id(1L).cliente(clienteDelAnalista).build();
        SolicitudCredito s2 = SolicitudCredito.builder().id(2L).cliente(clienteDeOtroAnalista).build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);
        when(solicitudRepository.findAll()).thenReturn(List.of(s1, s2));

        // When
        List<SolicitudResponse> resultado = solicitudService.listarTodas();

        // Then
        assertThat(resultado).hasSize(2);
        verify(solicitudRepository, times(1)).findAll();
        verify(solicitudRepository, never()).findByClienteAnalistaId(any());
    }

    @Test
    @DisplayName("Listar: ANALISTA ve solo las suyas")
    void listarTodas_conAnalista_devuelveSoloSuyas() {
        // Given
        SolicitudCredito s1 = SolicitudCredito.builder().id(1L).cliente(clienteDelAnalista).build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista);
        when(solicitudRepository.findByClienteAnalistaId(3L)).thenReturn(List.of(s1));

        // When
        List<SolicitudResponse> resultado = solicitudService.listarTodas();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getClienteId()).isEqualTo(1L);
        verify(solicitudRepository, times(1)).findByClienteAnalistaId(3L);
        verify(solicitudRepository, never()).findAll();
    }
}