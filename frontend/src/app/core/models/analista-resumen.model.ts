export interface AnalistaResumen {
  analistaId: number;
  analistaNombre: string;
  analistaUsername: string;
  totalClientes: number;
  totalSolicitudes: number;
  montoTotalSolicitado: number;
  montoTotalDesembolsado: number;
  solicitudesPendientes: number;
  solicitudesAprobadas: number;
  solicitudesRechazadas: number;
  solicitudesDesembolsadas: number;
}
