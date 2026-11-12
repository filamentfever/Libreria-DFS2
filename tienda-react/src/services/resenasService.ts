// ============================================================
// MSCI-RESENAS · reseñas y valoraciones por producto
// API Gateway: /api/resenas/**
// ============================================================

import { api } from "@/services/api"

export interface Resena {
  id: number
  idProducto: number
  idUsuario: number
  autor: string
  puntuacion: number
  comentario: string
  fecha: string
}

export interface ResumenValoracion {
  idProducto: number
  promedio: number | null
  cantidad: number
  distribucion: Record<string, number>
}

export interface ResenaPayload {
  idProducto: number
  idUsuario: number
  puntuacion: number
  comentario: string
}

export const resenasService = {
  crear: (payload: ResenaPayload): Promise<Resena> =>
    api.post<Resena>("/api/resenas", payload),

  porProducto: (idProducto: number): Promise<Resena[]> =>
    api.get<Resena[]>(`/api/resenas/producto/${idProducto}`),

  promedio: (idProducto: number): Promise<ResumenValoracion> =>
    api.get<ResumenValoracion>(`/api/resenas/producto/${idProducto}/promedio`),
}
