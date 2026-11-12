// Hook de MSCI-NOTIFICACIONES: envío y cola de correos simulados.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { notificacionesService } from "@/services/notificacionesService"
import type {
  Notificacion,
  NotificacionPayload,
  ResultadoCola,
} from "@/services/notificacionesService"

export function useNotificaciones(auto = false) {
  const [notificaciones, setNotificaciones] = useState<Notificacion[]>([])
  const [cargando, setCargando] = useState(auto)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  useEffect(() => {
    if (!auto) return
    let activo = true
    setCargando(true)
    notificacionesService
      .listar()
      .then((lista) => {
        if (activo) setNotificaciones(lista)
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
  }, [auto, version])

  const enviar = useCallback(async (payload: NotificacionPayload) => {
    setError("")
    try {
      const notificacion = await notificacionesService.enviar(payload)
      setNotificaciones((actuales) => [notificacion, ...actuales])
      return notificacion
    } catch (ex) {
      setError(mensajeDeError(ex))
      throw ex
    }
  }, [])

  const procesarCola = useCallback(async (): Promise<ResultadoCola> => {
    const resultado = await notificacionesService.procesarCola()
    setVersion((v) => v + 1)
    return resultado
  }, [])

  return {
    notificaciones,
    cargando,
    error,
    enviar,
    procesarCola,
    recargar: () => setVersion((v) => v + 1),
  }
}
