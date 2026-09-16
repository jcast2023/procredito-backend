package com.procredito.backend.service.impl;

import com.procredito.backend.dto.ClienteRequest;
import com.procredito.backend.dto.ClienteResponse;
import com.procredito.backend.entity.Cliente;
import com.procredito.backend.repository.ClienteRepository;
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

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDocumento(request.getDocumento())) {
            throw new RuntimeException("Ya existe un cliente con ese documento");
        }

        Cliente cliente = Cliente.builder()
                .documento(request.getDocumento())
                .nombres(request.getNombres())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .tipoNegocio(request.getTipoNegocio())
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        return mapToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setNombres(request.getNombres());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setTipoNegocio(request.getTipoNegocio());

        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }

    private ClienteResponse mapToResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .documento(cliente.getDocumento())
                .nombres(cliente.getNombres())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .tipoNegocio(cliente.getTipoNegocio())
                .build();
    }
}
