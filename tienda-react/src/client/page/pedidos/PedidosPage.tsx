// Historial de compras del cliente (MSCI-PEDIDOS).
import { Fragment, useState } from "react"
import { Link } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { usePedidos } from "@/hooks/usePedidos"
import { formatoPrecio } from "@/services/catalogoService"

function formatearFecha(fecha: string): string {
  const [anio, mes, dia] = fecha.split("-")
  return dia && mes && anio ? `${dia}-${mes}-${anio}` : fecha
}

function claseEstado(estado: string): string {
  return `badge-estado st-${estado.toLowerCase().replace(/_/g, "-")}`
}

export const PedidosPage = () => {
  const { usuario, cargando: cargandoSesion } = useCarrito()
  const { pedidos, cargando, error } = usePedidos(usuario, {}, Boolean(usuario))
  const [detalleId, setDetalleId] = useState<number | null>(null)

  return (
    <section>
      <div className="container">
        <nav aria-label="Ruta de navegación" className="migas">
          <Link to="/">Inicio</Link> / <span>Mis pedidos</span>
        </nav>

        <h1 className="section-title">Historial de compras</h1>

        {cargandoSesion && <p className="cargando">Cargando tu cuenta…</p>}
        {cargando && <p className="cargando">Cargando tus pedidos…</p>}
        {error && <p className="alerta alerta-error">No se pudieron cargar los pedidos: {error}</p>}

        {!cargando && pedidos.length === 0 && (
          <div className="carrito-vacio">
            <p>Todavía no tienes pedidos registrados.</p>
            <Link to="/catalogo" className="btn btn-primary">
              Ir al catálogo
            </Link>
          </div>
        )}

        {pedidos.length > 0 && (
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Pedido</th>
                <th scope="col">Fecha</th>
                <th scope="col">Productos</th>
                <th scope="col">Total</th>
                <th scope="col">Estado</th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              {pedidos.map((pedido) => (
                <Fragment key={pedido.id}>
                  <tr>
                    <td>{pedido.numero}</td>
                    <td>{formatearFecha(pedido.fecha)}</td>
                    <td>{pedido.items.map((item) => item.nombreProducto).join(", ")}</td>
                    <td>{formatoPrecio(pedido.total)}</td>
                    <td>
                      <span className={claseEstado(pedido.estado)}>
                        {pedido.estadoCliente || pedido.estado}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn-quitar"
                        onClick={() =>
                          setDetalleId((actual) => (actual === pedido.id ? null : pedido.id))
                        }
                      >
                        {detalleId === pedido.id ? "Ocultar" : "Ver detalle"}
                      </button>
                    </td>
                  </tr>

                  {detalleId === pedido.id && (
                    <tr>
                      <td colSpan={6}>
                        <div className="detalle-pedido">
                          <p>
                            <strong>Entrega:</strong> {pedido.entrega} ·{" "}
                            {pedido.direccionDespacho}
                          </p>
                          <p>
                            <strong>Medio de pago:</strong> {pedido.medioPago}
                            {pedido.cupon ? ` · Cupón ${pedido.cupon}` : ""}
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
                              {pedido.items.map((item) => (
                                <tr key={`${pedido.id}-${item.productoId}`}>
                                  <td>{item.nombreProducto}</td>
                                  <td>{item.cantidad}</td>
                                  <td>{formatoPrecio(item.precioUnitario)}</td>
                                  <td>{formatoPrecio(item.subtotal)}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                          <p className="detalle-total">
                            Envío: {formatoPrecio(pedido.envio)} · IVA incluido:{" "}
                            {formatoPrecio(pedido.iva)} · <strong>Total: {formatoPrecio(pedido.total)}</strong>
                          </p>
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  )
}
