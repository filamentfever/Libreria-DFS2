// Contacto: el formulario envía una notificación al microservicio correspondiente.
import { useState, type FormEvent } from "react"
import { Link } from "react-router"
import { useNotificaciones } from "@/hooks/useNotificaciones"
import { mensajeDeError } from "@/services/api"

export const ContactoPage = () => {
  const { enviar } = useNotificaciones()
  const [form, setForm] = useState({ nombre: "", email: "", mensaje: "" })
  const [aviso, setAviso] = useState("")
  const [error, setError] = useState("")
  const [enviando, setEnviando] = useState(false)

  const manejarEnvio = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setAviso("")
    setError("")
    setEnviando(true)
    try {
      await enviar({
        tipoEvento: "CONTACTO",
        destinatario: form.email,
        asunto: `Contacto de ${form.nombre}`,
        mensaje: form.mensaje,
      })
      setAviso("¡Mensaje enviado! Te responderemos a la brevedad.")
      setForm({ nombre: "", email: "", mensaje: "" })
    } catch (ex) {
      setError(mensajeDeError(ex))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="container my-4">
      <nav aria-label="Ruta de navegación" className="migas">
        <Link to="/">Inicio</Link> / <span>Contacto</span>
      </nav>

      <h1 className="section-title mb-4">Contacto</h1>

      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card p-4 border-0 shadow-sm">
            {aviso && <p className="alerta alerta-ok">{aviso}</p>}
            {error && <p className="alerta alerta-error">{error}</p>}

            <form onSubmit={(e) => void manejarEnvio(e)}>
              <div className="mb-3">
                <label htmlFor="nombre" className="form-label">
                  Nombre completo
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="nombre"
                  placeholder="Tu nombre"
                  value={form.nombre}
                  onChange={(e) => setForm((f) => ({ ...f, nombre: e.target.value }))}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="email" className="form-label">
                  Correo electrónico
                </label>
                <input
                  type="email"
                  className="form-control"
                  id="email"
                  placeholder="nombre@correo.com"
                  value={form.email}
                  onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                  required
                />
              </div>
              <div className="mb-3">
                <label htmlFor="mensaje" className="form-label">
                  Mensaje
                </label>
                <textarea
                  className="form-control"
                  id="mensaje"
                  rows={4}
                  placeholder="Escribe tu consulta aquí..."
                  value={form.mensaje}
                  onChange={(e) => setForm((f) => ({ ...f, mensaje: e.target.value }))}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary w-100 py-2 fw-bold" disabled={enviando}>
                {enviando ? "Enviando…" : "Enviar mensaje"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>
  )
}
