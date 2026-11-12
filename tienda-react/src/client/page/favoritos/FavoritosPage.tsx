// Favoritos guardados en el navegador (preferencia local del cliente).
import { useState } from "react"
import { Link } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { useFavoritos } from "@/hooks/useFavoritos"
import { formatoPrecio } from "@/services/catalogoService"
import { mensajeDeError } from "@/services/api"

export const FavoritosPage = () => {
  const { favoritos, quitar, limpiar } = useFavoritos()
  const { agregar, agregandoId } = useCarrito()
  const [error, setError] = useState("")

  const manejarAgregar = async (id: number): Promise<void> => {
    setError("")
    try {
      await agregar(id, 1)
    } catch (ex) {
      setError(mensajeDeError(ex))
    }
  }

  return (
    <section>
      <div className="container">
        <nav aria-label="Ruta de navegación" className="migas">
          <Link to="/">Inicio</Link> / <span>Favoritos</span>
        </nav>

        <h1 className="section-title">Mis favoritos</h1>

        {error && <p className="alerta alerta-error">{error}</p>}

        {favoritos.length === 0 ? (
          <div className="carrito-vacio">
            <p>Todavía no tienes productos favoritos.</p>
            <Link to="/catalogo" className="btn btn-primary">
              Explorar catálogo
            </Link>
          </div>
        ) : (
          <>
            <div className="d-flex justify-content-end mb-3">
              <button type="button" className="btn btn-secondary btn-sm" onClick={limpiar}>
                Limpiar favoritos
              </button>
            </div>

            <div className="product-grid">
              {favoritos.map((favorito) => (
                <div className="product-card" key={favorito.id}>
                  <Link to={`/producto/${favorito.slug}`}>
                    <img
                      src={favorito.imagen || "/img/imagendestaca.jpg"}
                      alt={favorito.nombre}
                    />
                    <div className="info">
                      <p className="name">{favorito.nombre}</p>
                      <p className="price">{formatoPrecio(favorito.precio)}</p>
                    </div>
                  </Link>
                  <div className="acciones-producto">
                    <button
                      type="button"
                      className="btn btn-primary"
                      style={{ width: "100%" }}
                      disabled={agregandoId === favorito.id}
                      onClick={() => void manejarAgregar(favorito.id)}
                    >
                      {agregandoId === favorito.id ? "Agregando…" : "Agregar al carrito"}
                    </button>
                    <button
                      type="button"
                      className="btn-favorito activo"
                      onClick={() => quitar(favorito.id)}
                    >
                      ♥ Quitar de favoritos
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </section>
  )
}
