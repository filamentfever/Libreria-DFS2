// ============================================================
// MSCI-CARRITO · carrito persistido por usuario
// API Gateway: /api/carritos/**
// ============================================================

import { api } from "@/services/api"

export interface ItemCarrito {
  id: number
  productoId: number
  nombreProducto: string
  precioUnitario: number
  cantidad: number
  subtotal: number
}

export interface Carrito {
  id: number
  usuario: string
  fechaCreacion: string
  items: ItemCarrito[]
  cantidadTotal: number
  total: number
}

export const carritoService = {
  obtener: (id: number): Promise<Carrito> => api.get<Carrito>(`/api/carritos/${id}`),

  obtenerPorUsuario: (usuario: string): Promise<Carrito> =>
    api.get<Carrito>(`/api/carritos/usuario/${encodeURIComponent(usuario)}`),

  crear: (usuario: string): Promise<Carrito> => api.post<Carrito>("/api/carritos", { usuario }),

  agregarItem: (carritoId: number, productoId: number, cantidad = 1): Promise<Carrito> =>
    api.post<Carrito>(`/api/carritos/${carritoId}/items`, { productoId, cantidad }),

  cambiarCantidad: (carritoId: number, itemId: number, cantidad: number): Promise<Carrito> =>
    api.patch<Carrito>(`/api/carritos/${carritoId}/items/${itemId}`, { cantidad }),

  quitarItem: (carritoId: number, itemId: number): Promise<Carrito> =>
    api.delete<Carrito>(`/api/carritos/${carritoId}/items/${itemId}`),

  vaciar: (carritoId: number): Promise<Carrito> =>
    api.delete<Carrito>(`/api/carritos/${carritoId}/items`),
}

// Alias con los nombres usados por CarritoContext.
export const obtenerCarrito = carritoService.obtener
export const obtenerCarritoPorUsuario = carritoService.obtenerPorUsuario
export const crearCarrito = carritoService.crear
export const agregarItem = carritoService.agregarItem
export const cambiarCantidad = carritoService.cambiarCantidad
export const quitarItem = carritoService.quitarItem
export const vaciarCarrito = carritoService.vaciar
