package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.ClienteRequest;
import com.procredito.backend.dto.ClienteResponse;
import com.procredito.backend.entity.Cliente;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.EstadoSolicitud;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.exception.BusinessException;
import com.procredito.backend.repository.ClienteRepository;
import com.procredito.backend.repository.SolicitudCreditoRepository;
import com.procredito.backend.repository.UsuarioRepository;
import com.procredito.backend.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ClienteServiceImpl.
 * Verifica reglas de negocio de roles, asignación de analistas
 * y validaciones.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl - Tests unitarios")
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SolicitudCreditoRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    // ============================================================
    // Datos de prueba
    // ============================================================
    private Usuario admin;
    private Usuario analista1;
    private Usuario analista2;
    private Cliente clienteDeAnalista1;

    @BeforeEach
    void setUp() {
        admin = Usuario.builder()
                .id(1L)
                .username("admin")
                .nombreCompleto("Administrador")
                .rol(Rol.ADMIN)
                .activo(true)
                .build();

        analista1 = Usuario.builder()
                .id(3L)
                .username("analista1")
                .nombreCompleto("Ana Torres")
                .rol(Rol.ANALISTA)
                .activo(true)
                .build();

        analista2 = Usuario.builder()
                .id(4L)
                .username("analista2")
                .nombreCompleto("Carlos Ramírez")
                .rol(Rol.ANALISTA)
                .activo(true)
                .build();

        clienteDeAnalista1 = Cliente.builder()
                .id(1L)
                .documento("12345678")
                .nombres("Juan Perez")
                .telefono("999111222")
                .direccion("Av. Los Olivos 123")
                .tipoNegocio("Bodega")
                .analista(analista1)
                .build();
    }

    // ============================================================
    // TESTS DE CREACIÓN
    // ============================================================

    @Test
    @DisplayName("Crear: ADMIN sin analistaId → lanza excepción")
    void crear_conAdminSinAnalistaId_lanzaExcepcion() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);
        when(clienteRepository.existsByDocumento("99999999")).thenReturn(false);

        ClienteRequest request = new ClienteRequest();
        request.setDocumento("99999999");
        request.setNombres("Test User");
        request.setAnalistaId(null); // ← admin no envió analista

        // When/Then
        assertThatThrownBy(() -> clienteService.crear(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El administrador debe asignar un analista");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear: ADMIN con analistaId válido → crea cliente asignado")
    void crear_conAdminYAnalistaValido_creaClienteAsignado() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);
        when(clienteRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(analista1));
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(inv -> {
                    Cliente c = inv.getArgument(0);
                    c.setId(10L);
                    return c;
                });

        ClienteRequest request = new ClienteRequest();
        request.setDocumento("99999999");
        request.setNombres("Nuevo Cliente");
        request.setTelefono("999888777");
        request.setDireccion("Av. Test 123");
        request.setTipoNegocio("Farmacia");
        request.setAnalistaId(3L);

        // When
        ClienteResponse response = clienteService.crear(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAnalistaId()).isEqualTo(3L);
        assertThat(response.getAnalistaNombre()).isEqualTo("Ana Torres");

        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear: ADMIN con usuario que NO es ANALISTA → lanza excepción")
    void crear_conAdminYUsuarioNoAnalista_lanzaExcepcion() {
        // Given
        Usuario usuarioAdmin2 = Usuario.builder()
                .id(99L)
                .username("otroAdmin")
                .rol(Rol.ADMIN)
                .build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);
        when(clienteRepository.existsByDocumento("99999999")).thenReturn(false);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.of(usuarioAdmin2));

        ClienteRequest request = new ClienteRequest();
        request.setDocumento("99999999");
        request.setNombres("Test");
        request.setAnalistaId(99L);

        // When/Then
        assertThatThrownBy(() -> clienteService.crear(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("debe tener rol ANALISTA");
    }

    @Test
    @DisplayName("Crear: ANALISTA se auto-asigna")
    void crear_conAnalista_seAutoAsigna() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista1);
        when(clienteRepository.existsByDocumento("99999999")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(inv -> {
                    Cliente c = inv.getArgument(0);
                    c.setId(10L);
                    return c;
                });

        ClienteRequest request = new ClienteRequest();
        request.setDocumento("99999999");
        request.setNombres("Nuevo Cliente");
        request.setTelefono("999888777");
        request.setDireccion("Av. Test 123");
        request.setTipoNegocio("Farmacia");
        // No envía analistaId

        // When
        ClienteResponse response = clienteService.crear(request);

        // Then
        assertThat(response.getAnalistaId()).isEqualTo(3L);
        assertThat(response.getAnalistaNombre()).isEqualTo("Ana Torres");
    }

    @Test
    @DisplayName("Crear: documento duplicado → lanza excepción")
    void crear_conDocumentoDuplicado_lanzaExcepcion() {
        // Given
        when(clienteRepository.existsByDocumento("12345678")).thenReturn(true);

        ClienteRequest request = new ClienteRequest();
        request.setDocumento("12345678");
        request.setNombres("Test");

        // When/Then
        assertThatThrownBy(() -> clienteService.crear(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe un cliente con ese documento");

        verify(clienteRepository, never()).save(any());
    }

    // ============================================================
    // TESTS DE LISTADO (filtrado por rol)
    // ============================================================

    @Test
    @DisplayName("Listar: ADMIN ve todos los clientes")
    void listarTodos_conAdmin_devuelveTodos() {
        // Given
        Cliente c1 = Cliente.builder().id(1L).nombres("A").analista(analista1).build();
        Cliente c2 = Cliente.builder().id(2L).nombres("B").analista(analista2).build();

        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);
        when(clienteRepository.findAll()).thenReturn(List.of(c1, c2));

        // When
        List<ClienteResponse> resultado = clienteService.listarTodos();

        // Then
        assertThat(resultado).hasSize(2);
        verify(clienteRepository, times(1)).findAll();
        verify(clienteRepository, never()).findByAnalistaId(anyLong());
    }

    @Test
    @DisplayName("Listar: ANALISTA ve solo los suyos")
    void listarTodos_conAnalista_devuelveSoloSuyos() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista1);
        when(clienteRepository.findByAnalistaId(3L)).thenReturn(List.of(clienteDeAnalista1));

        // When
        List<ClienteResponse> resultado = clienteService.listarTodos();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getAnalistaId()).isEqualTo(3L);
        verify(clienteRepository, times(1)).findByAnalistaId(3L);
        verify(clienteRepository, never()).findAll();
    }

    // ============================================================
    // TESTS DE OBTENER POR ID (validación de propiedad)
    // ============================================================

    @Test
    @DisplayName("Obtener: ADMIN obtiene cualquier cliente")
    void obtenerPorId_conAdmin_obtieneCualquiera() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(admin);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteDeAnalista1));

        // When
        ClienteResponse response = clienteService.obtenerPorId(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Obtener: ANALISTA obtiene solo los suyos")
    void obtenerPorId_conAnalistaSuyo_obtieneCliente() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista1);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDeAnalista1));

        // When
        ClienteResponse response = clienteService.obtenerPorId(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Obtener: ANALISTA con cliente ajeno → lanza excepción")
    void obtenerPorId_conAnalistaYClienteAjeno_lanzaExcepcion() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista2);
        when(clienteRepository.findByIdAndAnalistaId(1L, 4L))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> clienteService.obtenerPorId(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no tiene permiso");
    }

    // ============================================================
    // TESTS DE ELIMINACIÓN
    // ============================================================

    @Test
    @DisplayName("Eliminar: cliente con solicitudes activas → lanza excepción")
    void eliminar_conSolicitudesActivas_lanzaExcepcion() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista1);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDeAnalista1));
        when(solicitudRepository.existsByClienteIdAndEstadoIn(
                eq(1L),
                argThat(estados -> estados.contains(EstadoSolicitud.APROBADO)
                        && estados.contains(EstadoSolicitud.DESEMBOLSADO))
        )).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> clienteService.eliminar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede eliminar el cliente");

        verify(clienteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar: cliente sin solicitudes activas → elimina")
    void eliminar_sinSolicitudesActivas_elimina() {
        // Given
        when(securityUtils.getUsuarioAutenticado()).thenReturn(analista1);
        when(clienteRepository.findByIdAndAnalistaId(1L, 3L))
                .thenReturn(Optional.of(clienteDeAnalista1));
        when(solicitudRepository.existsByClienteIdAndEstadoIn(anyLong(), any()))
                .thenReturn(false);

        // When
        clienteService.eliminar(1L);

        // Then
        verify(clienteRepository, times(1)).delete(clienteDeAnalista1);
    }

    // ============================================================
    // TESTS DE REASIGNACIÓN (solo admin)
    // ============================================================

    @Test
    @DisplayName("Reasignar: ADMIN reasigna cliente a otro analista")
    void reasignarAnalista_conAdmin_reasigna() {
        // Given
        when(securityUtils.esAdmin()).thenReturn(true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteDeAnalista1));
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(analista2));
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // When
        ClienteResponse response = clienteService.reasignarAnalista(1L, 4L);

        // Then
        assertThat(response.getAnalistaId()).isEqualTo(4L);
        assertThat(response.getAnalistaNombre()).isEqualTo("Carlos Ramírez");
    }

    @Test
    @DisplayName("Reasignar: ANALISTA no puede reasignar → lanza excepción")
    void reasignarAnalista_conAnalista_lanzaExcepcion() {
        // Given
        when(securityUtils.esAdmin()).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> clienteService.reasignarAnalista(1L, 4L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo el administrador");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reasignar: nuevo analista no existe → lanza excepción")
    void reasignarAnalista_conAnalistaInexistente_lanzaExcepcion() {
        // Given
        when(securityUtils.esAdmin()).thenReturn(true);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteDeAnalista1));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> clienteService.reasignarAnalista(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Analista no encontrado");
    }
}