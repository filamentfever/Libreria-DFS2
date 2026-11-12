// Pie de página estilo librería: marca, newsletter y enlaces de ayuda.
import { useState, type FormEvent } from "react"
import { Link } from "react-router"
import { MailIcon, PinIcon } from "@/client/components/icons"

export function SiteFooter() {
  const [correo, setCorreo] = useState("")
  const [suscrito, setSuscrito] = useState(false)

  const suscribir = (evento: FormEvent): void => {
    evento.preventDefault()
    if (!correo.trim()) return
    setSuscrito(true)
    setCorreo("")
  }

  return (
    <footer className="site-footer cp-footer">
      <div className="container">
        <div className="cp-footer-top">
          <div className="cp-footer-brand">
            <Link to="/" className="cp-logo cp-logo--footer" aria-label="Librería Juanin">
              <span className="cp-logo-mark" aria-hidden>
                LJ
              </span>
              <span className="cp-logo-text">
                <small>Librería</small>
                <strong>Juanin</strong>
              </span>
            </Link>
            <p>
              Libros, cuadernos y papelería para acompañarte en cada historia. Compra online y
              retira en tienda.
            </p>

            <form className="cp-newsletter" onSubmit={suscribir}>
              <label htmlFor="newsletter-correo">Novedades y recomendados en tu correo</label>
              <div className="cp-newsletter-row">
                <input
                  id="newsletter-correo"
                  type="email"
                  placeholder="tucorreo@ejemplo.cl"
                  value={correo}
                  onChange={(evento) => setCorreo(evento.target.value)}
                  required
                />
                <button type="submit">Suscribirme</button>
              </div>
              {suscrito && <p className="cp-newsletter-ok">¡Gracias! Te escribiremos pronto.</p>}
            </form>

            <div className="cp-footer-contact">
              <p>
                <MailIcon /> libreria_juanin@gmail.cl
              </p>
              <p>
                <PinIcon /> Retiro y tienda en Santiago
              </p>
            </div>
          </div>

          <div className="cp-footer-col">
            <h3>Catálogo</h3>
            <ul>
              <li>
                <Link to="/catalogo?orden=novedades">Novedades</Link>
              </li>
              <li>
                <Link to="/catalogo?categoria=Libros">Libros</Link>
              </li>
              <li>
                <Link to="/catalogo?categoria=Cuadernos">Cuadernos</Link>
              </li>
              <li>
                <Link to="/catalogo?categoria=Lápices">Lápices</Link>
              </li>
              <li>
                <Link to="/catalogo">Ver todo</Link>
              </li>
            </ul>
          </div>

          <div className="cp-footer-col">
            <h3>Mi cuenta</h3>
            <ul>
              <li>
                <Link to="/perfil">Mi perfil</Link>
              </li>
              <li>
                <Link to="/pedidos">Mis pedidos</Link>
              </li>
              <li>
                <Link to="/favoritos">Favoritos</Link>
              </li>
              <li>
                <Link to="/carrito">Carrito</Link>
              </li>
              <li>
                <Link to="/adm">Panel admin</Link>
              </li>
            </ul>
          </div>

          <div className="cp-footer-col">
            <h3>Ayuda</h3>
            <ul>
              <li>
                <Link to="/contacto">Contacto</Link>
              </li>
              <li>
                <Link to="/contacto">Envíos y retiros</Link>
              </li>
              <li>
                <Link to="/contacto">Cambios y devoluciones</Link>
              </li>
              <li>
                <a href="mailto:libreria_juanin@gmail.cl">Escríbenos</a>
              </li>
              <li>
                <Link to="/catalogo">Preguntas frecuentes</Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="cp-footer-bottom">
          <p>&copy; 2026 Librería Juanin · Todos los derechos reservados.</p>
          <p className="cp-footer-pay">Webpay · Mercado Pago · Transferencia bancaria</p>
        </div>
      </div>
    </footer>
  )
}
