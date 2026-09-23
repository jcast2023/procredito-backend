export interface DashboardResumen {
  totalClientes: number;
  totalSolicitudes: number;
  montoTotalSolicitado: number;
  montoTotalDesembolsado: number;
  solicitudesPorEstado: {
    PENDIENTE: number;
    APROBADO: number;
    RECHAZADO: number;
    DESEMBOLSADO: number;
  };
  promedioMontoSolicitado: number;
  promedioCuotaMensual: number;
}
