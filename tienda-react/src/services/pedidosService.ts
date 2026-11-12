// ============================================================
// MSCI-PEDIDOS · checkout, historial y gestión de pedidos
// API Gateway: /api/pedidos/**
// ============================================================

import { api, conQuery } from "@/services/api"
import type { Tarjeta } from "@/services/pagosService"

export type MedioPago = "TARJETA" | "TRANSFERENCIA"
export type TipoEntrega = "DOMICILIO" | "RETIRO"

export interface CheckoutPayload {
  carritoId: number
  medioPago: MedioPago
  cuponCodigo?: string
  entrega: TipoEntrega
  comuna?: string
  direccion?: string
  sucursal?: string
  tarjeta?: Tarjeta
}

export interface ItemPedido {
  productoId: number
  nombreProducto: string
  precioUnitario: number
  cantidad: number
  subtotal: number
}

export interface Pedido {
  id: number
  numero: string
  usuario: string
  fecha: string
  estado: string
  estadoCliente: string
  entrega: string
  direccionDespacho: string
  comuna: string | null
  subtotal: number
  descuento: number
  envio: number
  neto: number
  iva: number
  total: number
  cupon: string | null
  medioPago: string
  pagoConfirmado: boolean
  codigoAutorizacion: string | null
  items: ItemPedido[]
}

export interface CotizacionEnvio {
  comuna: string
  costo: number
  diasMin: number
  diasMax: number
  mensajeEntrega: string
}

export interface FiltrosPedidos {
  estado?: string
  desde?: string
  hasta?: string
}

export const pedidosService = {
  crear: (payload: CheckoutPayload): Promise<Pedido> =>
    api.post<Pedido>("/api/pedidos", payload),

  listar: (filtros: FiltrosPedidos = {}): Promise<Pedido[]> =>
    api.get<Pedido[]>(
      conQuery("/api/pedidos", {
        estado: filtros.estado,
        desde: filtros.desde,
        hasta: filtros.hasta,
      })
    ),

  obtener: (id: number): Promise<Pedido> => api.get<Pedido>(`/api/pedidos/${id}`),

  porNumero: (numero: string): Promise<Pedido> =>
    api.get<Pedido>(`/api/pedidos/numero/${encodeURIComponent(numero)}`),

  historial: (usuario: string): Promise<Pedido[]> =>
    api.get<Pedido[]>(`/api/pedidos/usuario/${encodeURIComponent(usuario)}`),

  cotizarEnvio: (comuna: string): Promise<CotizacionEnvio> =>
    api.get<CotizacionEnvio>(`/api/pedidos/envio/${encodeURIComponent(comuna)}`),

  cambiarEstado: (id: number, estado: string): Promise<Pedido> =>
    api.patch<Pedido>(`/api/pedidos/${id}/estado`, { estado }),

  adjuntarComprobante: (id: number, archivo: string): Promise<Pedido> =>
    api.put<Pedido>(`/api/pedidos/${id}/comprobante-transferencia`, { archivo }),
}

export const ESTADOS_PEDIDO = [
  "NUEVO",
  "PENDIENTE",
  "ARMANDO_CAJA",
  "LISTO_PARA_RETIRO",
  "ENVIADO",
  "ENTREGADO",
  "CANCELADO",
] as const
