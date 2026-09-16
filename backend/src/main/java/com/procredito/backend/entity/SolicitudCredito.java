package com.procredito.backend.entity;

import com.procredito.backend.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_credito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoSolicitado;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tasaInteres;

    @Column(nullable = false)
    private Integer plazoMeses;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cuotaMensual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    private LocalDateTime fechaActualizacion;
}