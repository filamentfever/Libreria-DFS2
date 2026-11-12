// Catálogo: filtros, búsqueda, orden y paginación contra MSCI-CATALOGO.
import { useEffect, useState } from "react"
import { Link, useSearchParams } from "react-router"
import { ProductoCard } from "@/client/components/ProductoCard"
import { useCatalogo, useCategorias } from "@/hooks/useProductos"

export const CatalogoPage = () => {
  const [params, setParams] = useSearchParams()
  const { categorias } = useCategorias()

  const categoria = params.get("categoria") ?? ""
  const orden = params.get("orden") ?? ""
  const q = params.get("q") ?? ""
  const pagina = Number(params.get("pagina") ?? "1") || 1

  const [texto, setTexto] = useState(q)

  useEffect(() => {
    setTexto(q)
  }, [q])

  const aplicar = (cambios: Record<string, string | number | undefined>): void => {
    const siguientes = new URLSearchParams(params)
    for (const [clave, valor] of Object.entries(cambios)) {
      if (valor === undefined || valor === "") siguientes.delete(clave)
      else siguientes.set(clave, String(valor))
    }
    setParams(siguientes)
  }

  const { productos, totalPaginas, total, cargando, error } = useCatalogo({
    categoria: categoria || undefined,
    orden: orden || undefined,
    q: q || undefined,
    pagina,
    tamano: 12,
  })

  const paginas = Array.from({ length: Math.max(1, totalPaginas) }, (_, i) => i + 1)

  return (
    <section>
      <div className="container">
        <nav aria-label="Ruta de navegación" className="migas">
          <Link to="/">Inicio</Link> / <span>Catálogo</span>
        </nav>

        <h1 className="section-title">Catálogo de productos</h1>

        <form
          className="filters"
          onSubmit={(e) => {
            e.preventDefault()
            aplicar({ q: texto.trim() || undefined, pagina: 1 })
          }}
        >
          <div>
            <label htmlFor="categoria">Categoría</label>
            <select
              id="categoria"
              name="categoria"
              value={categoria}
              onChange={(e) => aplicar({ categoria: e.target.value || undefined, pagina: 1 })}
            >
              <option value="">Todas</option>
              {categorias.map((c) => (
                <option key={c.nombre} value={c.nombre}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="orden">Ordenar por</label>
            <select
              id="orden"
              name="orden"
              value={orden}
              onChange={(e) => aplicar({ orden: e.target.value || undefined, pagina: 1 })}
            >
              <option value="">Relevancia</option>
              <option value="precio-asc">Precio: menor a mayor</option>
              <option value="precio-desc">Precio: mayor a menor</option>
              <option value="nombre-asc">Nombre: A-Z</option>
              <option value="novedades">Novedades</option>
            </select>
          </div>

          <div>
            <label htmlFor="buscar">Buscar</label>
            <input
              type="search"
              id="buscar"
              name="buscar"
              placeholder="Nombre del producto"
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
            />
          </div>

          <div className="filtro-acciones">
            <button type="submit" className="btn btn-primary btn-sm">
              Buscar
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => {
                setTexto("")
                aplicar({ q: undefined, categoria: undefined, orden: undefined, pagina: 1 })
              }}
            >
              Limpiar
            </button>
          </div>
        </form>

        {cargando && <p className="cargando">Cargando catálogo…</p>}

        {error && (
          <p className="alerta alerta-error">
            No se pudo cargar el catálogo: {error}. ¿Está corriendo el API Gateway en el puerto
            8080?
          </p>
        )}

        {!cargando && !error && productos.length === 0 && (
          <p className="alerta alerta-ok">No hay productos que coincidan con la búsqueda.</p>
        )}

        {!cargando && !error && productos.length > 0 && (
          <>
            <p className="resultado-conteo">
              {total} producto{total === 1 ? "" : "s"} encontrado{total === 1 ? "" : "s"}
            </p>

            <div className="product-grid">
              {productos.map((producto) => (
                <ProductoCard key={producto.id} producto={producto} />
              ))}
            </div>

            {totalPaginas > 1 && (
              <nav aria-label="Paginación" className="paginacion">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  disabled={pagina <= 1}
                  onClick={() => aplicar({ pagina: Math.max(1, pagina - 1) })}
                >
                  Anterior
                </button>
                {paginas.map((numero) => (
                  <button
                    key={numero}
                    type="button"
                    className="btn btn-secondary btn-sm"
                    aria-current={numero === pagina ? "page" : undefined}
                    onClick={() => aplicar({ pagina: numero })}
                  >
                    {numero}
                  </button>
                ))}
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  disabled={pagina >= totalPaginas}
                  onClick={() => aplicar({ pagina: Math.min(totalPaginas, pagina + 1) })}
                >
                  Siguiente
                </button>
              </nav>
            )}
          </>
        )}
      </div>
    </section>
  )
}
