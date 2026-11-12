// Hook de MSCI-USUARIOS: datos de la cuenta, direcciones y actualización.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { useCarrito } from "@/client/context/CarritoContext"
import { usuariosService } from "@/services/usuariosService"
import type {
  ActualizarUsuarioPayload,
  Direccion,
  DireccionPayload,
  Usuario,
} from "@/services/usuariosService"

export function useUsuarios(nombreParam?: string | null) {
  const { usuario: usuarioSesion } = useCarrito()
  const nombre = nombreParam ?? usuarioSesion
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  useEffect(() => {
    if (!nombre) {
      setCargando(false)
      return
    }

    let activo = true
    setCargando(true)
    setError("")

    usuariosService
      .buscarPorNombre(nombre)
      .then((datos) => {
        if (activo) setUsuario(datos)
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
  }, [nombre, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  const actualizar = useCallback(
    async (payload: ActualizarUsuarioPayload): Promise<void> => {
      if (!usuario) return
      setError("")
      try {
        const actualizado = await usuariosService.actualizar(usuario.id, payload)
        setUsuario(actualizado)
      } catch (ex) {
        setError(mensajeDeError(ex))
        throw ex
      }
    },
    [usuario]
  )

  const agregarDireccion = useCallback(
    async (payload: DireccionPayload): Promise<Direccion[]> => {
      if (!usuario) return []
      setError("")
      const direcciones = await usuariosService.agregarDireccion(usuario.id, payload)
      setUsuario({ ...usuario, direcciones })
      return direcciones
    },
    [usuario]
  )

  const eliminarDireccion = useCallback(
    async (direccionId: number): Promise<void> => {
      if (!usuario) return
      const direcciones = await usuariosService.eliminarDireccion(usuario.id, direccionId)
      setUsuario({ ...usuario, direcciones })
    },
    [usuario]
  )

  const marcarPredeterminada = useCallback(
    async (direccionId: number): Promise<void> => {
      if (!usuario) return
      const direcciones = await usuariosService.marcarPredeterminada(usuario.id, direccionId)
      setUsuario({ ...usuario, direcciones })
    },
    [usuario]
  )

  return {
    usuario,
    direcciones: usuario?.direcciones ?? [],
    cargando,
    error,
    recargar,
    actualizar,
    agregarDireccion,
    eliminarDireccion,
    marcarPredeterminada,
  }
}
