package com.procredito.backend.repository;

import com.procredito.backend.entity.SolicitudCredito;
import com.procredito.backend.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCredito, Long> {
    List<SolicitudCredito> findByEstado(EstadoSolicitud estado);
    List<SolicitudCredito> findByClienteId(Long clienteId);
    boolean existsByClienteIdAndEstadoIn(Long clienteId, List<EstadoSolicitud> estados);

}