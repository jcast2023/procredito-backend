package com.procredito.backend.serviceImpl;

import com.procredito.backend.dto.CambioEstadoRequest;
import com.procredito.backend.dto.SolicitudRequest;
import com.procredito.backend.dto.SolicitudResponse;
import com.procredito.backend.entity.Cliente;
import com.procredito.backend.entity.SolicitudCredito;
import com.procredito.backend.enums.EstadoSolicitud;
import com.procredito.backend.repository.ClienteRepository;
import com.procredito.backend.repository.SolicitudCreditoRepository;
import com.procredito.backend.service.SolicitudCreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.procredito.backend.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class SolicitudCreditoServiceImpl implements SolicitudCreditoService {

    private final SolicitudCreditoRepository solicitudRepository;
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public SolicitudResponse crear(SolicitudRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new BusinessException("Cliente no encontrado con ID: " + request.getClienteId()));

        BigDecimal cuota = calcularCuota(
                request.getMontoSolicitado(),
                request.getTasaInteres(),
                request.getPlazoMeses()
        );

        SolicitudCredito solicitud = SolicitudCredito.builder()
                .cliente(cliente)
                .montoSolicitado(request.getMontoSolicitado())
                .tasaInteres(request.getTasaInteres())
                .plazoMeses(request.getPlazoMeses())
                .cuotaMensual(cuota)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        SolicitudCredito guardada = solicitudRepository.save(solicitud);
        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(Long id) {
        SolicitudCredito solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada"));
        return mapToResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarTodas() {
        return solicitudRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarPorEstado(EstadoSolicitud estado) {
        return solicitudRepository.findByEstado(estado)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SolicitudResponse cambiarEstado(Long id, CambioEstadoRequest request) {
        SolicitudCredito solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Solicitud no encontrada con ID: " + id));

        EstadoSolicitud estadoActual = solicitud.getEstado();
        EstadoSolicitud nuevoEstado = request.getNuevoEstado();

        // Validar transición permitida
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new BusinessException(
                    "Transición de estado no permitida: " + estadoActual + " → " + nuevoEstado +
                            ". Transiciones válidas desde " + estadoActual + ": " +
                            obtenerTransicionesValidas(estadoActual)
            );
        }

        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaActualizacion(LocalDateTime.now());

        return mapToResponse(solicitudRepository.save(solicitud));
    }

    /**
     * Reglas de negocio para transiciones de estado:
     * PENDIENTE   → APROBADO | RECHAZADO
     * APROBADO    → DESEMBOLSADO
     * RECHAZADO   → (terminal, no permite cambios)
     * DESEMBOLSADO→ (terminal, no permite cambios)
     */
    private boolean esTransicionValida(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (nuevo == null) return false;

        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoSolicitud.APROBADO
                    || nuevo == EstadoSolicitud.RECHAZADO;
            case APROBADO -> nuevo == EstadoSolicitud.DESEMBOLSADO;
            case RECHAZADO, DESEMBOLSADO -> false;
        };
    }

    private String obtenerTransicionesValidas(EstadoSolicitud estado) {
        return switch (estado) {
            case PENDIENTE -> "[APROBADO, RECHAZADO]";
            case APROBADO -> "[DESEMBOLSADO]";
            case RECHAZADO, DESEMBOLSADO -> "ninguna (estado terminal)";
        };
    }

    // Cálculo de cuota - Sistema Francés simplificado
    private BigDecimal calcularCuota(BigDecimal monto, BigDecimal tasaAnual, Integer plazoMeses) {
        // Tasa mensual
        BigDecimal tasaMensual = tasaAnual
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        // Fórmula: Cuota = P * (i * (1+i)^n) / ((1+i)^n - 1)
        BigDecimal unoMasI = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = unoMasI.pow(plazoMeses, new MathContext(10));

        BigDecimal numerador = monto.multiply(tasaMensual).multiply(potencia);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE);

        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private SolicitudResponse mapToResponse(SolicitudCredito s) {
        return SolicitudResponse.builder()
                .id(s.getId())
                .clienteId(s.getCliente().getId())
                .clienteNombres(s.getCliente().getNombres())
                .clienteDocumento(s.getCliente().getDocumento())
                .montoSolicitado(s.getMontoSolicitado())
                .tasaInteres(s.getTasaInteres())
                .plazoMeses(s.getPlazoMeses())
                .cuotaMensual(s.getCuotaMensual())
                .estado(s.getEstado())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaActualizacion(s.getFechaActualizacion())
                .build();
    }
}
