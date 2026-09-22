package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.DashboardResponse;
import com.procredito.backend.entity.SolicitudCredito;
import com.procredito.backend.entity.Usuario;
import com.procredito.backend.enums.EstadoSolicitud;
import com.procredito.backend.enums.Rol;
import com.procredito.backend.repository.ClienteRepository;
import com.procredito.backend.repository.SolicitudCreditoRepository;
import com.procredito.backend.security.SecurityUtils;
import com.procredito.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClienteRepository clienteRepository;
    private final SolicitudCreditoRepository solicitudRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen() {
        Usuario usuarioActual = securityUtils.getUsuarioAutenticado();
        boolean esAdmin = usuarioActual.getRol() == Rol.ADMIN;

        // ============================================================
        // 1. Total de clientes
        // ============================================================
        long totalClientes;
        if (esAdmin) {
            totalClientes = clienteRepository.count();
        } else {
            totalClientes = clienteRepository.countByAnalistaId(usuarioActual.getId());
        }

        // ============================================================
        // 2. Cargar solicitudes (filtradas por rol)
        // ============================================================
        List<SolicitudCredito> solicitudes;
        if (esAdmin) {
            solicitudes = solicitudRepository.findAll();
        } else {
            solicitudes = solicitudRepository.findByClienteAnalistaId(usuarioActual.getId());
        }
        long totalSolicitudes = solicitudes.size();

        // ============================================================
        // 3. Conteo por estado
        // ============================================================
        Map<String, Long> porEstado = new HashMap<>();
        for (EstadoSolicitud estado : EstadoSolicitud.values()) {
            long count = solicitudes.stream()
                    .filter(s -> s.getEstado() == estado)
                    .count();
            porEstado.put(estado.name(), count);
        }

        // ============================================================
        // 4. Suma de montos solicitados
        // ============================================================
        BigDecimal montoTotalSolicitado = solicitudes.stream()
                .map(SolicitudCredito::getMontoSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ============================================================
        // 5. Suma de montos desembolsados
        // ============================================================
        BigDecimal montoTotalDesembolsado = solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.DESEMBOLSADO)
                .map(SolicitudCredito::getMontoSolicitado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ============================================================
        // 6. Promedio del monto solicitado
        // ============================================================
        BigDecimal promedioMontoSolicitado = totalSolicitudes == 0
                ? BigDecimal.ZERO
                : montoTotalSolicitado.divide(
                BigDecimal.valueOf(totalSolicitudes), 2, RoundingMode.HALF_UP);

        // ============================================================
        // 7. Promedio de cuota mensual
        // ============================================================
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
}