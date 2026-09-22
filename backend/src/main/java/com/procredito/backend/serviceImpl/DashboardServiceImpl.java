package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.AnalistaResumenResponse;
import com.procredito.backend.dto.DashboardResponse;
import com.procredito.backend.entity.SolicitudCredito;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.EstadoSolicitud;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.exception.BusinessException;
import com.procredito.backend.repository.ClienteRepository;
import com.procredito.backend.repository.SolicitudCreditoRepository;
import com.procredito.backend.repository.UsuarioRepository;
import com.procredito.backend.security.SecurityUtils;
import com.procredito.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClienteRepository clienteRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;

    // ============================================================
    // RESUMEN GENERAL (según rol)
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen() {
        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        boolean esAdmin = usuarioActual.getRol() == Rol.ADMIN;

        long totalClientes = esAdmin
                ? clienteRepository.count()
                : clienteRepository.countByAnalistaId(usuarioActual.getId());

        List<SolicitudCredito> solicitudes = esAdmin
                ? solicitudRepository.findAll()
                : solicitudRepository.findByClienteAnalistaId(usuarioActual.getId());

        long totalSolicitudes = solicitudes.size();

        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            long count = solicitudes.stream()
                    .filter(s -> s.getEstado() == estado)
                    .count();
            porEstado.put(estado.name(), count);
        }

        BigDecimal montoTotalSolicitado = solicitudes.stream()
                .map(SolicitudCredito::getMontoSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoTotalDesembolsado = solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.DESEMBOLSADO)
                .map(SolicitudCredito::getMontoSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioMontoSolicitado = totalSolicitudes == 0
                ? BigDecimal.ZERO
                : montoTotalSolicitado.divide(
                BigDecimal.valueOf(totalSolicitudes), 2, RoundingMode.HALF_UP);

        BigDecimal sumaCuotas = solicitudes.stream()
                .map(SolicitudCredito::getCuotaMensual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioCuotaMensual = totalSolicitudes == 0
                ? BigDecimal.ZERO
                : sumaCuotas.divide(
                BigDecimal.valueOf(totalSolicitudes), 2, RoundingMode.HALF_UP);

        return DashboardResponse.builder()
                .totalClientes(totalClientes)
                .totalSolicitudes(totalSolicitudes)
                .montoTotalSolicitado(montoTotalSolicitado)
                .montoTotalDesembolsado(montoTotalDesembolsado)
                .solicitudesPorEstado(porEstado)
                .promedioMontoSolicitado(promedioMontoSolicitado)
                .promedioCuotaMensual(promedioCuotaMensual)
                .build();
    }

    // ============================================================
    // RESUMEN POR ANALISTA (solo ADMIN)
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public List<AnalistaResumenResponse> obtenerResumenPorAnalista() {
        if (!securityUtils.esAdmin()) {
            throw new BusinessException("Solo el administrador puede ver el resumen por analista");
        }

        List<Usuario> analistas = usuarioRepository.findByRol(Rol.ANALISTA);
        List<AnalistaResumenResponse> resultado = new ArrayList<>();

        for (Usuario analista : analistas) {
            Long analistaId = analista.getId();

            long totalClientes = clienteRepository.countByAnalistaId(analistaId);

            List<SolicitudCredito> solicitudes =
                    solicitudRepository.findByClienteAnalistaId(analistaId);

            long totalSolicitudes = solicitudes.size();
            long pendientes = solicitudes.stream()
                    .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE).count();
            long aprobadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == EstadoSolicitud.APROBADO).count();
            long rechazadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == EstadoSolicitud.RECHAZADO).count();
            long desembolsadas = solicitudes.stream()
                    .filter(s -> s.getEstado() == EstadoSolicitud.DESEMBOLSADO).count();

            BigDecimal montoTotal = solicitudes.stream()
                    .map(SolicitudCredito::getMontoSolicitado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal montoDesembolsado = solicitudes.stream()
                    .filter(s -> s.getEstado() == EstadoSolicitud.DESEMBOLSADO)
                    .map(SolicitudCredito::getMontoSolicitado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resultado.add(AnalistaResumenResponse.builder()
                    .analistaId(analistaId)
                    .analistaNombre(analista.getNombreCompleto())
                    .analistaUsername(analista.getUsername())
                    .totalClientes(totalClientes)
                    .totalSolicitudes(totalSolicitudes)
                    .montoTotalSolicitado(montoTotal)
                    .montoTotalDesembolsado(montoDesembolsado)
                    .solicitudesPendientes(pendientes)
                    .solicitudesAprobadas(aprobadas)
                    .solicitudesRechazadas(rechazadas)
                    .solicitudesDesembolsadas(desembolsadas)
                    .build());
        }

        return resultado;
    }
}