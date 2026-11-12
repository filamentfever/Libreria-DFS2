// Modal de pago del carrito: entrega, cupón, tarjeta/transferencia y checkout real
// contra MSCI-PEDIDOS (que orquesta carrito, catálogo y pagos).
import { useEffect, useMemo, useState, type FormEvent } from "react"
import { Link } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { useUsuarios } from "@/hooks/useUsuarios"
import { usePagos } from "@/hooks/usePagos"
import { useCotizacionEnvio } from "@/hooks/useEnvios"
import { pedidosService } from "@/services/pedidosService"
import type { CheckoutPayload, MedioPago, Pedido, TipoEntrega } from "@/services/pedidosService"
import { formatoPrecio } from "@/services/catalogoService"
import { mensajeDeError } from "@/services/api"

interface Props {
  abierto: boolean
  onCerrar: () => void
  onExito?: (pedido: Pedido) => void
}

const COMUNAS = [
  "Concepción",
  "Talcahuano",
  "San Pedro de la Paz",
  "Chiguayante",
  "Hualpén",
  "Santiago",
  "Providencia",
  "Las Condes",
  "Valparaíso",
  "Temuco",
]

type Paso = "formulario" | "procesando" | "resultado"

export function CheckoutModal({ abierto, onCerrar, onExito }: Props) {
  const { carrito, items, total, recargar } = useCarrito()
  const { direcciones } = useUsuarios()
  const { cupon, validarCupon, datosTransferencia, error: errorPago } = usePagos()
  const [paso, setPaso] = useState<Paso>("formulario")
  const [medioPago, setMedioPago] = useState<MedioPago>("TARJETA")
  const [entrega, setEntrega] = useState<TipoEntrega>("RETIRO")
  const [sucursal, setSucursal] = useState("Sucursal Centro")
  const [comuna, setComuna] = useState("Concepción")
  const [direccion, setDireccion] = useState("")
  const [cuponCodigo, setCuponCodigo] = useState("")
  const [tarjeta, setTarjeta] = useState({
    titular: "",
    numero: "",
    vencimiento: "",
    cvv: "",
  })
  const [error, setError] = useState("")
  const [pedido, setPedido] = useState<Pedido | null>(null)

  const { cotizacion, cargando: cotizando, error: errorCotizacion } = useCotizacionEnvio(
    entrega === "DOMICILIO" ? comuna : ""
  )

  const direccionPredeterminada = useMemo(
    () => direcciones.find((d) => d.predeterminada) ?? direcciones[0] ?? null,
    [direcciones]
  )

  useEffect(() => {
    if (!direccionPredeterminada) return
    setDireccion((actual) => actual || direccionPredeterminada.direccion)
    setComuna((actual) =>
      actual && actual !== "Concepción" ? actual : direccionPredeterminada.comuna || actual
    )
  }, [direccionPredeterminada])

  useEffect(() => {
    if (!abierto) return
    setPaso("formulario")
    setError("")
    setPedido(null)
    setCuponCodigo("")
  }, [abierto])

  const descuento = cupon?.valido
    ? Math.round((total * cupon.porcentaje) / 100)
    : 0
  const costoEnvio = entrega === "DOMICILIO" ? (cotizacion?.costo ?? 0) : 0
  const totalFinal = Math.max(0, total - descuento + costoEnvio)

  const cerrar = (): void => {
    setPaso("formulario")
    onCerrar()
  }

  const manejarSubmit = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setError("")

    if (!carrito || items.length === 0) {
      setError("Tu carrito está vacío.")
      return
    }
    if (entrega === "DOMICILIO" && !direccion.trim()) {
      setError("Indica la dirección de despacho.")
      return
    }
    if (entrega === "RETIRO" && !sucursal.trim()) {
      setError("Indica la sucursal de retiro.")
      return
    }
    if (
      medioPago === "TARJETA" &&
      (!tarjeta.titular.trim() ||
        tarjeta.numero.replace(/\D/g, "").length < 13 ||
        !/^(0[1-9]|1[0-2])\/\d{2}$/.test(tarjeta.vencimiento) ||
        !/^\d{3,4}$/.test(tarjeta.cvv))
    ) {
      setError("Revisa los datos de la tarjeta: titular, número, vencimiento MM/AA y CVV.")
      return
    }

    setPaso("procesando")
    try {
      const payload: CheckoutPayload = {
        carritoId: carrito.id,
        medioPago,
        entrega,
        cuponCodigo: cuponCodigo.trim() || undefined,
        ...(entrega === "DOMICILIO"
          ? { comuna, direccion: direccion.trim() }
          : { sucursal: sucursal.trim() }),
        ...(medioPago === "TARJETA" ? { tarjeta } : {}),
      }
      const creado = await pedidosService.crear(payload)
      setPedido(creado)
      await recargar()
      setPaso("resultado")
      onExito?.(creado)
    } catch (ex) {
      setError(mensajeDeError(ex))
      setPaso("formulario")
    }
  }

  if (!abierto) return null

  return (
    <>
      <div className="modal-backdrop-custom" onClick={cerrar} />
      <div
        className="modal fade show d-block"
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-pago-titulo"
      >
        <div className="modal-dialog modal-dialog-centered">
          <div className="modal-content">
            {paso === "formulario" && (
              <form onSubmit={(e) => void manejarSubmit(e)} noValidate>
                <div className="modal-header">
                  <h5 className="modal-title" id="modal-pago-titulo">
                    Pago
                  </h5>
                  <button
                    type="button"
                    className="btn-close"
                    aria-label="Cerrar"
                    onClick={cerrar}
                  />
                </div>

                <div className="modal-body">
                  {(error || errorPago) && (
                    <p className="alerta alerta-error">{error || errorPago}</p>
                  )}

                  <div className="card mb-4">
                    <div className="card-body">
                      <h5 className="card-title">Opciones de entrega</h5>

                      <div className="form-check mb-2">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="tipoEntrega"
                          id="retiroTienda"
                          checked={entrega === "RETIRO"}
                          onChange={() => setEntrega("RETIRO")}
                        />
                        <label className="form-check-label" htmlFor="retiroTienda">
                          Retiro en tienda
                        </label>
                      </div>

                      <div className="form-check mb-3">
                        <input
                          className="form-check-input"
                          type="radio"
                          name="tipoEntrega"
                          id="envioDomicilio"
                          checked={entrega === "DOMICILIO"}
                          onChange={() => setEntrega("DOMICILIO")}
                        />
                        <label className="form-check-label" htmlFor="envioDomicilio">
                          Envío a domicilio
                        </label>
                      </div>

                      {entrega === "RETIRO" ? (
                        <div className="p-3 border rounded bg-light">
                          <h6 className="mb-2">Sucursal de retiro</h6>
                          <input
                            type="text"
                            className="form-control"
                            value={sucursal}
                            onChange={(e) => setSucursal(e.target.value)}
                            placeholder="Ej: Sucursal Centro"
                          />
                          <p className="form-note mb-0">
                            Te avisaremos cuando tu pedido esté listo para retiro.
                          </p>
                        </div>
                      ) : (
                        <div className="p-3 border rounded bg-light">
                          <h6 className="mb-3">Dirección de envío</h6>

                          {direcciones.length > 0 && (
                            <div className="mb-2">
                              <label className="form-label" htmlFor="direccionGuardada">
                                Usar mi dirección guardada
                              </label>
                              <select
                                id="direccionGuardada"
                                className="form-select"
                                onChange={(e) => {
                                  const elegida = direcciones.find(
                                    (d) => String(d.id) === e.target.value
                                  )
                                  if (elegida) {
                                    setDireccion(elegida.direccion)
                                    setComuna(elegida.comuna)
                                  }
                                }}
                              >
                                {direcciones.map((d) => (
                                  <option key={d.id} value={d.id}>
                                    {d.alias}: {d.direccion}, {d.comuna}
                                  </option>
                                ))}
                              </select>
                            </div>
                          )}

                          <div className="mb-2">
                            <label className="form-label" htmlFor="nuevaComuna">
                              Comuna
                            </label>
                            <select
                              id="nuevaComuna"
                              className="form-select"
                              value={comuna}
                              onChange={(e) => setComuna(e.target.value)}
                            >
                              {COMUNAS.map((c) => (
                                <option key={c} value={c}>
                                  {c}
                                </option>
                              ))}
                            </select>
                          </div>

                          <label className="form-label" htmlFor="nuevaDireccion">
                            Dirección
                          </label>
                          <input
                            id="nuevaDireccion"
                            type="text"
                            className="form-control"
                            value={direccion}
                            onChange={(e) => setDireccion(e.target.value)}
                            placeholder="Ej: Av. Los Leones 123, depto. 45"
                          />

                          {cotizando && <p className="form-note">Cotizando envío…</p>}
                          {cotizacion && (
                            <p className="form-note">
                              Envío {formatoPrecio(cotizacion.costo)} ·{" "}
                              {cotizacion.mensajeEntrega}
                            </p>
                          )}
                          {errorCotizacion && (
                            <p className="mini-aviso error mt-2">{errorCotizacion}</p>
                          )}
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="checkout-resumen">
                    <div className="checkout-total-row">
                      <span>Artículos</span>
                      <span>{formatoPrecio(total)}</span>
                    </div>
                    <div className="checkout-total-row">
                      <span>Envío</span>
                      <span>{costoEnvio === 0 ? "Gratis / retiro" : formatoPrecio(costoEnvio)}</span>
                    </div>
                    {descuento > 0 && (
                      <div className="checkout-total-row descuento">
                        <span>Descuento cupón</span>
                        <span>-{formatoPrecio(descuento)}</span>
                      </div>
                    )}
                    <div className="checkout-total-row total">
                      <span>Total a pagar</span>
                      <span>{formatoPrecio(totalFinal)}</span>
                    </div>
                  </div>

                  <div className="form-field">
                    <label htmlFor="cupon">Cupón de descuento (opcional)</label>
                    <div className="input-con-boton">
                      <input
                        id="cupon"
                        type="text"
                        value={cuponCodigo}
                        placeholder="Ej: BIENVENIDA10"
                        onChange={(e) => setCuponCodigo(e.target.value.toUpperCase())}
                      />
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        onClick={() => void validarCupon(cuponCodigo)}
                      >
                        Aplicar
                      </button>
                    </div>
                    {cupon && (
                      <p className={`mini-aviso ${cupon.valido ? "ok" : "error"}`}>
                        {cupon.mensaje}
                      </p>
                    )}
                  </div>

                  <div className="mb-3">
                    <span className="d-block fw-bold mb-2">Medio de pago</span>
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="radio"
                        name="medioPago"
                        id="pagoTarjeta"
                        checked={medioPago === "TARJETA"}
                        onChange={() => setMedioPago("TARJETA")}
                      />
                      <label className="form-check-label" htmlFor="pagoTarjeta">
                        Tarjeta de crédito / débito
                      </label>
                    </div>
                    <div className="form-check">
                      <input
                        className="form-check-input"
                        type="radio"
                        name="medioPago"
                        id="pagoTransferencia"
                        checked={medioPago === "TRANSFERENCIA"}
                        onChange={() => setMedioPago("TRANSFERENCIA")}
                      />
                      <label className="form-check-label" htmlFor="pagoTransferencia">
                        Transferencia bancaria
                      </label>
                    </div>
                  </div>

                  {medioPago === "TARJETA" ? (
                    <>
                      <div className="form-field">
                        <label htmlFor="pago-nombre">Nombre del titular</label>
                        <input
                          type="text"
                          id="pago-nombre"
                          placeholder="Como aparece en la tarjeta"
                          value={tarjeta.titular}
                          onChange={(e) =>
                            setTarjeta((t) => ({ ...t, titular: e.target.value }))
                          }
                          required
                        />
                      </div>

                      <div className="form-field">
                        <label htmlFor="pago-numero">Número de tarjeta</label>
                        <input
                          type="text"
                          id="pago-numero"
                          inputMode="numeric"
                          maxLength={19}
                          placeholder="4111 1111 1111 1111"
                          value={tarjeta.numero}
                          onChange={(e) => {
                            const digitos = e.target.value
                              .replace(/\D/g, "")
                              .slice(0, 16)
                              .replace(/(.{4})/g, "$1 ")
                              .trim()
                            setTarjeta((t) => ({ ...t, numero: digitos }))
                          }}
                          required
                        />
                      </div>

                      <div style={{ display: "flex", gap: "1rem" }}>
                        <div className="form-field" style={{ flex: 1 }}>
                          <label htmlFor="pago-vencimiento">Vencimiento</label>
                          <input
                            type="text"
                            id="pago-vencimiento"
                            placeholder="MM/AA"
                            maxLength={5}
                            value={tarjeta.vencimiento}
                            onChange={(e) => {
                              let valor = e.target.value.replace(/\D/g, "").slice(0, 4)
                              if (valor.length > 2) valor = `${valor.slice(0, 2)}/${valor.slice(2)}`
                              setTarjeta((t) => ({ ...t, vencimiento: valor }))
                            }}
                            required
                          />
                        </div>
                        <div className="form-field" style={{ flex: 1 }}>
                          <label htmlFor="pago-cvv">CVV</label>
                          <input
                            type="text"
                            id="pago-cvv"
                            inputMode="numeric"
                            maxLength={4}
                            placeholder="123"
                            value={tarjeta.cvv}
                            onChange={(e) =>
                              setTarjeta((t) => ({
                                ...t,
                                cvv: e.target.value.replace(/\D/g, "").slice(0, 4),
                              }))
                            }
                            required
                          />
                        </div>
                      </div>
                      <p className="form-note">
                        Pago simulado: usa 4111 1111 1111 1111. Las tarjetas terminadas en 0000 se
                        rechazan por saldo y las terminadas en 9999 por error temporal.
                      </p>
                    </>
                  ) : (
                    datosTransferencia && (
                      <div className="datos-transferencia">
                        <h6>Datos para transferir</h6>
                        <p>
                          <strong>Titular:</strong> {datosTransferencia.titular}
                        </p>
                        <p>
                          <strong>RUT:</strong> {datosTransferencia.rut}
                        </p>
                        <p>
                          <strong>Banco:</strong> {datosTransferencia.banco}
                        </p>
                        <p>
                          <strong>Cuenta:</strong> {datosTransferencia.tipoCuenta}{" "}
                          {datosTransferencia.numeroCuenta}
                        </p>
                        <p>
                          <strong>Email:</strong> {datosTransferencia.email}
                        </p>
                        <p className="form-note">{datosTransferencia.aviso}</p>
                      </div>
                    )
                  )}
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={cerrar}>
                    Cancelar
                  </button>
                  <button type="submit" className="btn btn-primary">
                    {medioPago === "TARJETA" ? "Pagar ahora" : "Crear pedido"}
                  </button>
                </div>
              </form>
            )}

            {paso === "procesando" && (
              <div className="text-center" style={{ padding: "3rem 1.5rem" }}>
                <div
                  className="spinner-border"
                  role="status"
                  style={{ width: "3rem", height: "3rem", color: "var(--coral-dark)" }}
                >
                  <span className="visually-hidden">Procesando...</span>
                </div>
                <p style={{ marginTop: "1.25rem", fontWeight: 600 }}>
                  Procesando tu pago simulado...
                </p>
              </div>
            )}

            {paso === "resultado" && pedido && (
              <div className="text-center" style={{ padding: "2.5rem 1.5rem" }}>
                <div style={{ fontSize: "2.5rem" }}>✅</div>
                <h5 style={{ marginTop: "0.75rem" }}>
                  {pedido.pagoConfirmado ? "¡Pago simulado exitoso!" : "Pedido registrado"}
                </h5>
                <p style={{ color: "var(--color-text-muted)" }}>
                  N.º de pedido: <strong>{pedido.numero}</strong>
                </p>
                <p style={{ color: "var(--color-text-muted)" }}>
                  Total: <strong>{formatoPrecio(pedido.total)}</strong>
                </p>
                {pedido.codigoAutorizacion && (
                  <p style={{ color: "var(--color-text-muted)" }}>
                    Código de autorización: <strong>{pedido.codigoAutorizacion}</strong>
                  </p>
                )}
                {!pedido.pagoConfirmado && datosTransferencia && (
                  <p className="form-note">
                    Tu pedido queda pendiente hasta que envíes el comprobante de transferencia a{" "}
                    {datosTransferencia.email}.
                  </p>
                )}
                <div className="d-flex justify-content-center gap-2 mt-3">
                  <Link to="/pedidos" className="btn btn-secondary" onClick={cerrar}>
                    Ver mis pedidos
                  </Link>
                  <button type="button" className="btn btn-primary" onClick={cerrar}>
                    Cerrar
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  )
}
