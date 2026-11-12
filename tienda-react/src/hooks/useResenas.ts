// Hook de MSCI-RESENAS: listado, promedio y creación de reseñas.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { resenasService } from "@/services/resenasService"
import type { Resena, ResenaPayload, ResumenValoracion } from "@/services/resenasService"

export function useResenas(productoId?: number) {
  const [resenas, setResenas] = useState<Resena[]>([])
  const [resumen, setResumen] = useState<ResumenValoracion | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  useEffect(() => {
    if (!productoId) {
      setCargando(false)
      return
    }

    let activo = true
    setCargando(true)
    setError("")

    Promise.all([
      resenasService.porProducto(productoId),
      resenasService.promedio(productoId),
    ])
      .then(([lista, promedio]) => {
        if (!activo) return
        setResenas(lista)
        setResumen(promedio)
      })
      .catch((ex) => {
        if (activo) setError(mensajeDeError(ex))
      })
      .finally(() => {
        if (activo) setCargando(false)
      })

    return () => {
      activo = false
    }
  }, [productoId, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  const crear = useCallback(
    async (payload: ResenaPayload) => {
      const nueva = await resenasService.crear(payload)
      setResenas((actuales) => [nueva, ...actuales])
      setVersion((v) => v + 1)
      return nueva
    },
    []
  )

  return { resenas, resumen, cargando, error, crear, recargar }
}
