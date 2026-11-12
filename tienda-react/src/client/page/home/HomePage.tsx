// Portada estilo librería: hero, carrusel de novedades, categorías y recomendados.
import { useRef } from "react"
import { Link } from "react-router"
import { ProductoCard } from "@/client/components/ProductoCard"
import {
  ArrowRightIcon,
  BookIcon,
  CardIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ShieldIcon,
  StoreIcon,
  TruckIcon,
} from "@/client/components/icons"
import type { Producto } from "@/services/catalogoService"
import { useCategorias, useProductos } from "@/hooks/useProductos"

const imagenesCategoria: Record<string, string> = {
  Libros: "/img/libroaura.jpg",
  Cuadernos: "/img/cuadernomiku.jpg",
  "Lápices": "/img/lapizmina.jpg",
  Papeles: "/img/hojascarta.jpg",
  Accesorios: "/img/estuche.jpg",
}

const ordenCategorias = ["Libros", "Cuadernos", "Lápices", "Accesorios", "Papeles"]

const categoriasDeRespaldo = [
  { nombre: "Libros", subcategorias: [] },
  { nombre: "Cuadernos", subcategorias: [] },
  { nombre: "Lápices", subcategorias: [] },
]

function CarruselProductos({
  titulo,
  enlaceVerTodo,
  productos,
  cargando,
  error,
}: {
  titulo: string
  enlaceVerTodo: string
  productos: Producto[]
  cargando: boolean
  error: string
}) {
  const rail = useRef<HTMLDivElement>(null)

  const mover = (direccion: -1 | 1): void => {
    const nodo = rail.current
    if (!nodo) return
    nodo.scrollBy({ left: direccion * Math.max(260, nodo.clientWidth * 0.75), behavior: "smooth" })
  }

  return (
    <section className="cp-section">
      <div className="container">
        <div className="cp-section-head">
          <h2>
            {titulo} <span>[+]</span>
          </h2>
          <Link to={enlaceVerTodo} className="cp-ver-todo">
            Ver todo <ArrowRightIcon />
          </Link>
        </div>

        {cargando && (
          <div className="cp-rail cp-rail--skeleton" aria-hidden>
            {Array.from({ length: 4 }, (_, i) => (
              <div className="cp-rail-item" key={i}>
                <div className="card-media skeleton" />
                <div className="skeleton skeleton-line" />
                <div className="skeleton skeleton-line short" />
              </div>
            ))}
          </div>
        )}

        {error && (
          <p className="alerta alerta-error">
            No se pudo cargar el catálogo: {error}. Verifica que el API Gateway (8080) y el
            microservicio de catálogo estén corriendo.
          </p>
        )}

        {!cargando && !error && (
          <div className="cp-rail-wrap">
            <button
              type="button"
              className="cp-rail-btn prev"
              aria-label="Ver productos anteriores"
              onClick={() => mover(-1)}
            >
              <ChevronLeftIcon />
            </button>
            <div className="cp-rail" ref={rail}>
              {productos.map((producto) => (
                <div className="cp-rail-item" key={producto.id}>
                  <ProductoCard producto={producto} />
                </div>
              ))}
            </div>
            <button
              type="button"
              className="cp-rail-btn next"
              aria-label="Ver más productos"
              onClick={() => mover(1)}
            >
              <ChevronRightIcon />
            </button>
          </div>
        )}
      </div>
    </section>
  )
}

export const HomePage = () => {
  const novedades = useProductos({ tamano: 8, orden: "novedades" })
  const recomendados = useProductos({ tamano: 8 })
  const { categorias } = useCategorias()

  const baseCategorias = categorias.length > 0 ? categorias : categoriasDeRespaldo
  const tiles = [...baseCategorias]
    .sort(
      (a, b) =>
        (ordenCategorias.indexOf(a.nombre) + 1 || 99) -
        (ordenCategorias.indexOf(b.nombre) + 1 || 99)
    )
    .slice(0, 3)

  return (
    <>
      <section className="cp-hero">
        <div className="cp-hero-inner">
          <div className="cp-hero-panel">
            <p className="cp-eyebrow">Librería · Papelería · Regalos</p>
            <h1>
              Todo para crear,
              <br />
              <em>leer y soñar.</em>
            </h1>
            <p className="cp-hero-sub">
              Cuadernos, libros y útiles elegidos para acompañarte todos los días.
            </p>
            <div className="cp-hero-actions">
              <Link to="/catalogo" className="cp-btn cp-btn--light">
                Ver catálogo
              </Link>
              <Link to="/catalogo?orden=novedades" className="cp-btn cp-btn--ghost">
                Novedades
              </Link>
            </div>
            <div className="cp-hero-badge" aria-hidden>
              <strong>Envío</strong>
              <span>express</span>
            </div>
          </div>
          <div className="cp-hero-media">
            <img src="/img/imagendestaca.jpg" alt="Útiles y cuadernos de Librería Juanin" />
            <span className="cp-hero-caption">Papelería para todos los días</span>
          </div>
        </div>
        <Link to="/contacto" className="cp-hero-strip">
          ⚡ Retiro en tienda inmediato · Despacho express a todo Chile ⚡
        </Link>
      </section>

      <CarruselProductos
        titulo="Novedades"
        enlaceVerTodo="/catalogo?orden=novedades"
        productos={novedades.productos}
        cargando={novedades.cargando}
        error={novedades.error}
      />

      <section className="cp-section cp-section--soft">
        <div className="container">
          <div className="cp-section-head">
            <h2>Explora por categoría</h2>
            <Link to="/catalogo" className="cp-ver-todo">
              Ver todo <ArrowRightIcon />
            </Link>
          </div>

          <div className="cp-tiles">
            {tiles.map((categoria, indice) => (
              <Link
                key={categoria.nombre}
                to={`/catalogo?categoria=${encodeURIComponent(categoria.nombre)}`}
                className={`cp-tile cp-tile-${indice + 1}`}
              >
                <div className="cp-tile-text">
                  <span>Colección</span>
                  <strong>{categoria.nombre}</strong>
                  <em>
                    Ver productos <ArrowRightIcon />
                  </em>
                </div>
                <img
                  src={imagenesCategoria[categoria.nombre] ?? "/img/imagendestaca.jpg"}
                  alt=""
                  loading="lazy"
                />
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="cp-section">
        <div className="container">
          <div className="cp-promo">
            <div className="cp-promo-text">
              <span className="cp-kicker">Nuestra selección</span>
              <h2>
                Historias que
                <br />
                te acompañan
              </h2>
              <p>
                Libros para regalar, cuadernos para estrenar y todo lo que necesitas para tu
                escritorio.
              </p>
              <Link to="/catalogo?categoria=Libros" className="cp-btn cp-btn--light">
                Ver libros <ArrowRightIcon />
              </Link>
            </div>
            <div className="cp-promo-media">
              <img src="/img/libroaura.jpg" alt="Libro destacado de la librería" loading="lazy" />
              <span className="cp-promo-tag">Recomendado del mes</span>
            </div>
          </div>
        </div>
      </section>

      <CarruselProductos
        titulo="Recomendados"
        enlaceVerTodo="/catalogo"
        productos={recomendados.productos}
        cargando={recomendados.cargando}
        error={recomendados.error}
      />

      <section className="cp-benefits">
        <div className="container cp-benefits-grid">
          <div className="cp-benefit">
            <TruckIcon />
            <div>
              <strong>Despacho a todo Chile</strong>
              <p>Envío express y seguimiento de tu pedido.</p>
            </div>
          </div>
          <div className="cp-benefit">
            <StoreIcon />
            <div>
              <strong>Retiro en tienda</strong>
              <p>Compra online y retira el mismo día.</p>
            </div>
          </div>
          <div className="cp-benefit">
            <CardIcon />
            <div>
              <strong>Pago seguro</strong>
              <p>Tarjeta, transferencia y cupones.</p>
            </div>
          </div>
          <div className="cp-benefit">
            <ShieldIcon />
            <div>
              <strong>Compra protegida</strong>
              <p>Te ayudamos con cambios y devoluciones.</p>
            </div>
          </div>
          <div className="cp-benefit">
            <BookIcon />
            <div>
              <strong>Selección cuidada</strong>
              <p>Libros y papelería elegidos a mano.</p>
            </div>
          </div>
        </div>
      </section>
    </>
  )
}
