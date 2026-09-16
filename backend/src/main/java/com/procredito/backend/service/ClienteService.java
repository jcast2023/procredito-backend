package com.procredito.backend.service;

import com.procredito.backend.dto.ClienteRequest;
import com.procredito.backend.dto.ClienteResponse;
import java.util.List;

public interface ClienteService {
    ClienteResponse crear(ClienteRequest request);
    ClienteResponse obtenerPorId(Long id);
    List<ClienteResponse> listarTodos();
    ClienteResponse actualizar(Long id, ClienteRequest request);
    void eliminar(Long id);
}
