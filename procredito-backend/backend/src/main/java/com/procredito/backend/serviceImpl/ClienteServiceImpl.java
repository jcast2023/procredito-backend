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
import com.procredito.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;

    // ============================================================
    // CREAR
    // ============================================================
    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        // Validar documento único
        if (clienteRepository.existsByDocumento(request.getDocumento())) {
            throw new BusinessException("Ya existe un cliente con ese documento");
        }

        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        Usuario analistaAsignado;

        if (usuarioActual.getRol() == Rol.ADMIN) {
            // El admin DEBE elegir un analista
            if (request.getAnalistaId() == null) {
                throw new BusinessException(
                        "El administrador debe asignar un analista al crear el cliente.");
            }
            analistaAsignado = usuarioRepository.findById(request.getAnalistaId())
                    .orElseThrow(() -> new BusinessException("Analista no encontrado con ID: " + request.getAnalistaId()));
            if (analistaAsignado.getRol() != Rol.ANALISTA) {
                throw new BusinessException("El usuario asignado debe tener rol ANALISTA.");
            }
        } else {
            // El analista se auto-asigna
            analistaAsignado = usuarioActual;
        }

        Cliente cliente = Cliente.builder()
                .documento(request.getDocumento())
                .nombres(request.getNombres())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .tipoNegocio(request.getTipoNegocio())
                .analista(analistaAsignado)
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        return mapToResponse(guardado);
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        Cliente cliente;

        if (usuarioActual.getRol() == Rol.ADMIN) {
            cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Cliente no encontrado"));
        } else {
            cliente = clienteRepository.findByIdAndAnalistaId(id, usuarioActual.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Cliente no encontrado o no tiene permiso para verlo."));
        }

        return mapToResponse(cliente);
    }

    // ============================================================
    // LISTAR
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        List<Cliente> clientes;

        if (usuarioActual.getRol() == Rol.ADMIN) {
            clientes = clienteRepository.findAll();
        } else {
            clientes = clienteRepository.findByAnalistaId(usuarioActual.getId());
        }

        return clientes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        Cliente cliente;

        if (usuarioActual.getRol() == Rol.ADMIN) {
            cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Cliente no encontrado"));
        } else {
            cliente = clienteRepository.findByIdAndAnalistaId(id, usuarioActual.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Cliente no encontrado o no tiene permiso para editarlo."));
        }

        // Actualizar campos permitidos (el documento NO se cambia)
        cliente.setNombres(request.getNombres());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setTipoNegocio(request.getTipoNegocio());

        // Solo el admin puede reasignar el analista
        if (usuarioActual.getRol() == Rol.ADMIN && request.getAnalistaId() != null) {
            Usuario nuevoAnalista = usuarioRepository.findById(request.getAnalistaId())
                    .orElseThrow(() -> new BusinessException("Analista no encontrado con ID: " + request.getAnalistaId()));
            if (nuevoAnalista.getRol() != Rol.ANALISTA) {
                throw new BusinessException("El usuario asignado debe tener rol ANALISTA.");
            }
            cliente.setAnalista(nuevoAnalista);
        }

        return mapToResponse(clienteRepository.save(cliente));
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        Cliente cliente;

        if (usuarioActual.getRol() == Rol.ADMIN) {
            cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("Cliente no encontrado"));
        } else {
            cliente = clienteRepository.findByIdAndAnalistaId(id, usuarioActual.getId())
                    .orElseThrow(() -> new BusinessException(
                            "Cliente no encontrado o no tiene permiso para eliminarlo."));
        }

        boolean tieneSolicitudesActivas = solicitudRepository
                .existsByClienteIdAndEstadoIn(id,
                        List.of(EstadoSolicitud.APROBADO, EstadoSolicitud.DESEMBOLSADO));

        if (tieneSolicitudesActivas) {
            throw new BusinessException(
                    "No se puede eliminar el cliente porque tiene solicitudes aprobadas o desembolsadas.");
        }

        clienteRepository.delete(cliente);
    }

    // ============================================================
    // REASIGNAR ANALISTA (solo admin)
    // ============================================================
    @Override
    @Transactional
    public ClienteResponse reasignarAnalista(Long clienteId, Long nuevoAnalistaId) {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede reasignar clientes.");
        }

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente no encontrado con ID: " + clienteId));

        Usuario nuevoAnalista = usuarioRepository.findById(nuevoAnalistaId)
                .orElseThrow(() -> new BusinessException("Analista no encontrado con ID: " + nuevoAnalistaId));

        if (nuevoAnalista.getRol() != Rol.ANALISTA) {
            throw new BusinessException("El usuario asignado debe tener rol ANALISTA.");
        }

        cliente.setAnalista(nuevoAnalista);
        return mapToResponse(clienteRepository.save(cliente));
    }

    // ============================================================
    // EXISTE DNI
    // ============================================================
    @Override
    public boolean existeDni(String dni, Long excluirId) {
        if (excluirId != null) {
            return clienteRepository.existsByDocumentoAndIdNot(dni, excluirId);
        }
        return clienteRepository.existsByDocumento(dni);
    }

    // ============================================================
    // MAP TO RESPONSE
    // ============================================================
    private ClienteResponse mapToResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .documento(cliente.getDocumento())
                .nombres(cliente.getNombres())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .tipoNegocio(cliente.getTipoNegocio())
                .analistaId(cliente.getAnalista() != null ? cliente.getAnalista().getId() : null)
                .analistaNombre(cliente.getAnalista() != null ? cliente.getAnalista().getNombreCompleto() : null)
                .build();
    }
}