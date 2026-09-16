package com.procredito.backend.service;

import com.procredito.backend.dto.CambioEstadoRequest;
import com.procredito.backend.dto.SolicitudRequest;
import com.procredito.backend.dto.SolicitudResponse;
import com.procredito.backend.enums.EstadoSolicitud;
import java.util.List;

public interface SolicitudCreditoService {
    SolicitudResponse crear(SolicitudRequest request);
    SolicitudResponse obtenerPorId(Long id);
    List<SolicitudResponse> listarTodas();
    List<SolicitudResponse> listarPorEstado(EstadoSolicitud estado);
    SolicitudResponse cambiarEstado(Long id, CambioEstadoRequest request);
}