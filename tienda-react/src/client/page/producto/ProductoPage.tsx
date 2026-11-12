// Detalle de producto + reseñas (MSCI-CATALOGO y MSCI-RESENAS).
import { useState, type FormEvent } from "react"
import { Link, useParams } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { useFavoritos } from "@/hooks/useFavoritos"
import { useProducto } from "@/hooks/useProductos"
import { useResenas } from "@/hooks/useResenas"
import { formatoPrecio, imagenProducto } from "@/services/catalogoService"
import { mensajeDeError } from "@/services/api"

function estrellas(puntuacion: number): string {
  const llenas = Math.round(puntuacion)
  return "★".repeat(Math.max(0, Math.min(5, llenas))) + "☆".repeat(Math.max(0, 5 - llenas))
}

export const ProductoPage = () => {
  const { slug } = useParams()
  const { producto, cargando, error } = useProducto(slug)
  const { resenas, resumen, crear, cargando: cargandoResenas } = useResenas(producto?.id)
  const { agregar, agregandoId, usuarioId } = useCarrito()
  const { esFavorito, alternar } = useFavoritos()

  const [cantidad, setCantidad] = useState(1)
  const [aviso, setAviso] = useState("")
  const [errorAccion, setErrorAccion] = useState("")
  const [puntuacion, setPuntuacion] = useState(5)
  const [comentario, setComentario] = useState("")
  const [avisoResena, setAvisoResena] = useState("")
  const [errorResena, setErrorResena] = useState("")

  const manejarAgregar = async (): Promise<void> => {
    if (!producto) return
    setAviso("")
    setErrorAccion("")
    try {
      await agregar(producto.id, cantidad)
      setAviso(`"${producto.nombre}" se agregó al carrito.`)
    } catch (ex) {
      setErrorAccion(mensajeDeError(ex))
    }
  }

  const manejarResena = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    if (!producto || !usuarioId) return
    setAvisoResena("")
    setErrorResena("")
    if (!comentario.trim()) {
      setErrorResena("Escribe un comentario para tu reseña.")
      return
    }
    try {
      await crear({
        idProducto: producto.id,
        idUsuario: usuarioId,
        puntuacion,
        comentario: comentario.trim(),
      })
      setComentario("")
      setPuntuacion(5)
      setAvisoResena("¡Gracias! Tu reseña fue publicada.")
    } catch (ex) {
      setErrorResena(mensajeDeError(ex))
    }
  }

  if (cargando) {
    return (
      <section>
        <div className="container">
          <p className="cargando">Cargando producto…</p>
        </div>
      </section>
    )
  }

  if (error || !producto) {
    return (
      <section>
        <div className="container">
          <p className="alerta alerta-error">
            No se pudo cargar el producto: {error || "no encontrado"}.
          </p>
          <Link to="/catalogo" className="btn btn-primary">
            Volver al catálogo
          </Link>
        </div>
      </section>
    )
  }

  const sinStock = producto.stock <= 0 || !producto.activo
  const agregando = agregandoId === producto.id
  const favorito = esFavorito(producto.id)
  const promedio = resumen?.promedio ?? 0

  return (
    <section className="container my-4">
      <nav aria-label="breadcrumb">
        <ol className="breadcrumb">
          <li className="breadcrumb-item">
            <Link to="/">Inicio</Link>
          </li>
          <li className="breadcrumb-item">
            <Link to="/catalogo">Catálogo</Link>
          </li>
          <li className="breadcrumb-item active" aria-current="page">
            {producto.nombre}
          </li>
        </ol>
      </nav>

      <article className="product-detail">
        <div className="product-gallery">
          <img src={imagenProducto(producto)} alt={producto.nombre} />
          <div className="thumbs">
            <img src={imagenProducto(producto)} alt={`Miniatura 1 de ${producto.nombre}`} />
            <img src={imagenProducto(producto)} alt={`Miniatura 2 de ${producto.nombre}`} />
            <img src={imagenProducto(producto)} alt={`Miniatura 3 de ${producto.nombre}`} />
          </div>
        </div>

        <div className="product-info">
          <h1>{producto.nombre}</h1>
          <p className="product-price">{formatoPrecio(producto.precio)}</p>
          <p>{producto.descripcion}</p>

          <form
            className="qty-form"
            onSubmit={(e) => {
              e.preventDefault()
              void manejarAgregar()
            }}
          >
            <label htmlFor="cantidad">Cantidad</label>
            <input
              type="number"
              id="cantidad"
              name="cantidad"
              value={cantidad}
              min={1}
              max={Math.max(1, producto.stock)}
              onChange={(e) => setCantidad(Math.max(1, Number(e.target.value) || 1))}
            />
            <button
              type="submit"
              className="btn btn-primary"
              disabled={sinStock || agregando}
            >
              {sinStock ? "Sin stock" : agregando ? "Agregando…" : "Agregar al carrito"}
            </button>
            <button
              type="button"
              className={`btn-favorito${favorito ? " activo" : ""}`}
              onClick={() => alternar(producto)}
            >
              {favorito ? "♥ En favoritos" : "♡ Favorito"}
            </button>
          </form>

          {aviso && <p className="alerta alerta-ok">{aviso}</p>}
          {errorAccion && <p className="alerta alerta-error">{errorAccion}</p>}

          <h2 style={{ fontSize: "1.1rem" }}>Detalles</h2>
          <ul>
            <li>
              <strong>Categoría:</strong> {producto.categoria}
              {producto.subcategoria ? ` / ${producto.subcategoria}` : ""}
            </li>
            <li>
              <strong>SKU:</strong> {producto.sku}
            </li>
            <li>
              <strong>Marca:</strong> {producto.marca || "Sin marca"}
            </li>
            <li>
              <strong>Disponibilidad:</strong>{" "}
              {sinStock ? "Sin stock" : `En stock (${producto.stock} unidades)`}
            </li>
          </ul>
        </div>
      </article>

      <section id="resenas" className="mt-5">
        <h2 className="section-title">Reseñas y valoraciones</h2>

        <div className="rating-summary">
          <span className="rating-average">
            {promedio ? promedio.toFixed(1).replace(".", ",") : "—"}
          </span>
          <span className="rating-stars" aria-label={`${promedio} de 5 estrellas`}>
            {estrellas(promedio)}
          </span>
          <p>
            {resumen?.cantidad ?? 0}{" "}
            {(resumen?.cantidad ?? 0) === 1 ? "valoración" : "valoraciones"}
          </p>
        </div>

        {cargandoResenas && <p className="cargando">Cargando reseñas…</p>}

        {!cargandoResenas && resenas.length === 0 && (
          <p className="text-muted">Aún no hay reseñas de este producto.</p>
        )}

        {resenas.length > 0 && (
          <div className="review-list">
            {resenas.map((resena) => (
              <article key={resena.id} className="review-card">
                <div className="review-header">
                  <span className="review-author">{resena.autor || "Cliente"}</span>
                  <span className="review-stars">{estrellas(resena.puntuacion)}</span>
                  <span className="review-date">
                    {new Date(resena.fecha).toLocaleDateString("es-CL")}
                  </span>
                </div>
                <p className="review-text">{resena.comentario}</p>
              </article>
            ))}
          </div>
        )}

        {usuarioId && (
          <form className="form-card" onSubmit={(e) => void manejarResena(e)}>
            <h3 style={{ fontSize: "1.05rem" }}>Escribe tu reseña</h3>
            <div className="form-field">
              <label htmlFor="resena-puntuacion">Puntuación</label>
              <select
                id="resena-puntuacion"
                value={puntuacion}
                onChange={(e) => setPuntuacion(Number(e.target.value))}
              >
                <option value={5}>5 estrellas</option>
                <option value={4}>4 estrellas</option>
                <option value={3}>3 estrellas</option>
                <option value={2}>2 estrellas</option>
                <option value={1}>1 estrella</option>
              </select>
            </div>
            <div className="form-field">
              <label htmlFor="resena-comentario">Comentario</label>
              <textarea
                id="resena-comentario"
                rows={3}
                value={comentario}
                onChange={(e) => setComentario(e.target.value)}
                placeholder="Cuéntanos qué te pareció el producto"
              />
            </div>
            {avisoResena && <p className="alerta alerta-ok">{avisoResena}</p>}
            {errorResena && <p className="alerta alerta-error">{errorResena}</p>}
            <button type="submit" className="btn btn-primary">
              Publicar reseña
            </button>
          </form>
        )}
      </section>
    </section>
  )
}
