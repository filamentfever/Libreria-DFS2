// Carrito conectado a MSCI-CARRITO + checkout real contra MSCI-PEDIDOS.
import { useMemo, useState } from "react"
import { Link } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { CheckoutModal } from "@/client/components/CheckoutModal"
import { ProductoCard } from "@/client/components/ProductoCard"
import { useProductos } from "@/hooks/useProductos"
import { formatoPrecio, imagenProducto } from "@/services/catalogoService"
import type { Pedido } from "@/services/pedidosService"

export const CarritoPage = () => {
  const {
    items,
    cantidadTotal,
    total,
    cargando,
    error,
    cambiarCantidad,
    quitar,
    vaciar,
  } = useCarrito()
  const { productos } = useProductos({ tamano: 100 })
  const [procesando, setProcesando] = useState<number | null>(null)
  const [vaciando, setVaciando] = useState(false)
  const [modalAbierto, setModalAbierto] = useState(false)
  const [avisoPedido, setAvisoPedido] = useState<Pedido | null>(null)

  const imagenes = useMemo(() => {
    const mapa = new Map<number, string>()
    for (const producto of productos) {
      mapa.set(producto.id, imagenProducto(producto))
    }
    return mapa
  }, [productos])

  const manejarCantidad = async (itemId: number, cantidad: number): Promise<void> => {
    if (cantidad < 1) return
    setProcesando(itemId)
    try {
      await cambiarCantidad(itemId, cantidad)
    } finally {
      setProcesando(null)
    }
  }

  const manejarQuitar = async (itemId: number): Promise<void> => {
    setProcesando(itemId)
    try {
      await quitar(itemId)
    } finally {
      setProcesando(null)
    }
  }

  const manejarVaciar = async (): Promise<void> => {
    if (!window.confirm("¿Vaciar todos los productos del carrito?")) return
    setVaciando(true)
    try {
      await vaciar()
    } finally {
      setVaciando(false)
    }
  }

  return (
    <section>
      <div className="container">
        <nav aria-label="Ruta de navegación" className="migas">
          <Link to="/">Inicio</Link> / <span>Carrito</span>
        </nav>

        <h1 className="section-title">Tu carrito de compras</h1>

        {avisoPedido && (
          <p className="alerta alerta-ok">
            Pedido <strong>{avisoPedido.numero}</strong> creado correctamente por{" "}
            {formatoPrecio(avisoPedido.total)}. Puedes revisarlo en{" "}
            <Link to="/pedidos">Mis pedidos</Link>.
          </p>
        )}

        {error && <p className="alerta alerta-error">{error}</p>}

        {cargando && <p className="cargando">Preparando tu carrito…</p>}

        {!cargando && items.length === 0 && (
          <div className="carrito-vacio">
            <p>Tu carrito está vacío por ahora.</p>
            <Link to="/catalogo" className="btn btn-primary">
              Ir al catálogo
            </Link>
          </div>
        )}

        {!cargando && items.length > 0 && (
          <>
            <div style={{ overflowX: "auto" }}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Cantidad</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((item) => {
                    const ocupado = procesando === item.id
                    const imagen = imagenes.get(item.productoId)
                    return (
                      <tr key={item.id}>
                        <td>
                          <div className="item-carrito">
                            {imagen ? (
                              <img src={imagen} alt={item.nombreProducto} />
                            ) : (
                              <span className="item-carrito-placeholder">📦</span>
                            )}
                            <div>
                              <span className="item-nombre">{item.nombreProducto}</span>
                              <span className="item-precio">
                                {formatoPrecio(item.precioUnitario)} c/u
                              </span>
                            </div>
                          </div>
                        </td>
                        <td>
                          <div className="control-cantidad">
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              aria-label={`Quitar una unidad de ${item.nombreProducto}`}
                              disabled={ocupado || item.cantidad <= 1}
                              onClick={() => void manejarCantidad(item.id, item.cantidad - 1)}
                            >
                              −
                            </button>
                            <span>{item.cantidad}</span>
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              aria-label={`Agregar una unidad de ${item.nombreProducto}`}
                              disabled={ocupado}
                              onClick={() => void manejarCantidad(item.id, item.cantidad + 1)}
                            >
                              +
                            </button>
                          </div>
                          <span className="item-subtotal">{formatoPrecio(item.subtotal)}</span>
                        </td>
                        <td>
                          <button
                            type="button"
                            className="btn-quitar"
                            disabled={ocupado}
                            onClick={() => void manejarQuitar(item.id)}
                          >
                            Quitar
                          </button>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            <div className="carrito-resumen-wrap">
              <div className="carrito-resumen">
                <div className="checkout-total-row">
                  <span>Artículos</span>
                  <span>{cantidadTotal}</span>
                </div>
                <div className="checkout-total-row total">
                  <span>Total</span>
                  <span>{formatoPrecio(total)}</span>
                </div>
                <div className="carrito-acciones">
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={() => setModalAbierto(true)}
                  >
                    Proceder al pago
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => void manejarVaciar()}
                    disabled={vaciando}
                  >
                    {vaciando ? "Vaciando…" : "Vaciar carrito"}
                  </button>
                </div>
              </div>
            </div>
          </>
        )}

        <section id="seguir-comprando">
          <h2 className="section-title">Seguir comprando</h2>
          <div className="product-grid">
            {productos.slice(0, 4).map((producto) => (
              <ProductoCard key={producto.id} producto={producto} />
            ))}
          </div>
        </section>
      </div>

      <CheckoutModal
        abierto={modalAbierto}
        onCerrar={() => setModalAbierto(false)}
        onExito={(pedido) => {
          setAvisoPedido(pedido)
        }}
      />
    </section>
  )
}
