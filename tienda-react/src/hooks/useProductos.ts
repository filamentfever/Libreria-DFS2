// Hook de MSCI-CATALOGO: catálogo paginado, detalle por slug y categorías.
import { useCallback, useEffect, useMemo, useState } from "react"
import { catalogoService } from "@/services/catalogoService"
import type {
  Categoria,
  FiltrosCatalogo,
  PaginaProductos,
  Producto,
} from "@/services/catalogoService"
import { mensajeDeError } from "@/services/api"

export function useProductos(inicial: FiltrosCatalogo = {}) {
  const [filtros, setFiltros] = useState<FiltrosCatalogo>(inicial)
  const [data, setData] = useState<PaginaProductos | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)
  const clave = JSON.stringify(filtros)

  useEffect(() => {
    let activo = true
    setCargando(true)
    setError("")

    catalogoService
      .listar(JSON.parse(clave) as FiltrosCatalogo)
      .then((pagina) => {
        if (!activo) return
        setData(pagina)
      })
      .catch((ex) => {
        if (!activo) return
        setError(mensajeDeError(ex))
      })
      .finally(() => {
        if (activo) setCargando(false)
      })

    return () => {
      activo = false
    }
  }, [clave, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  return {
    productos: data?.items ?? [],
    pagina: data?.pagina ?? 1,
    totalPaginas: data?.totalPaginas ?? 1,
    total: data?.total ?? 0,
    cargando,
    error,
    filtros,
    setFiltros,
    recargar,
  }
}

/** Variante controlada: vuelve a consultar cuando cambian los filtros. */
export function useCatalogo(filtros: FiltrosCatalogo) {
  const [data, setData] = useState<PaginaProductos | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)
  const clave = JSON.stringify(filtros)

  useEffect(() => {
    let activo = true
    setCargando(true)
    setError("")

    catalogoService
      .listar(JSON.parse(clave) as FiltrosCatalogo)
      .then((pagina) => {
        if (activo) setData(pagina)
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
    // `clave` resume todos los filtros y evita repetir la petición por identidad de objeto.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [clave, version])

  return {
    productos: data?.items ?? [],
    pagina: data?.pagina ?? 1,
    totalPaginas: data?.totalPaginas ?? 1,
    total: data?.total ?? 0,
    cargando,
    error,
    recargar: () => setVersion((v) => v + 1),
  }
}

export function useProducto(slug: string | undefined) {
  const [producto, setProducto] = useState<Producto | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  useEffect(() => {
    if (!slug) {
      setCargando(false)
      setError("Producto no encontrado.")
      return
    }

    let activo = true
    setCargando(true)
    setError("")

    const buscar = /^\d+$/.test(slug)
      ? catalogoService.obtener(Number(slug))
      : catalogoService.obtenerPorSlug(slug)

    buscar
      .then((encontrado) => {
        if (activo) setProducto(encontrado)
      })
      .catch((ex) => {
        if (!activo) return
        setProducto(null)
        setError(mensajeDeError(ex))
      })
      .finally(() => {
        if (activo) setCargando(false)
      })

    return () => {
      activo = false
    }
  }, [slug, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  return { producto, cargando, error, recargar }
}

export function useCategorias() {
  const [categorias, setCategorias] = useState<Categoria[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")

  useEffect(() => {
    let activo = true
    catalogoService
      .categorias()
      .then((lista) => {
        if (activo) setCategorias(lista)
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
  }, [])

  return { categorias, cargando, error }
}

/** Selecciona los primeros N productos para la portada. */
export function useDestacados(limite = 4) {
  const { productos, cargando, error, recargar } = useProductos({ tamano: limite })
  const destacados = useMemo(() => productos.slice(0, limite), [productos, limite])
  return { productos: destacados, cargando, error, recargar }
}
