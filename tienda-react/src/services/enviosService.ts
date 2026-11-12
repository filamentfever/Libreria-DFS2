// ============================================================
// MSCI-ENVIOS · despachos, tracking y puntos de retiro
// API Gateway: /api/envios/** y /api/retiros/**
// ============================================================

import { api, conQuery } from "@/services/api"
import type { CotizacionEnvio } from "@/services/pedidosService"

export interface Envio {
  id: number
  pedidoNumero: string
  destinatario: string
  direccion: string
  comuna: string
  costo: number
  estadoDespacho: string
  numeroSeguimiento: string | null
  transportista: string | null
  notas: string | null
  mensajeEntrega: string | null
  fechaCreacion: string
}

export interface Tracking {
  transportista: string
  codigo: string
  url: string
}

export interface PuntoRetiro {
  id: number
  sucursal: string
  direccion: string
  ciudad: string
  horario: string
  activo: boolean
}

export interface SeleccionRetiro {
  puntoId: number
  sucursal: string
  costoEnvio: number
  direccionTemporal: string
  horario: string
}

export interface InstruccionesRetiro {
  pedidoNumero: string
  codigo: string
  direccion: string
  horario: string
  texto: string
}

export interface EnvioPayload {
  pedidoNumero: string
  destinatario: string
  direccion: string
  comuna: string
  notas: string
}

export const enviosService = {
  cotizar: (comuna: string): Promise<CotizacionEnvio> =>
    api.get<CotizacionEnvio>(`/api/envios/cotizacion?comuna=${encodeURIComponent(comuna)}`),

  crear: (payload: EnvioPayload): Promise<Envio> => api.post<Envio>("/api/envios", payload),

  obtener: (id: number): Promise<Envio> => api.get<Envio>(`/api/envios/${id}`),

  porPedido: (numero: string): Promise<Envio> =>
    api.get<Envio>(`/api/envios/pedido/${encodeURIComponent(numero)}`),

  cambiarEstado: (id: number, estado: string): Promise<Envio> =>
    api.patch<Envio>(`/api/envios/${id}/estado`, { estado }),

  actualizarTracking: (id: number, transportista: string, codigo: string): Promise<Tracking> =>
    api.put<Tracking>(`/api/envios/${id}/tracking`, { transportista, codigo }),

  tracking: (id: number): Promise<Tracking> => api.get<Tracking>(`/api/envios/${id}/tracking`),

  puntos: (ciudad?: string): Promise<PuntoRetiro[]> =>
    api.get<PuntoRetiro[]>(conQuery("/api/retiros/puntos", { ciudad })),

  ciudades: (): Promise<string[]> => api.get<string[]>("/api/retiros/ciudades"),

  punto: (id: number): Promise<PuntoRetiro> => api.get<PuntoRetiro>(`/api/retiros/puntos/${id}`),

  seleccionarPunto: (id: number): Promise<SeleccionRetiro> =>
    api.post<SeleccionRetiro>(`/api/retiros/puntos/${id}/seleccionar`),

  instrucciones: (pedido: string, punto: number): Promise<InstruccionesRetiro> =>
    api.get<InstruccionesRetiro>(
      conQuery("/api/retiros/instrucciones", { pedido, punto })
    ),
}
