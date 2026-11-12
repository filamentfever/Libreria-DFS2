// ============================================================
// Inventario (gestionado por MSCI-CATALOGO en sus endpoints /admin)
// El frontend lo separa porque el panel de administración es su propio módulo.
// ============================================================

import { api, conQuery } from "@/services/api"
import type { Producto } from "@/services/catalogoService"

export interface ProductoPayload {
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
}

export interface ResultadoImportacion {
  agregados: number
  actualizados: number
  errores: string[]
}

interface ResultadoImportacionApi {
  insertados: number
  actualizados: number
  errores: Array<{ linea: number; motivo: string }>
}

export const inventarioService = {
  listar: (q?: string): Promise<Producto[]> =>
    api.get<Producto[]>(conQuery("/api/productos/admin", { q })),

  criticos: (): Promise<Producto[]> => api.get<Producto[]>("/api/productos/criticos"),

  crear: (payload: ProductoPayload): Promise<Producto> =>
    api.post<Producto>("/api/productos", payload),

  actualizar: (id: number, payload: ProductoPayload): Promise<Producto> =>
    api.put<Producto>(`/api/productos/${id}`, payload),

  eliminar: (id: number): Promise<void> => api.delete<void>(`/api/productos/${id}`),

  actualizarStock: (id: number, cantidad: number): Promise<Producto> =>
    api.patch<Producto>(`/api/productos/${id}/stock`, { cantidad }),

  importarCsv: (csv: string): Promise<ResultadoImportacion> =>
    api.postText<ResultadoImportacionApi>("/api/productos/importar-csv", csv).then(
      (resultado) => ({
        agregados: resultado.insertados,
        actualizados: resultado.actualizados,
        errores: resultado.errores.map(
          (error) => `Línea ${error.linea}: ${error.motivo}`
        ),
      })
    ),
}
