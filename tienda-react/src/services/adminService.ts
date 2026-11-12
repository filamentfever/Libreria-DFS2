// ============================================================
// MSCI-ADMIN · dashboard, ventas, stock y reportes
// API Gateway: /api/admin/**
// ============================================================

import { api, conQuery } from "@/services/api"

export interface ProductoVendido {
  productoId: number
  nombre: string
  unidadesVendidas: number
  ingresos: number
}

export interface ProductoStock {
  id: number
  sku: string
  nombre: string
  stock: number
  activo: boolean
}

export interface EstadoStock {
  totalProductos: number
  activos: number
  agotados: number
  stockBajo: number
  criticos: ProductoStock[]
}

export interface Dashboard {
  ventasTotales: number
  cantidadPedidos: number
  pedidosPendientes: number
  pedidosPorEstado: Record<string, number>
  productosMasVendidos: ProductoVendido[]
  estadoStock: EstadoStock
  usuariosRegistrados: number
  generadoEn: string
}

export interface VentaDia {
  fecha: string
  total: number
  pedidos: number
}

export interface ReporteVentas {
  desde: string
  hasta: string
  ventasTotales: number
  cantidadPedidos: number
  ticketPromedio: number
  ventasPorDia: VentaDia[]
}

export interface ReporteGeneral {
  reporteId: number
  generadoEn: string
  dashboard: Dashboard
  ventasUltimos30Dias: ReporteVentas
}

export interface ReporteResumen {
  id: number
  tipo: string
  ventasTotales: number
  cantidadPedidos: number
  productosActivos: number
  usuariosRegistrados: number
  generadoEn: string
}

export const adminService = {
  dashboard: (): Promise<Dashboard> => api.get<Dashboard>("/api/admin/dashboard"),

  ventas: (desde?: string, hasta?: string): Promise<ReporteVentas> =>
    api.get<ReporteVentas>(conQuery("/api/admin/ventas", { desde, hasta })),

  productosMasVendidos: (limite = 10, desde?: string, hasta?: string): Promise<ProductoVendido[]> =>
    api.get<ProductoVendido[]>(
      conQuery("/api/admin/productos-mas-vendidos", { limite, desde, hasta })
    ),

  stock: (): Promise<EstadoStock> => api.get<EstadoStock>("/api/admin/stock"),

  reporteGeneral: (): Promise<ReporteGeneral> =>
    api.get<ReporteGeneral>("/api/admin/reporte-general"),

  historial: (): Promise<ReporteResumen[]> =>
    api.get<ReporteResumen[]>("/api/admin/reportes/historial"),
}
