// ============================================================
// MSCI-NOTIFICACIONES · correos simulados y cola de envío
// API Gateway: /api/notificaciones/**
// ============================================================

import { api } from "@/services/api"

export interface Notificacion {
  id: number
  tipoEvento: string
  destinatario: string
  asunto: string
  mensaje: string
  estadoEnvio: string
  intentos: number
  fechaCreacion: string
  fechaEnvio: string | null
}

export interface NotificacionPayload {
  tipoEvento: string
  destinatario: string
  asunto: string
  mensaje: string
}

export interface RegistroUsuarioPayload {
  nombre: string
  email: string
}

export interface ConfirmacionPedidoPayload {
  email: string
  numeroPedido: string
  total: number
}

export interface ResultadoCola {
  procesadas: number
  enviadas: number
  fallidas: number
}

export const notificacionesService = {
  enviar: (payload: NotificacionPayload): Promise<Notificacion> =>
    api.post<Notificacion>("/api/notificaciones", payload),

  registroUsuario: (payload: RegistroUsuarioPayload): Promise<Notificacion> =>
    api.post<Notificacion>("/api/notificaciones/registro-usuario", payload),

  confirmacionPedido: (payload: ConfirmacionPedidoPayload): Promise<Notificacion> =>
    api.post<Notificacion>("/api/notificaciones/confirmacion-pedido", payload),

  alerta: (payload: NotificacionPayload): Promise<Notificacion> =>
    api.post<Notificacion>("/api/notificaciones/alerta", payload),

  enviarPorId: (id: number): Promise<Notificacion> =>
    api.post<Notificacion>(`/api/notificaciones/${id}/enviar`),

  procesarCola: (): Promise<ResultadoCola> =>
    api.post<ResultadoCola>("/api/notificaciones/procesar-cola"),

  listar: (): Promise<Notificacion[]> =>
    api.get<Notificacion[]>("/api/notificaciones"),
}
