// ============================================================
// Favoritos · preferencia local del navegador
// (no existe un microservicio de favoritos en el backend)
// ============================================================

import type { Producto } from "@/services/catalogoService"

const CLAVE = "libreria.favoritos"

export interface Favorito {
  id: number
  slug: string
  nombre: string
  precio: number
  imagen: string | null
}

function leer(): Favorito[] {
  try {
    const texto = localStorage.getItem(CLAVE)
    if (!texto) return []
    const datos = JSON.parse(texto) as Favorito[]
    return Array.isArray(datos) ? datos : []
  } catch {
    return []
  }
}

export const EVENTO_FAVORITOS = "libreria:favoritos"

function guardar(favoritos: Favorito[]): void {
  localStorage.setItem(CLAVE, JSON.stringify(favoritos))
  window.dispatchEvent(new CustomEvent(EVENTO_FAVORITOS, { detail: favoritos }))
}

export const favoritosService = {
  listar: (): Favorito[] => leer(),

  agregar: (producto: Producto): Favorito[] => {
    const actuales = leer()
    if (actuales.some((f) => f.id === producto.id)) return actuales
    const nuevo: Favorito = {
      id: producto.id,
      slug: producto.slug,
      nombre: producto.nombre,
      precio: producto.precio,
      imagen: producto.imagen,
    }
    const siguientes = [nuevo, ...actuales]
    guardar(siguientes)
    return siguientes
  },

  quitar: (id: number): Favorito[] => {
    const siguientes = leer().filter((f) => f.id !== id)
    guardar(siguientes)
    return siguientes
  },

  alternar: (producto: Producto): Favorito[] => {
    return leer().some((f) => f.id === producto.id)
      ? favoritosService.quitar(producto.id)
      : favoritosService.agregar(producto)
  },

  limpiar: (): void => {
    localStorage.removeItem(CLAVE)
    window.dispatchEvent(new CustomEvent(EVENTO_FAVORITOS, { detail: [] }))
  },
}
