// Tarjeta de producto estilo librería: portada, autor, precio y compra rápida.
import { useState } from "react"
import { Link } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { useFavoritos } from "@/hooks/useFavoritos"
import { formatoPrecio, imagenProducto } from "@/services/catalogoService"
import type { Producto } from "@/services/catalogoService"
import { mensajeDeError } from "@/services/api"
import { HeartIcon } from "@/client/components/icons"

interface Props {
  producto: Producto
  permitirFavorito?: boolean
}

export function ProductoCard({ producto, permitirFavorito = true }: Props) {
  const { agregar, agregandoId, carrito, cargando } = useCarrito()
  const { esFavorito, alternar } = useFavoritos()
  const [aviso, setAviso] = useState("")
  const [error, setError] = useState("")

  const sinStock = producto.stock <= 0 || !producto.activo
  const agregando = agregandoId === producto.id
  const preparando = cargando || !carrito
  const favorito = esFavorito(producto.id)
  const ultimasUnidades = !sinStock && producto.stock <= 3

  const manejarAgregar = async (): Promise<void> => {
    setAviso("")
    setError("")
    try {
      await agregar(producto.id, 1)
      setAviso("Agregado al carrito")
      window.setTimeout(() => setAviso(""), 2200)
    } catch (ex) {
      setError(mensajeDeError(ex))
    }
  }

  const etiquetaBoton = sinStock
    ? "Agotado"
    : agregando
      ? "Agregando…"
      : preparando
        ? "Preparando…"
        : "Agregar al carrito"

  return (
    <article className="product-card">
      <div className="card-media">
        <Link to={`/producto/${producto.slug}`} className="card-media-link">
          <img src={imagenProducto(producto)} alt={producto.nombre} loading="lazy" />
        </Link>

        <div className="card-badges">
          {producto.nuevo && <span className="badge-nuevo">Nuevo</span>}
          {sinStock && <span className="badge-agotado">Agotado</span>}
        </div>

        {permitirFavorito && (
          <button
            type="button"
            className={`card-fav${favorito ? " activo" : ""}`}
            aria-pressed={favorito}
            aria-label={favorito ? "Quitar de favoritos" : "Agregar a favoritos"}
            title={favorito ? "Quitar de favoritos" : "Agregar a favoritos"}
            onClick={() => alternar(producto)}
          >
            <HeartIcon relleno={favorito} />
          </button>
        )}
      </div>

      <div className="info">
        <Link to={`/producto/${producto.slug}`} className="info-link">
          <h3 className="name">{producto.nombre}</h3>
        </Link>
        {producto.marca && <p className="autor">{producto.marca}</p>}
        <p className="price">{formatoPrecio(producto.precio)}</p>
        <p className="card-meta">
          <span>{producto.categoria}</span>
          {ultimasUnidades && <span className="stock-note">Últimas unidades</span>}
        </p>
      </div>

      <div className="acciones-producto">
        <button
          type="button"
          className="btn btn-carrito"
          disabled={sinStock || agregando || preparando}
          onClick={() => void manejarAgregar()}
        >
          {etiquetaBoton}
        </button>

        {aviso && <p className="mini-aviso ok">{aviso}</p>}
        {error && <p className="mini-aviso error">{error}</p>}
      </div>
    </article>
  )
}
