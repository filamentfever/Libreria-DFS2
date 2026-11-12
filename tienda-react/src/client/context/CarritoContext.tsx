// Estado global del carrito: crea/recupera el carrito del usuario y mantiene
// sincronizadas todas las pantallas con MSCI-CARRITO (sin estado duplicado).

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from "react"
import { ApiError, mensajeDeError } from "@/services/api"
import { carritoService } from "@/services/carritoService"
import type { Carrito, ItemCarrito } from "@/services/carritoService"
import { obtenerOCrearUsuario } from "@/services/usuariosService"

interface ValorCarrito {
  carrito: Carrito | null
  items: ItemCarrito[]
  usuario: string | null
  usuarioId: number | null
  cantidadTotal: number
  total: number
  cargando: boolean
  error: string
  agregandoId: number | null
  agregar: (productoId: number, cantidad?: number) => Promise<void>
  cambiarCantidad: (itemId: number, cantidad: number) => Promise<void>
  quitar: (itemId: number) => Promise<void>
  vaciar: () => Promise<void>
  /** Vuelve a leer el carrito del backend (p. ej. después del checkout). */
  recargar: () => Promise<void>
  limpiarError: () => void
}

const CarritoContext = createContext<ValorCarrito | null>(null)

export function CarritoProvider({ children }: { children: ReactNode }) {
  const [carrito, setCarrito] = useState<Carrito | null>(null)
  const [usuario, setUsuario] = useState<string | null>(null)
  const [usuarioId, setUsuarioId] = useState<number | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [agregandoId, setAgregandoId] = useState<number | null>(null)
  const iniciado = useRef(false)

  // Crea la sesión (usuario + carrito) una sola vez al cargar la aplicación.
  useEffect(() => {
    if (iniciado.current) return
    iniciado.current = true

    const iniciar = async (): Promise<void> => {
      try {
        const cuenta = await obtenerOCrearUsuario()
        setUsuario(cuenta.username)
        setUsuarioId(cuenta.id)

        let carritoActual: Carrito
        try {
          carritoActual = await carritoService.obtenerPorUsuario(cuenta.username)
        } catch (ex) {
          if (ex instanceof ApiError && ex.status === 404) {
            carritoActual = await carritoService.crear(cuenta.username)
          } else {
            throw ex
          }
        }
        setCarrito(carritoActual)
      } catch (ex) {
        iniciado.current = false
        setError(mensajeDeError(ex))
      } finally {
        setCargando(false)
      }
    }

    void iniciar()
  }, [])

  const recargar = useCallback(async (): Promise<void> => {
    if (!usuario) return
    try {
      const actualizado = await carritoService.obtenerPorUsuario(usuario)
      setCarrito(actualizado)
    } catch (ex) {
      setError(mensajeDeError(ex))
    }
  }, [usuario])

  const agregar = useCallback(
    async (productoId: number, cantidad = 1): Promise<void> => {
      if (!carrito) {
        throw new Error("El carrito todavía se está preparando. Intenta de nuevo en un momento.")
      }
      setError("")
      setAgregandoId(productoId)
      try {
        const actualizado = await carritoService.agregarItem(carrito.id, productoId, cantidad)
        setCarrito(actualizado)
      } catch (ex) {
        const texto = mensajeDeError(ex)
        setError(texto)
        throw new Error(texto)
      } finally {
        setAgregandoId(null)
      }
    },
    [carrito]
  )

  const cambiarCantidad = useCallback(
    async (itemId: number, cantidad: number): Promise<void> => {
      if (!carrito) return
      setError("")
      try {
        setCarrito(await carritoService.cambiarCantidad(carrito.id, itemId, cantidad))
      } catch (ex) {
        setError(mensajeDeError(ex))
      }
    },
    [carrito]
  )

  const quitar = useCallback(
    async (itemId: number): Promise<void> => {
      if (!carrito) return
      setError("")
      try {
        setCarrito(await carritoService.quitarItem(carrito.id, itemId))
      } catch (ex) {
        setError(mensajeDeError(ex))
      }
    },
    [carrito]
  )

  const vaciar = useCallback(async (): Promise<void> => {
    if (!carrito) return
    setError("")
    try {
      setCarrito(await carritoService.vaciar(carrito.id))
    } catch (ex) {
      setError(mensajeDeError(ex))
    }
  }, [carrito])

  const limpiarError = useCallback(() => setError(""), [])

  return (
    <CarritoContext.Provider
      value={{
        carrito,
        items: carrito?.items ?? [],
        usuario,
        usuarioId,
        cantidadTotal: carrito?.cantidadTotal ?? 0,
        total: carrito?.total ?? 0,
        cargando,
        error,
        agregandoId,
        agregar,
        cambiarCantidad,
        quitar,
        vaciar,
        recargar,
        limpiarError,
      }}
    >
      {children}
    </CarritoContext.Provider>
  )
}

export function useCarrito(): ValorCarrito {
  const contexto = useContext(CarritoContext)
  if (!contexto) {
    throw new Error("useCarrito debe usarse dentro de <CarritoProvider>")
  }
  return contexto
}
