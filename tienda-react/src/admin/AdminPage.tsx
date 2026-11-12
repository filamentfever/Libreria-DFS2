// Panel de administración: dashboard, inventario y pedidos.
// Consume MSCI-ADMIN, MSCI-CATALOGO (inventario) y MSCI-PEDIDOS.
import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from "react"
import { Link } from "react-router"
import { useAdmin } from "@/hooks/useAdmin"
import { useInventario } from "@/hooks/useInventario"
import { usePedidos } from "@/hooks/usePedidos"
import { useCategorias } from "@/hooks/useProductos"
import { formatoPrecio, imagenProducto } from "@/services/catalogoService"
import type { Producto } from "@/services/catalogoService"
import type { ProductoPayload } from "@/services/inventarioService"
import { ESTADOS_PEDIDO, pedidosService } from "@/services/pedidosService"
import type { Pedido } from "@/services/pedidosService"
import { mensajeDeError } from "@/services/api"

type Seccion = "dashboard" | "inventario" | "pedidos"

interface FormularioProducto {
  nombre: string
  sku: string
  categoria: string
  subcategoria: string
  marca: string
  descripcion: string
  precio: string
  stock: string
  imagen: string
}

const FORM_VACIO: FormularioProducto = {
  nombre: "",
  sku: "",
  categoria: "",
  subcategoria: "",
  marca: "",
  descripcion: "",
  precio: "",
  stock: "0",
  imagen: "/img/imagendestaca.jpg",
}

function claseEstado(estado: string): string {
  return `badge-estado st-${estado.toLowerCase().replace(/_/g, "-")}`
}

function formatearFecha(fecha: string | null | undefined): string {
  if (!fecha) return "—"
  const [anio, mes, dia] = fecha.slice(0, 10).split("-")
  return dia && mes && anio ? `${dia}-${mes}-${anio}` : fecha
}

function badgeStock(stock: number): { clase: string; texto: string } {
  if (stock === 0) return { clase: "badge-stock sin-stock", texto: "Sin stock" }
  if (stock < 5) return { clase: "badge-stock bajo", texto: "Stock bajo" }
  return { clase: "badge-stock", texto: "Disponible" }
}

export const AdminPage = () => {
  const [seccion, setSeccion] = useState<Seccion>("dashboard")

  const { dashboard, stock, cargando: cargandoAdmin, error: errorAdmin, recargar: recargarAdmin } =
    useAdmin()
  const {
    productos,
    cargando: cargandoInventario,
    guardando,
    error: errorInventario,
    crear,
    actualizar,
    eliminar,
    cambiarStock,
    importarCsv,
  } = useInventario("")
  const { categorias } = useCategorias()

  const [busqueda, setBusqueda] = useState("")
  const [stocks, setStocks] = useState<Record<number, number>>({})
  const [mensaje, setMensaje] = useState("")
  const [errorAccion, setErrorAccion] = useState("")

  const [modalProducto, setModalProducto] = useState(false)
  const [productoEditado, setProductoEditado] = useState<Producto | null>(null)
  const [form, setForm] = useState<FormularioProducto>(FORM_VACIO)

  const [filtroEstado, setFiltroEstado] = useState("")
  const [desde, setDesde] = useState("")
  const [hasta, setHasta] = useState("")
  const {
    pedidos,
    cargando: cargandoPedidos,
    error: errorPedidos,
    recargar: recargarPedidos,
  } = usePedidos(
    undefined,
    {
      estado: filtroEstado || undefined,
      desde: desde || undefined,
      hasta: hasta || undefined,
    },
    true
  )
  const [pedidoDetalle, setPedidoDetalle] = useState<Pedido | null>(null)
  const [cambiandoPedido, setCambiandoPedido] = useState<number | null>(null)

  useEffect(() => {
    setStocks(Object.fromEntries(productos.map((producto) => [producto.id, producto.stock])))
  }, [productos])

  const productosFiltrados = useMemo(() => {
    const termino = busqueda.trim().toLowerCase()
    if (!termino) return productos
    return productos.filter(
      (producto) =>
        producto.nombre.toLowerCase().includes(termino) ||
        producto.sku.toLowerCase().includes(termino)
    )
  }, [busqueda, productos])

  const abrirNuevo = (): void => {
    setProductoEditado(null)
    setForm({ ...FORM_VACIO, categoria: categorias[0]?.nombre ?? "" })
    setErrorAccion("")
    setModalProducto(true)
  }

  const abrirEditar = (producto: Producto): void => {
    setProductoEditado(producto)
    setForm({
      nombre: producto.nombre,
      sku: producto.sku,
      categoria: producto.categoria,
      subcategoria: producto.subcategoria ?? "",
      marca: producto.marca ?? "",
      descripcion: producto.descripcion,
      precio: String(producto.precio),
      stock: String(producto.stock),
      imagen: producto.imagen ?? "/img/imagendestaca.jpg",
    })
    setErrorAccion("")
    setModalProducto(true)
  }

  const guardarProducto = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setErrorAccion("")
    const payload: ProductoPayload = {
      sku: form.sku.trim(),
      nombre: form.nombre.trim(),
      descripcion: form.descripcion.trim(),
      precio: Math.max(0, Number(form.precio) || 0),
      categoria: form.categoria || categorias[0]?.nombre || "Sin categoría",
      subcategoria: form.subcategoria.trim() || null,
      marca: form.marca.trim() || null,
      colores: productoEditado?.colores ?? [],
      imagen: form.imagen.trim() || null,
      stock: Math.max(0, Number(form.stock) || 0),
    }

    try {
      if (productoEditado) {
        await actualizar(productoEditado.id, payload)
        setMensaje("Producto actualizado.")
      } else {
        await crear(payload)
        setMensaje("Producto creado.")
      }
      setModalProducto(false)
      recargarAdmin()
    } catch (ex) {
      setErrorAccion(mensajeDeError(ex))
    }
  }

  const guardarStock = async (producto: Producto): Promise<void> => {
    setErrorAccion("")
    setMensaje("")
    try {
      const cantidad = Math.max(0, Number(stocks[producto.id] ?? producto.stock))
      await cambiarStock(producto.id, cantidad)
      setMensaje(`Stock de ${producto.nombre} actualizado a ${cantidad}.`)
      recargarAdmin()
    } catch (ex) {
      setErrorAccion(mensajeDeError(ex))
    }
  }

  const borrarProducto = async (producto: Producto): Promise<void> => {
    if (!window.confirm(`¿Eliminar "${producto.nombre}" del catálogo?`)) return
    setErrorAccion("")
    setMensaje("")
    try {
      await eliminar(producto.id)
      setMensaje("Producto eliminado.")
      recargarAdmin()
    } catch (ex) {
      setErrorAccion(mensajeDeError(ex))
    }
  }

  const cargarCsv = async (evento: ChangeEvent<HTMLInputElement>): Promise<void> => {
    const archivo = evento.target.files?.[0]
    if (!archivo) return
    setErrorAccion("")
    setMensaje("")
    try {
      const texto = await archivo.text()
      const resultado = await importarCsv(texto)
      setMensaje(
        `Importación terminada: ${resultado.agregados} agregados, ${resultado.actualizados} actualizados` +
          (resultado.errores.length ? `, ${resultado.errores.length} errores.` : ".")
      )
      recargarAdmin()
    } catch (ex) {
      setErrorAccion(mensajeDeError(ex))
    } finally {
      evento.target.value = ""
    }
  }

  const cambiarEstadoPedido = async (pedido: Pedido, estado: string): Promise<void> => {
    setCambiandoPedido(pedido.id)
    setErrorAccion("")
    setMensaje("")
    try {
      await pedidosService.cambiarEstado(pedido.id, estado)
      setMensaje(`Pedido ${pedido.numero} actualizado a ${estado.replace(/_/g, " ")}.`)
      recargarPedidos()
      recargarAdmin()
    } catch (ex) {
      setErrorAccion(mensajeDeError(ex))
    } finally {
      setCambiandoPedido(null)
    }
  }

  return (
    <section style={{ paddingTop: "1.5rem" }}>
      <div className="container">
        <nav aria-label="Ruta de navegación" className="migas">
          <Link to="/">Inicio</Link> / <span>Panel de administración</span>
        </nav>

        <h1 className="section-title">Panel de administración</h1>

        {(mensaje || errorAccion || errorAdmin || errorInventario || errorPedidos) && (
          <div style={{ marginBottom: "1rem" }}>
            {mensaje && <p className="alerta alerta-ok">{mensaje}</p>}
            {errorAccion && <p className="alerta alerta-error">{errorAccion}</p>}
            {errorAdmin && <p className="alerta alerta-error">Dashboard: {errorAdmin}</p>}
            {errorInventario && <p className="alerta alerta-error">Inventario: {errorInventario}</p>}
            {errorPedidos && <p className="alerta alerta-error">Pedidos: {errorPedidos}</p>}
          </div>
        )}

        <div className="admin-layout" style={{ minHeight: "auto" }}>
          <aside className="admin-sidebar">
            <nav aria-label="Secciones del panel">
              <ul>
                <li>
                  <a
                    href="#dashboard"
                    className={seccion === "dashboard" ? "active" : undefined}
                    aria-current={seccion === "dashboard" ? "page" : undefined}
                    onClick={(e) => {
                      e.preventDefault()
                      setSeccion("dashboard")
                    }}
                  >
                    Dashboard
                  </a>
                </li>
                <li>
                  <a
                    href="#inventario"
                    className={seccion === "inventario" ? "active" : undefined}
                    aria-current={seccion === "inventario" ? "page" : undefined}
                    onClick={(e) => {
                      e.preventDefault()
                      setSeccion("inventario")
                    }}
                  >
                    Inventario
                  </a>
                </li>
                <li>
                  <a
                    href="#pedidos"
                    className={seccion === "pedidos" ? "active" : undefined}
                    aria-current={seccion === "pedidos" ? "page" : undefined}
                    onClick={(e) => {
                      e.preventDefault()
                      setSeccion("pedidos")
                    }}
                  >
                    Pedidos
                  </a>
                </li>
              </ul>
            </nav>
          </aside>

          <div className="admin-main">
            {seccion === "dashboard" && (
              <section id="dashboard">
                <h2 className="section-title" style={{ fontSize: "1.3rem" }}>
                  Resumen general
                </h2>

                {cargandoAdmin && <p className="cargando">Cargando métricas…</p>}

                <div className="stat-cards">
                  <div className="stat-card">
                    <p className="value">{stock?.activos ?? 0}</p>
                    <p className="label">Productos activos</p>
                  </div>
                  <div className="stat-card alert">
                    <p className="value">{stock?.stockBajo ?? 0}</p>
                    <p className="label">Artículos con stock crítico (&lt; 5)</p>
                  </div>
                  <div className="stat-card">
                    <p className="value">{dashboard?.pedidosPendientes ?? 0}</p>
                    <p className="label">Pedidos por gestionar</p>
                  </div>
                  <div className="stat-card">
                    <p className="value">{formatoPrecio(dashboard?.ventasTotales ?? 0)}</p>
                    <p className="label">Ventas totales registradas</p>
                  </div>
                </div>

                <div className="admin-card">
                  <h2>Artículos críticos (stock bajo)</h2>
                  <div className="table-responsive-x">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>SKU</th>
                          <th>Producto</th>
                          <th>Stock</th>
                          <th>Estado</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(stock?.criticos ?? []).map((producto) => (
                          <tr key={producto.id} className="fila-stock-bajo">
                            <td>{producto.sku}</td>
                            <td>{producto.nombre}</td>
                            <td>
                              <span className="badge-stock bajo">
                                {producto.stock} unid.
                              </span>
                            </td>
                            <td>Reponer pronto</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {(stock?.criticos ?? []).length === 0 && (
                    <p className="form-note">
                      No hay artículos con stock crítico por el momento.
                    </p>
                  )}
                </div>

                <div className="admin-card" style={{ marginTop: "1.5rem" }}>
                  <h2>Productos más vendidos</h2>
                  <div className="table-responsive-x">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Producto</th>
                          <th>Unidades</th>
                          <th>Ingresos</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(dashboard?.productosMasVendidos ?? []).map((producto) => (
                          <tr key={producto.productoId}>
                            <td>{producto.nombre}</td>
                            <td>{producto.unidadesVendidas}</td>
                            <td>{formatoPrecio(producto.ingresos)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {(dashboard?.productosMasVendidos ?? []).length === 0 && (
                    <p className="form-note">Aún no hay ventas registradas.</p>
                  )}
                </div>
              </section>
            )}

            {seccion === "inventario" && (
              <section id="inventario">
                <div className="admin-toolbar">
                  <h2 className="section-title" style={{ fontSize: "1.3rem", margin: 0 }}>
                    Gestión de inventario
                  </h2>
                  <button type="button" className="btn btn-primary" onClick={abrirNuevo}>
                    + Nuevo producto
                  </button>
                </div>

                <form
                  className="filters"
                  onSubmit={(e) => e.preventDefault()}
                  aria-label="Buscar productos"
                >
                  <div style={{ flex: 1, minWidth: "220px" }}>
                    <label htmlFor="buscar-producto">Buscar por nombre o SKU</label>
                    <input
                      type="search"
                      id="buscar-producto"
                      placeholder="Ej: Cuaderno Miku o MIKU-001"
                      value={busqueda}
                      onChange={(e) => setBusqueda(e.target.value)}
                    />
                  </div>
                </form>

                <div className="import-box">
                  <div>
                    <label htmlFor="input-csv" style={{ fontWeight: 600, display: "block", marginBottom: "0.25rem" }}>
                      Carga masiva (CSV)
                    </label>
                    <span className="form-note" style={{ margin: 0 }}>
                      Columnas esperadas: SKU, Nombre, Categoría, Precio, Stock
                    </span>
                  </div>
                  <input
                    type="file"
                    id="input-csv"
                    accept=".csv"
                    onChange={(e) => void cargarCsv(e)}
                    disabled={guardando}
                  />
                </div>

                <div className="admin-card">
                  {cargandoInventario && <p className="cargando">Cargando productos…</p>}
                  <div className="table-responsive-x">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>SKU</th>
                          <th>Producto</th>
                          <th>Categoría</th>
                          <th>Precio</th>
                          <th>Stock</th>
                          <th>Estado</th>
                          <th>Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productosFiltrados.map((producto) => {
                          const badge = badgeStock(producto.stock)
                          const sinStock = producto.stock <= 0 || !producto.activo
                          return (
                            <tr
                              key={producto.id}
                              className={
                                sinStock
                                  ? "fila-sin-stock"
                                  : producto.stock < 5
                                    ? "fila-stock-bajo"
                                    : undefined
                              }
                            >
                              <td>{producto.sku}</td>
                              <td>
                                <span style={{ display: "inline-flex", alignItems: "center", gap: "0.5rem" }}>
                                  <img
                                    src={imagenProducto(producto)}
                                    alt=""
                                    style={{ width: 34, height: 34, objectFit: "cover", borderRadius: 6 }}
                                  />
                                  {producto.nombre}
                                </span>
                              </td>
                              <td>{producto.categoria}</td>
                              <td>{formatoPrecio(producto.precio)}</td>
                              <td>
                                <input
                                  type="number"
                                  className="input-stock"
                                  min={0}
                                  value={stocks[producto.id] ?? producto.stock}
                                  onChange={(e) =>
                                    setStocks((actual) => ({
                                      ...actual,
                                      [producto.id]: Math.max(0, Number(e.target.value) || 0),
                                    }))
                                  }
                                  aria-label={`Stock de ${producto.nombre}`}
                                />
                              </td>
                              <td>
                                <span className={badge.clase}>{badge.texto}</span>
                                {!producto.activo && (
                                  <span className="form-note" style={{ margin: 0 }}>
                                    {" "}
                                    (oculto en catálogo)
                                  </span>
                                )}
                              </td>
                              <td className="acciones-cell">
                                <button
                                  type="button"
                                  className="btn btn-secondary btn-sm"
                                  disabled={guardando}
                                  onClick={() => void guardarStock(producto)}
                                >
                                  Guardar stock
                                </button>
                                <button
                                  type="button"
                                  className="btn btn-secondary btn-sm"
                                  onClick={() => abrirEditar(producto)}
                                >
                                  Editar
                                </button>
                                <button
                                  type="button"
                                  className="btn btn-danger-outline btn-sm"
                                  disabled={guardando}
                                  onClick={() => void borrarProducto(producto)}
                                >
                                  Eliminar
                                </button>
                              </td>
                            </tr>
                          )
                        })}
                      </tbody>
                    </table>
                  </div>
                  {!cargandoInventario && productosFiltrados.length === 0 && (
                    <p className="form-note">No se encontraron productos con ese criterio de búsqueda.</p>
                  )}
                </div>
              </section>
            )}

            {seccion === "pedidos" && (
              <section id="pedidos">
                <h2 className="section-title" style={{ fontSize: "1.3rem" }}>
                  Gestión de pedidos
                </h2>

                <form className="filters" onSubmit={(e) => e.preventDefault()}>
                  <div>
                    <label htmlFor="filtro-estado">Estado</label>
                    <select
                      id="filtro-estado"
                      value={filtroEstado}
                      onChange={(e) => setFiltroEstado(e.target.value)}
                    >
                      <option value="">Todos</option>
                      {ESTADOS_PEDIDO.map((estado) => (
                        <option key={estado} value={estado}>
                          {estado.replace(/_/g, " ")}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label htmlFor="filtro-desde">Desde</label>
                    <input
                      type="date"
                      id="filtro-desde"
                      value={desde}
                      onChange={(e) => setDesde(e.target.value)}
                    />
                  </div>
                  <div>
                    <label htmlFor="filtro-hasta">Hasta</label>
                    <input
                      type="date"
                      id="filtro-hasta"
                      value={hasta}
                      onChange={(e) => setHasta(e.target.value)}
                    />
                  </div>
                  <div style={{ alignSelf: "flex-end" }}>
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => {
                        setFiltroEstado("")
                        setDesde("")
                        setHasta("")
                      }}
                    >
                      Limpiar filtros
                    </button>
                  </div>
                </form>

                <div className="admin-card">
                  {cargandoPedidos && <p className="cargando">Cargando pedidos…</p>}
                  <div className="table-responsive-x">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Pedido</th>
                          <th>Cliente</th>
                          <th>Fecha</th>
                          <th>Entrega</th>
                          <th>Total</th>
                          <th>Estado</th>
                          <th>Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {pedidos.map((pedido) => (
                          <tr key={pedido.id}>
                            <td>{pedido.numero}</td>
                            <td>{pedido.usuario}</td>
                            <td>{formatearFecha(pedido.fecha)}</td>
                            <td>{pedido.entrega}</td>
                            <td>{formatoPrecio(pedido.total)}</td>
                            <td>
                              <span className={claseEstado(pedido.estado)}>
                                {(pedido.estadoCliente || pedido.estado).replace(/_/g, " ")}
                              </span>
                            </td>
                            <td className="acciones-cell">
                              <select
                                className="form-select form-select-sm"
                                value={pedido.estado}
                                disabled={cambiandoPedido === pedido.id}
                                onChange={(e) => void cambiarEstadoPedido(pedido, e.target.value)}
                                aria-label={`Cambiar estado del pedido ${pedido.numero}`}
                              >
                                {ESTADOS_PEDIDO.map((estado) => (
                                  <option key={estado} value={estado}>
                                    {estado.replace(/_/g, " ")}
                                  </option>
                                ))}
                              </select>
                              <button
                                type="button"
                                className="btn btn-secondary btn-sm"
                                onClick={() => setPedidoDetalle(pedido)}
                              >
                                Ver detalle
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {!cargandoPedidos && pedidos.length === 0 && (
                    <p className="form-note">
                      No hay pedidos que coincidan con los filtros seleccionados.
                    </p>
                  )}
                </div>
              </section>
            )}
          </div>
        </div>
      </div>

      {modalProducto && (
        <>
          <div className="modal-backdrop-custom" onClick={() => setModalProducto(false)} />
          <div className="modal fade show d-block" tabIndex={-1} role="dialog" aria-modal="true">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <form onSubmit={(e) => void guardarProducto(e)}>
                  <div className="modal-header">
                    <h5 className="modal-title">
                      {productoEditado ? "Editar producto" : "Nuevo producto"}
                    </h5>
                    <button
                      type="button"
                      className="btn-close"
                      aria-label="Cerrar"
                      onClick={() => setModalProducto(false)}
                    />
                  </div>
                  <div className="modal-body">
                    {errorAccion && <p className="alerta alerta-error">{errorAccion}</p>}

                    <div className="form-field">
                      <label htmlFor="producto-nombre">Nombre</label>
                      <input
                        type="text"
                        id="producto-nombre"
                        value={form.nombre}
                        onChange={(e) => setForm((f) => ({ ...f, nombre: e.target.value }))}
                        required
                      />
                    </div>
                    <div className="form-field">
                      <label htmlFor="producto-sku">SKU</label>
                      <input
                        type="text"
                        id="producto-sku"
                        value={form.sku}
                        onChange={(e) => setForm((f) => ({ ...f, sku: e.target.value }))}
                        required
                      />
                    </div>
                    <div className="form-field">
                      <label htmlFor="producto-categoria">Categoría</label>
                      <select
                        id="producto-categoria"
                        value={form.categoria}
                        onChange={(e) => setForm((f) => ({ ...f, categoria: e.target.value }))}
                        required
                      >
                        {categorias.map((categoria) => (
                          <option key={categoria.nombre} value={categoria.nombre}>
                            {categoria.nombre}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="form-field">
                      <label htmlFor="producto-subcategoria">Subcategoría (opcional)</label>
                      <input
                        type="text"
                        id="producto-subcategoria"
                        value={form.subcategoria}
                        onChange={(e) => setForm((f) => ({ ...f, subcategoria: e.target.value }))}
                      />
                    </div>
                    <div className="form-field">
                      <label htmlFor="producto-marca">Marca (opcional)</label>
                      <input
                        type="text"
                        id="producto-marca"
                        value={form.marca}
                        onChange={(e) => setForm((f) => ({ ...f, marca: e.target.value }))}
                      />
                    </div>
                    <div className="form-field">
                      <label htmlFor="producto-descripcion">Descripción</label>
                      <textarea
                        id="producto-descripcion"
                        rows={3}
                        value={form.descripcion}
                        onChange={(e) => setForm((f) => ({ ...f, descripcion: e.target.value }))}
                      />
                    </div>
                    <div style={{ display: "flex", gap: "1rem" }}>
                      <div className="form-field" style={{ flex: 1 }}>
                        <label htmlFor="producto-precio">Precio (CLP)</label>
                        <input
                          type="number"
                          id="producto-precio"
                          min={0}
                          value={form.precio}
                          onChange={(e) => setForm((f) => ({ ...f, precio: e.target.value }))}
                          required
                        />
                      </div>
                      <div className="form-field" style={{ flex: 1 }}>
                        <label htmlFor="producto-stock">Stock</label>
                        <input
                          type="number"
                          id="producto-stock"
                          min={0}
                          value={form.stock}
                          onChange={(e) => setForm((f) => ({ ...f, stock: e.target.value }))}
                          required
                        />
                      </div>
                    </div>
                    <div className="form-field">
                      <label htmlFor="producto-imagen">Imagen (ruta o URL)</label>
                      <input
                        type="text"
                        id="producto-imagen"
                        value={form.imagen}
                        onChange={(e) => setForm((f) => ({ ...f, imagen: e.target.value }))}
                        placeholder="/img/producto.jpg"
                      />
                    </div>
                  </div>
                  <div className="modal-footer">
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => setModalProducto(false)}
                    >
                      Cancelar
                    </button>
                    <button type="submit" className="btn btn-primary" disabled={guardando}>
                      {guardando ? "Guardando…" : "Guardar producto"}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </>
      )}

      {pedidoDetalle && (
        <>
          <div className="modal-backdrop-custom" onClick={() => setPedidoDetalle(null)} />
          <div className="modal fade show d-block" tabIndex={-1} role="dialog" aria-modal="true">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Detalle del pedido {pedidoDetalle.numero}</h5>
                  <button
                    type="button"
                    className="btn-close"
                    aria-label="Cerrar"
                    onClick={() => setPedidoDetalle(null)}
                  />
                </div>
                <div className="modal-body">
                  <p>
                    <strong>Cliente:</strong> {pedidoDetalle.usuario}
                  </p>
                  <p>
                    <strong>Fecha:</strong> {formatearFecha(pedidoDetalle.fecha)}
                  </p>
                  <p>
                    <strong>Entrega:</strong> {pedidoDetalle.entrega} ·{" "}
                    {pedidoDetalle.direccionDespacho}
                  </p>
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Precio</th>
                        <th>Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pedidoDetalle.items.map((item) => (
                        <tr key={item.productoId}>
                          <td>{item.nombreProducto}</td>
                          <td>{item.cantidad}</td>
                          <td>{formatoPrecio(item.precioUnitario)}</td>
                          <td>{formatoPrecio(item.subtotal)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <p style={{ textAlign: "right", fontWeight: 700, marginTop: "0.75rem" }}>
                    Total: {formatoPrecio(pedidoDetalle.total)}
                  </p>
                </div>
                <div className="modal-footer">
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setPedidoDetalle(null)}
                  >
                    Cerrar
                  </button>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </section>
  )
}
