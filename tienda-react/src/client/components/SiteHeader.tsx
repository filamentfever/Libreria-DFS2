// Cabecera estilo librería: barra de anuncios, buscador central, acciones y navegación.
import { useState } from "react"
import { Link, NavLink, useNavigate } from "react-router"
import { useCarrito } from "@/client/context/CarritoContext"
import { useCategorias } from "@/hooks/useProductos"
import { AuthDrawer } from "@/client/components/AuthDrawer"
import {
  BagIcon,
  ChevronDownIcon,
  CloseIcon,
  HeartIcon,
  MenuIcon,
  SearchIcon,
  UserIcon,
} from "@/client/components/icons"

const claseEnlace = ({ isActive }: { isActive: boolean }) =>
  isActive ? "nav-link active" : "nav-link"

export function SiteHeader() {
  const [abierto, setAbierto] = useState(false)
  const [authAbierto, setAuthAbierto] = useState(false)
  const [catAbierto, setCatAbierto] = useState(false)
  const [texto, setTexto] = useState("")
  const { cantidadTotal } = useCarrito()
  const { categorias } = useCategorias()
  const navigate = useNavigate()

  const buscar = (evento: React.FormEvent): void => {
    evento.preventDefault()
    const consulta = texto.trim()
    setAbierto(false)
    navigate(consulta ? `/catalogo?q=${encodeURIComponent(consulta)}` : "/catalogo")
  }

  const enlacesPrincipales = (
    <>
      <Link to="/catalogo?orden=novedades" className="cp-nav-link" onClick={() => setAbierto(false)}>
        Novedades
      </Link>
      <Link to="/catalogo?categoria=Libros" className="cp-nav-link" onClick={() => setAbierto(false)}>
        Libros
      </Link>
      <Link to="/catalogo?categoria=Cuadernos" className="cp-nav-link" onClick={() => setAbierto(false)}>
        Cuadernos
      </Link>
      <Link to="/catalogo?categoria=Lápices" className="cp-nav-link" onClick={() => setAbierto(false)}>
        Lápices
      </Link>
      <Link to="/catalogo" className="cp-nav-link" onClick={() => setAbierto(false)}>
        Catálogo
      </Link>
      <Link to="/contacto" className="cp-nav-link" onClick={() => setAbierto(false)}>
        Contacto
      </Link>
    </>
  )

  return (
    <>
      <header className="site-header cp-site-header">
      <div className="cp-topbar">
        <div className="container cp-topbar-inner">
          <p className="cp-topbar-msg">
            ⚡ Retiro en tienda inmediato · Envío express a todo Chile ⚡
          </p>
          <div className="cp-topbar-links">
            <Link to="/contacto">Tiendas y contacto</Link>
            <Link to="/adm">Panel admin</Link>
          </div>
        </div>
      </div>

      <div className="container cp-header-main">
        <Link to="/" className="cp-logo" aria-label="Librería Juanin, ir al inicio">
          <span className="cp-logo-mark" aria-hidden>
            LJ
          </span>
          <span className="cp-logo-text">
            <small>Librería</small>
            <strong>Juanin</strong>
          </span>
        </Link>

        <form className="cp-search" role="search" onSubmit={buscar}>
          <SearchIcon className="cp-search-icon" />
          <input
            type="search"
            placeholder="Busca por título, autor o categoría"
            aria-label="Buscar productos"
            value={texto}
            onChange={(evento) => setTexto(evento.target.value)}
          />
          <button type="submit">Buscar</button>
        </form>

        <div className="cp-actions">
          <button
            type="button"
            className="cp-action cp-action--boton"
            onClick={() => setAuthAbierto(true)}
            aria-haspopup="dialog"
          >
            <UserIcon />
            <span>Mi cuenta</span>
          </button>
          <Link to="/favoritos" className="cp-action">
            <HeartIcon />
            <span>Favoritos</span>
          </Link>
          <Link to="/carrito" className="cp-action cp-action--bag">
            <span className="cp-bag-icon">
              <BagIcon />
              <b className="cp-bag-count" data-carrito-contador>
                {cantidadTotal}
              </b>
            </span>
            <span>Carrito</span>
          </Link>
          <button
            className="cp-menu-btn"
            type="button"
            aria-label={abierto ? "Cerrar menú" : "Abrir menú"}
            aria-expanded={abierto}
            onClick={() => setAbierto((valor) => !valor)}
          >
            {abierto ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>
      </div>

      <nav className="cp-nav" aria-label="Navegación principal">
        <div className="container cp-nav-inner">
          <div
            className="cp-nav-dropdown"
            onMouseEnter={() => setCatAbierto(true)}
            onMouseLeave={() => setCatAbierto(false)}
          >
            <button
              type="button"
              className="cp-nav-link cp-nav-toggle"
              aria-expanded={catAbierto}
              onClick={() => setCatAbierto((valor) => !valor)}
            >
              Categorías <ChevronDownIcon />
            </button>
            {catAbierto && (
              <div className="cp-dropdown">
                <Link to="/catalogo" onClick={() => setCatAbierto(false)}>
                  Todas las categorías
                </Link>
                {categorias.map((categoria) => (
                  <Link
                    key={categoria.nombre}
                    to={`/catalogo?categoria=${encodeURIComponent(categoria.nombre)}`}
                    onClick={() => setCatAbierto(false)}
                  >
                    {categoria.nombre}
                  </Link>
                ))}
              </div>
            )}
          </div>
          {enlacesPrincipales}
        </div>
      </nav>

      {abierto && (
        <div className="cp-mobile">
          <form className="cp-search cp-search--mobile" role="search" onSubmit={buscar}>
            <SearchIcon className="cp-search-icon" />
            <input
              type="search"
              placeholder="Buscar en la librería"
              aria-label="Buscar productos"
              value={texto}
              onChange={(evento) => setTexto(evento.target.value)}
            />
            <button type="submit">Buscar</button>
          </form>

          <div className="cp-mobile-links">
            <Link to="/" onClick={() => setAbierto(false)}>
              Inicio
            </Link>
            {enlacesPrincipales}
            <Link to="/favoritos" onClick={() => setAbierto(false)}>
              Favoritos
            </Link>
            <Link to="/perfil" onClick={() => setAbierto(false)}>
              Mi perfil
            </Link>
            <button
              type="button"
              className="cp-mobile-auth"
              onClick={() => {
                setAbierto(false)
                setAuthAbierto(true)
              }}
            >
              Iniciar sesión
            </button>
            <Link to="/carrito" onClick={() => setAbierto(false)}>
              Carrito ({cantidadTotal})
            </Link>
          </div>

          <p className="cp-mobile-cats-title">Categorías</p>
          <div className="cp-mobile-cats">
            {categorias.map((categoria) => (
              <Link
                key={categoria.nombre}
                to={`/catalogo?categoria=${encodeURIComponent(categoria.nombre)}`}
                onClick={() => setAbierto(false)}
              >
                {categoria.nombre}
              </Link>
            ))}
          </div>

          <div className="cp-mobile-utils">
            <NavLink to="/adm" className={claseEnlace} onClick={() => setAbierto(false)}>
              Panel admin
            </NavLink>
            <Link to="/contacto" onClick={() => setAbierto(false)}>
              Contacto
            </Link>
          </div>
        </div>
      )}
      </header>

      <AuthDrawer abierto={authAbierto} onCerrar={() => setAuthAbierto(false)} />
    </>
  )
}
