// ============================================================
// MSCI-PAGOS · pasarela simulada, cupones y datos de transferencia
// API Gateway: /api/pagos/**
// ============================================================

import { api } from "@/services/api"

export interface Tarjeta {
  titular: string
  numero: string
  vencimiento: string
  cvv: string
}

export interface PagoTarjetaPayload extends Tarjeta {
  monto: number
  referencia?: string
}

export interface ResultadoPago {
  aprobado: boolean
  codigoAutorizacion: string | null
  motivo: string | null
  mensaje: string
  puedeReintentar: boolean
  errores: Record<string, string> | null
}

export interface CuponValidacion {
  valido: boolean
  codigo: string | null
  porcentaje: number
  mensaje: string
}

export interface DatosTransferencia {
  titular: string
  rut: string
  banco: string
  tipoCuenta: string
  numeroCuenta: string
  email: string
  aviso: string
}

export const pagosService = {
  pagarTarjeta: (payload: PagoTarjetaPayload): Promise<ResultadoPago> =>
    api.post<ResultadoPago>("/api/pagos/tarjeta", payload),

  validarCupon: (codigo: string): Promise<CuponValidacion> =>
    api.get<CuponValidacion>(`/api/pagos/cupones/${encodeURIComponent(codigo)}`),

  datosTransferencia: (): Promise<DatosTransferencia> =>
    api.get<DatosTransferencia>("/api/pagos/transferencia/datos"),
}
