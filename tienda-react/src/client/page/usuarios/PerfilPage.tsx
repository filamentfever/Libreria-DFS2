// Perfil del cliente: datos, historial de compras y direcciones (MSCI-USUARIOS).
import { useEffect, useState, type FormEvent } from "react"
import { Link } from "react-router"
import { useUsuarios } from "@/hooks/useUsuarios"
import { usePedidos } from "@/hooks/usePedidos"
import { formatoPrecio } from "@/services/catalogoService"
import { mensajeDeError } from "@/services/api"

function formatearFecha(fecha: string): string {
  const [anio, mes, dia] = fecha.split("-")
  return dia && mes && anio ? `${dia}-${mes}-${anio}` : fecha
}

function claseEstado(estado: string): string {
  return `badge-estado st-${estado.toLowerCase().replace(/_/g, "-")}`
}

function iniciales(nombre?: string): string {
  if (!nombre) return "LJ"
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((parte) => parte[0]?.toUpperCase() ?? "")
    .join("")
}

export const PerfilPage = () => {
  const {
    usuario,
    direcciones,
    cargando,
    actualizar,
    agregarDireccion,
    eliminarDireccion,
    marcarPredeterminada,
  } = useUsuarios()
  const { pedidos, cargando: cargandoPedidos } = usePedidos(
    usuario?.username,
    {},
    Boolean(usuario)
  )

  const [form, setForm] = useState({ nombre: "", email: "", telefono: "" })
  const [aviso, setAviso] = useState("")
  const [error, setError] = useState("")
  const [guardando, setGuardando] = useState(false)
  const [nuevaDireccion, setNuevaDireccion] = useState({
    alias: "",
    direccion: "",
    comuna: "",
    predeterminada: false,
  })
  const [avisoDireccion, setAvisoDireccion] = useState("")
  const [errorDireccion, setErrorDireccion] = useState("")
  const [seccionActiva, setSeccionActiva] = useState("datos-personales")

  // Resalta en la navegación la sección que se está viendo al hacer scroll.
  useEffect(() => {
    const ids = ["datos-personales", "historial-compras", "direcciones"]
    const nodos = ids
      .map((id) => document.getElementById(id))
      .filter((nodo): nodo is HTMLElement => Boolean(nodo))
    if (!nodos.length) return

    const observador = new IntersectionObserver(
      (entradas) => {
        const visibles = entradas
          .filter((entrada) => entrada.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
        if (visibles[0]) setSeccionActiva(visibles[0].target.id)
      },
      { rootMargin: "-35% 0px -55% 0px", threshold: 0 }
    )

    nodos.forEach((nodo) => observador.observe(nodo))
    return () => observador.disconnect()
  }, [cargando])

  useEffect(() => {
    if (!usuario) return
    setForm({
      nombre: usuario.nombre,
      email: usuario.email,
      telefono: usuario.telefono ?? "",
    })
  }, [usuario])

  const guardarPerfil = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setAviso("")
    setError("")
    setGuardando(true)
    try {
      await actualizar(form)
      setAviso("Datos personales actualizados.")
    } catch (ex) {
      setError(mensajeDeError(ex))
    } finally {
      setGuardando(false)
    }
  }

  const guardarDireccion = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setAvisoDireccion("")
    setErrorDireccion("")
    if (!nuevaDireccion.alias.trim() || !nuevaDireccion.direccion.trim() || !nuevaDireccion.comuna.trim()) {
      setErrorDireccion("Completa etiqueta, dirección y comuna.")
      return
    }
    try {
      await agregarDireccion(nuevaDireccion)
      setNuevaDireccion({ alias: "", direccion: "", comuna: "", predeterminada: false })
      setAvisoDireccion("Dirección agregada.")
    } catch (ex) {
      setErrorDireccion(mensajeDeError(ex))
    }
  }

  const borrarDireccion = async (id: number): Promise<void> => {
    if (!window.confirm("¿Eliminar esta dirección?")) return
    setErrorDireccion("")
    try {
      await eliminarDireccion(id)
    } catch (ex) {
      setErrorDireccion(mensajeDeError(ex))
    }
  }

  const usarComoPredeterminada = async (id: number): Promise<void> => {
    setErrorDireccion("")
    try {
      await marcarPredeterminada(id)
    } catch (ex) {
      setErrorDireccion(mensajeDeError(ex))
    }
  }

  const totalComprado = pedidos.reduce((suma, pedido) => suma + pedido.total, 0)

  return (
    <section className="cp-profile">
      <div className="container">
        <nav aria-label="Ruta de navegación" className="migas">
          <Link to="/">Inicio</Link> / <span>Mi perfil</span>
        </nav>

        <header className="cp-profile-hero">
          <div className="cp-profile-identity">
            <span className="cp-profile-avatar" aria-hidden>
              {iniciales(usuario?.nombre)}
            </span>
            <div className="cp-profile-who">
              <p className="cp-profile-kicker">Mi cuenta</p>
              <h1>{usuario?.nombre ?? "Mi perfil"}</h1>
              <p className="cp-profile-meta">
                {usuario?.email ?? "Cargando tu cuenta…"}
                {usuario?.username ? ` · @${usuario.username}` : ""}
              </p>
            </div>
          </div>

          <dl className="cp-profile-stats">
            <div>
              <dt>Pedidos</dt>
              <dd>{pedidos.length}</dd>
            </div>
            <div>
              <dt>Direcciones</dt>
              <dd>{direcciones.length}</dd>
            </div>
            <div>
              <dt>Total comprado</dt>
              <dd>{formatoPrecio(totalComprado)}</dd>
            </div>
          </dl>
        </header>

        <nav className="cp-profile-nav" aria-label="Secciones del perfil">
          <a
            href="#datos-personales"
            className={seccionActiva === "datos-personales" ? "activo" : undefined}
          >
            Datos personales
          </a>
          <a
            href="#historial-compras"
            className={seccionActiva === "historial-compras" ? "activo" : undefined}
          >
            Historial de compras
          </a>
          <a
            href="#direcciones"
            className={seccionActiva === "direcciones" ? "activo" : undefined}
          >
            Direcciones de despacho
          </a>
        </nav>

        {cargando && <p className="cargando">Cargando tu cuenta…</p>}

        <div className="cp-profile-content">
          <section id="datos-personales" className="cp-panel">
            <div className="cp-panel-head">
              <div>
                <p className="cp-panel-kicker">Cuenta</p>
                <h2>Datos personales</h2>
                <p className="cp-panel-desc">
                  Revisa y actualiza la información con la que te contactamos.
                </p>
              </div>
            </div>

            {aviso && <p className="alerta alerta-ok">{aviso}</p>}
            {error && <p className="alerta alerta-error">{error}</p>}

            <form className="cp-form" onSubmit={(e) => void guardarPerfil(e)}>
              <div className="cp-form-grid">
                <div className="cp-field cp-field--wide">
                  <label htmlFor="perfil-nombre">Nombre completo</label>
                  <input
                    type="text"
                    id="perfil-nombre"
                    value={form.nombre}
                    onChange={(e) => setForm((f) => ({ ...f, nombre: e.target.value }))}
                    required
                  />
                </div>

                <div className="cp-field">
                  <label htmlFor="perfil-usuario">Usuario</label>
                  <input
                    type="text"
                    id="perfil-usuario"
                    value={usuario?.username ?? ""}
                    readOnly
                  />
                </div>

                <div className="cp-field">
                  <label htmlFor="perfil-rut">RUT</label>
                  <input type="text" id="perfil-rut" value={usuario?.rut ?? ""} readOnly />
                </div>

                <div className="cp-field">
                  <label htmlFor="perfil-email">Correo electrónico</label>
                  <input
                    type="email"
                    id="perfil-email"
                    value={form.email}
                    onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                    required
                  />
                </div>

                <div className="cp-field">
                  <label htmlFor="perfil-telefono">Teléfono</label>
                  <input
                    type="tel"
                    id="perfil-telefono"
                    value={form.telefono}
                    onChange={(e) => setForm((f) => ({ ...f, telefono: e.target.value }))}
                  />
                </div>

                <div className="cp-field cp-field--wide">
                  <label htmlFor="perfil-password">Nueva contraseña</label>
                  <input
                    type="password"
                    id="perfil-password"
                    placeholder="Disponible próximamente"
                    disabled
                  />
                  <p className="cp-field-hint">
                    Por ahora puedes recuperar tu contraseña desde el panel de tu cuenta.
                  </p>
                </div>
              </div>

              <div className="cp-form-actions">
                <button
                  type="submit"
                  className="cp-btn cp-btn--solid"
                  disabled={guardando || !usuario}
                >
                  {guardando ? "Guardando…" : "Guardar cambios"}
                </button>
              </div>
            </form>
          </section>

          <section id="historial-compras" className="cp-panel">
            <div className="cp-panel-head">
              <div>
                <p className="cp-panel-kicker">Tus pedidos</p>
                <h2>Historial de compras</h2>
                <p className="cp-panel-desc">
                  Revisa el estado y el detalle de cada compra que hiciste en la librería.
                </p>
              </div>
              <Link to="/pedidos" className="cp-panel-link">
                Ver todos los pedidos
              </Link>
            </div>

            {cargandoPedidos && <p className="cargando">Cargando pedidos…</p>}

            {!cargandoPedidos && pedidos.length === 0 ? (
              <p className="cp-empty">Aún no tienes compras registradas.</p>
            ) : (
              <div className="cp-table-wrap">
                <table className="cp-table">
                  <thead>
                    <tr>
                      <th scope="col">Pedido</th>
                      <th scope="col">Fecha</th>
                      <th scope="col">Productos</th>
                      <th scope="col" className="cp-num">Total</th>
                      <th scope="col">Estado</th>
                      <th scope="col">
                        <span className="cp-sr">Acciones</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {pedidos.map((pedido) => (
                      <tr key={pedido.id}>
                        <td className="cp-strong">{pedido.numero}</td>
                        <td>{formatearFecha(pedido.fecha)}</td>
                        <td>{pedido.items.map((item) => item.nombreProducto).join(", ")}</td>
                        <td className="cp-num cp-strong">{formatoPrecio(pedido.total)}</td>
                        <td>
                          <span className={claseEstado(pedido.estado)}>
                            {pedido.estadoCliente || pedido.estado}
                          </span>
                        </td>
                        <td className="cp-cell-action">
                          <Link to="/pedidos" className="cp-table-link">
                            Ver detalle
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section id="direcciones" className="cp-panel">
            <div className="cp-panel-head">
              <div>
                <p className="cp-panel-kicker">Despachos</p>
                <h2>Direcciones de despacho</h2>
                <p className="cp-panel-desc">
                  Guarda tus direcciones para agilizar tus próximas compras.
                </p>
              </div>
            </div>

            {avisoDireccion && <p className="alerta alerta-ok">{avisoDireccion}</p>}
            {errorDireccion && <p className="alerta alerta-error">{errorDireccion}</p>}

            {direcciones.length === 0 ? (
              <p className="cp-empty">Todavía no tienes direcciones guardadas.</p>
            ) : (
              <div className="cp-address-grid">
                {direcciones.map((direccion) => (
                  <article className="cp-address" key={direccion.id}>
                    <div className="cp-address-top">
                      <h3>{direccion.alias}</h3>
                      {direccion.predeterminada && (
                        <span className="cp-badge-default">Predeterminada</span>
                      )}
                    </div>
                    <p>{direccion.direccion}</p>
                    <p>{direccion.comuna}</p>
                    <div className="cp-address-actions">
                      {!direccion.predeterminada && (
                        <button
                          type="button"
                          className="cp-link-action"
                          onClick={() => void usarComoPredeterminada(direccion.id)}
                        >
                          Usar como predeterminada
                        </button>
                      )}
                      <button
                        type="button"
                        className="cp-link-action cp-link-action--danger"
                        onClick={() => void borrarDireccion(direccion.id)}
                      >
                        Eliminar
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}

            <div className="cp-subpanel">
              <h3>Agregar nueva dirección</h3>
              <form className="cp-form" onSubmit={(e) => void guardarDireccion(e)}>
                <div className="cp-form-grid">
                  <div className="cp-field">
                    <label htmlFor="dir-etiqueta">Etiqueta (Casa, Trabajo, etc.)</label>
                    <input
                      type="text"
                      id="dir-etiqueta"
                      value={nuevaDireccion.alias}
                      onChange={(e) =>
                        setNuevaDireccion((d) => ({ ...d, alias: e.target.value }))
                      }
                      required
                    />
                  </div>

                  <div className="cp-field">
                    <label htmlFor="dir-comuna">Comuna</label>
                    <input
                      type="text"
                      id="dir-comuna"
                      value={nuevaDireccion.comuna}
                      onChange={(e) =>
                        setNuevaDireccion((d) => ({ ...d, comuna: e.target.value }))
                      }
                      required
                    />
                  </div>

                  <div className="cp-field cp-field--wide">
                    <label htmlFor="dir-direccion">Dirección</label>
                    <input
                      type="text"
                      id="dir-direccion"
                      placeholder="Calle, número, departamento"
                      value={nuevaDireccion.direccion}
                      onChange={(e) =>
                        setNuevaDireccion((d) => ({ ...d, direccion: e.target.value }))
                      }
                      required
                    />
                  </div>
                </div>

                <label htmlFor="dir-predeterminada" className="cp-check">
                  <input
                    type="checkbox"
                    id="dir-predeterminada"
                    checked={nuevaDireccion.predeterminada}
                    onChange={(e) =>
                      setNuevaDireccion((d) => ({
                        ...d,
                        predeterminada: e.target.checked,
                      }))
                    }
                  />
                  <span>Usar como dirección predeterminada</span>
                </label>

                <div className="cp-form-actions">
                  <button type="submit" className="cp-btn cp-btn--solid">
                    Guardar dirección
                  </button>
                </div>
              </form>
            </div>
          </section>
        </div>
      </div>
    </section>
  )
}
