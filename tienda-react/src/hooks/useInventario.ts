// Hook de inventario: CRUD de productos y stock desde el panel admin.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { inventarioService } from "@/services/inventarioService"
import type { ProductoPayload } from "@/services/inventarioService"
import type { Producto } from "@/services/catalogoService"

export function useInventario(q = "") {
  const [productos, setProductos] = useState<Producto[]>([])
  const [cargando, setCargando] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let activo = true
    setCargando(true)
    setError("")

    inventarioService
      .listar(q)
      .then((lista) => {
        if (activo) setProductos(lista)
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
  }, [q, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  const cambiarStock = useCallback(async (id: number, cantidad: number) => {
    setGuardando(true)
    try {
      await inventarioService.actualizarStock(id, cantidad)
      setVersion((v) => v + 1)
    } finally {
      setGuardando(false)
    }
  }, [])

  const crear = useCallback(async (payload: ProductoPayload) => {
    setGuardando(true)
    try {
      await inventarioService.crear(payload)
      setVersion((v) => v + 1)
    } finally {
      setGuardando(false)
    }
  }, [])

  const actualizar = useCallback(async (id: number, payload: ProductoPayload) => {
    setGuardando(true)
    try {
      await inventarioService.actualizar(id, payload)
      setVersion((v) => v + 1)
    } finally {
      setGuardando(false)
    }
  }, [])

  const eliminar = useCallback(async (id: number) => {
    setGuardando(true)
    try {
      await inventarioService.eliminar(id)
      setVersion((v) => v + 1)
    } finally {
      setGuardando(false)
    }
  }, [])

  const importarCsv = useCallback(async (csv: string) => {
    setGuardando(true)
    try {
      const resultado = await inventarioService.importarCsv(csv)
      setVersion((v) => v + 1)
      return resultado
    } finally {
      setGuardando(false)
    }
  }, [])

  return {
    productos,
    cargando,
    guardando,
    error,
    recargar,
    cambiarStock,
    crear,
    actualizar,
    eliminar,
    importarCsv,
  }
}
