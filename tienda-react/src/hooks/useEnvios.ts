// Hook de MSCI-ENVIOS: cotización, tracking y puntos de retiro.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { enviosService } from "@/services/enviosService"
import type {
  Envio,
  PuntoRetiro,
  SeleccionRetiro,
  Tracking,
} from "@/services/enviosService"
import type { CotizacionEnvio } from "@/services/pedidosService"

export function useCotizacionEnvio(comuna: string) {
  const [cotizacion, setCotizacion] = useState<CotizacionEnvio | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    if (!comuna.trim()) {
      setCotizacion(null)
      return
    }

    let activo = true
    setCargando(true)
    setError("")

    enviosService
      .cotizar(comuna)
      .then((datos) => {
        if (activo) setCotizacion(datos)
      })
      .catch((ex) => {
        if (!activo) return
        setCotizacion(null)
        setError(mensajeDeError(ex))
      })
      .finally(() => {
        if (activo) setCargando(false)
      })

    return () => {
      activo = false
    }
  }, [comuna])

  return { cotizacion, cargando, error }
}

export function usePuntosRetiro(ciudad?: string) {
  const [puntos, setPuntos] = useState<PuntoRetiro[]>([])
  const [ciudades, setCiudades] = useState<string[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    let activo = true
    setCargando(true)
    setError("")

    Promise.all([enviosService.puntos(ciudad), enviosService.ciudades()])
      .then(([listaPuntos, listaCiudades]) => {
        if (!activo) return
        setPuntos(listaPuntos)
        setCiudades(listaCiudades)
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
  }, [ciudad])

  const seleccionar = useCallback(
    async (id: number): Promise<SeleccionRetiro> => enviosService.seleccionarPunto(id),
    []
  )

  return { puntos, ciudades, cargando, error, seleccionar }
}

export function useEnvio(pedidoNumero?: string) {
  const [envio, setEnvio] = useState<Envio | null>(null)
  const [tracking, setTracking] = useState<Tracking | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    if (!pedidoNumero) return
    let activo = true
    setCargando(true)
    enviosService
      .porPedido(pedidoNumero)
      .then((datos) => {
        if (activo) setEnvio(datos)
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
  }, [pedidoNumero])

  const consultarTracking = useCallback(async (id: number) => {
    try {
      const datos = await enviosService.tracking(id)
      setTracking(datos)
      return datos
    } catch (ex) {
      setError(mensajeDeError(ex))
      return null
    }
  }, [])

  return { envio, tracking, cargando, error, consultarTracking }
}
