// ============================================================
// MSCI-CATALOGO · productos públicos, categorías y búsqueda
// API Gateway: /api/productos/**
// ============================================================

import { api, conQuery } from "@/services/api"

export interface Producto {
  id: number
  slug: string
  sku: string
  nombre: string
  descripcion: string
  precio: number
  categoria: string
  subcategoria: string | null
  marca: string | null
  colores: string[]
  imagen: string | null
  stock: number
  activo: boolean
  nuevo: boolean
  fechaIngreso: string | null
}

export interface PaginaProductos {
  items: Producto[]
  pagina: number
  tamano: number
  total: number
  totalPaginas: number
  hayMas: boolean
  sinResultados: boolean
  mensaje: string | null
}

export interface Categoria {
  nombre: string
  subcategorias: string[]
}

export interface FiltrosCatalogo {
  categoria?: string
  subcategoria?: string
  q?: string
  precioMin?: number
  precioMax?: number
  orden?: "precio-asc" | "precio-desc" | "nombre-asc" | "nombre-desc" | "novedades" | string
  pagina?: number
  tamano?: number
}

export const catalogoService = {
  listar: (filtros: FiltrosCatalogo = {}): Promise<PaginaProductos> =>
    api.get<PaginaProductos>(
      conQuery("/api/productos", {
        categoria: filtros.categoria,
        subcategoria: filtros.subcategoria,
        q: filtros.q,
        precioMin: filtros.precioMin,
        precioMax: filtros.precioMax,
        orden: filtros.orden,
        pagina: filtros.pagina,
        tamano: filtros.tamano,
      })
    ),

  obtener: (id: number): Promise<Producto> => api.get<Producto>(`/api/productos/${id}`),

  obtenerPorSlug: (slug: string): Promise<Producto> =>
    api.get<Producto>(`/api/productos/slug/${encodeURIComponent(slug)}`),

  relacionados: (id: number, limite = 4): Promise<Producto[]> =>
    api.get<Producto[]>(`/api/productos/${id}/relacionados?limite=${limite}`),

  categorias: (): Promise<Categoria[]> => api.get<Categoria[]>("/api/productos/categorias"),

  sugerencias: (q: string): Promise<string[]> =>
    api.get<string[]>(conQuery("/api/productos/sugerencias", { q })),
}

/** Precio en pesos chilenos: 19990 -> "$19.990". */
export function formatoPrecio(valor: number | string | null | undefined): string {
  const numero = Number(valor ?? 0)
  return `$${numero.toLocaleString("es-CL")}`
}

/** La imagen del catálogo ya viene como /img/...; si falta usamos el banner. */
export function imagenProducto(producto: Pick<Producto, "imagen">): string {
  return producto.imagen?.trim() ? producto.imagen : "/img/imagendestaca.jpg"
}
