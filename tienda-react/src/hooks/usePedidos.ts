// Hook de MSCI-PEDIDOS: historial del cliente y listado/filtros del panel admin.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { pedidosService } from "@/services/pedidosService"
import type {
  CheckoutPayload,
  FiltrosPedidos,
  Pedido,
} from "@/services/pedidosService"

export function usePedidos(
  usuario?: string | null,
  filtros: FiltrosPedidos = {},
  activo = true
) {
  const [pedidos, setPedidos] = useState<Pedido[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  const { estado, desde, hasta } = filtros

  useEffect(() => {
    if (!activo) {
      setCargando(false)
      return
    }

    let vigente = true
    setCargando(true)
    setError("")

    const peticion = usuario
      ? pedidosService.historial(usuario)
      : pedidosService.listar({ estado, desde, hasta })

    peticion
      .then((lista) => {
        if (vigente) setPedidos(lista)
      })
      .catch((ex) => {
        if (vigente) setError(mensajeDeError(ex))
      })
      .finally(() => {
        if (vigente) setCargando(false)
      })

    return () => {
      vigente = false
    }
  }, [usuario, estado, desde, hasta, activo, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  const crear = useCallback(async (payload: CheckoutPayload): Promise<Pedido> => {
    setError("")
    try {
      const pedido = await pedidosService.crear(payload)
      setPedidos((actuales) => [pedido, ...actuales])
      return pedido
    } catch (ex) {
      const texto = mensajeDeError(ex)
      setError(texto)
      throw new Error(texto)
    }
  }, [])

  return { pedidos, cargando, error, recargar, crear }
}
