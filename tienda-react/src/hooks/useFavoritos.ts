// Hook de favoritos: estado local sincronizado entre todas las pantallas.
import { useCallback, useEffect, useState } from "react"
import { EVENTO_FAVORITOS, favoritosService } from "@/services/favoritosService"
import type { Favorito } from "@/services/favoritosService"
import type { Producto } from "@/services/catalogoService"

export function useFavoritos() {
  const [favoritos, setFavoritos] = useState<Favorito[]>(() => favoritosService.listar())

  useEffect(() => {
    const actualizar = (evento: Event) => {
      const detalle = (evento as CustomEvent<Favorito[]>).detail
      setFavoritos(Array.isArray(detalle) ? detalle : favoritosService.listar())
    }
    window.addEventListener(EVENTO_FAVORITOS, actualizar)
    window.addEventListener("storage", actualizar)
    return () => {
      window.removeEventListener(EVENTO_FAVORITOS, actualizar)
      window.removeEventListener("storage", actualizar)
    }
  }, [])

  const esFavorito = useCallback(
    (id: number) => favoritos.some((f) => f.id === id),
    [favoritos]
  )

  const alternar = useCallback((producto: Producto) => {
    setFavoritos(favoritosService.alternar(producto))
  }, [])

  const quitar = useCallback((id: number) => {
    setFavoritos(favoritosService.quitar(id))
  }, [])

  const limpiar = useCallback(() => {
    favoritosService.limpiar()
    setFavoritos([])
  }, [])

  return { favoritos, esFavorito, alternar, quitar, limpiar }
}
