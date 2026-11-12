// Panel lateral de cuenta: iniciar sesión, crear cuenta y recuperar contraseña.
import { useEffect, useRef, useState, type FormEvent } from "react"
import { Link } from "react-router"
import { ApiError, mensajeDeError } from "@/services/api"
import {
  esSesionManual,
  guardarSesionManual,
  leerSesion,
  usuariosService,
} from "@/services/usuariosService"
import type { Sesion, Usuario } from "@/services/usuariosService"
import { cerrarSesionManual } from "@/services/usuariosService"
import { CloseIcon, EyeIcon, EyeOffIcon, UserIcon } from "@/client/components/icons"

type Vista = "login" | "registro" | "recuperar" | "cuenta"

interface Props {
  abierto: boolean
  onCerrar: () => void
}

const CLAVE_RECORDAR = "libreria.auth.recordarme"

function sesionDesdeUsuario(usuario: Usuario, password: string): Sesion {
  return {
    nombre: usuario.nombre,
    username: usuario.username,
    email: usuario.email,
    rut: usuario.rut,
    telefono: usuario.telefono ?? "",
    password,
  }
}

function tituloDeVista(vista: Vista): string {
  switch (vista) {
    case "registro":
      return "Crear cuenta"
    case "recuperar":
      return "Recuperar contraseña"
    case "cuenta":
      return "Mi cuenta"
    default:
      return "Iniciar sesión"
  }
}

export function AuthDrawer({ abierto, onCerrar }: Props) {
  const [vista, setVista] = useState<Vista>("login")
  const [login, setLogin] = useState({ correo: "", password: "" })
  const [registro, setRegistro] = useState({
    nombre: "",
    rut: "",
    email: "",
    telefono: "",
    password: "",
    confirmacion: "",
  })
  const [recuperarCorreo, setRecuperarCorreo] = useState("")
  const [verPassword, setVerPassword] = useState(false)
  const [verConfirmacion, setVerConfirmacion] = useState(false)
  const [recordarme, setRecordarme] = useState(false)
  const [error, setError] = useState("")
  const [errores, setErrores] = useState<Record<string, string>>({})
  const [exito, setExito] = useState("")
  const [enviando, setEnviando] = useState(false)
  const correoRef = useRef<HTMLInputElement>(null)

  // Al abrir: elegir vista y limpiar mensajes.
  useEffect(() => {
    if (!abierto) return
    setError("")
    setErrores({})
    setExito("")
    setEnviando(false)
    setVista(esSesionManual() && leerSesion() ? "cuenta" : "login")
    setRecordarme(localStorage.getItem(CLAVE_RECORDAR) === "1")
    window.setTimeout(() => correoRef.current?.focus(), 340)
  }, [abierto])

  // Cerrar con Escape y bloquear el scroll del fondo.
  useEffect(() => {
    if (!abierto) return
    const alPresionar = (evento: KeyboardEvent): void => {
      if (evento.key === "Escape") onCerrar()
    }
    const overflowPrevio = document.body.style.overflow
    document.body.style.overflow = "hidden"
    document.addEventListener("keydown", alPresionar)
    return () => {
      document.body.style.overflow = overflowPrevio
      document.removeEventListener("keydown", alPresionar)
    }
  }, [abierto, onCerrar])

  const irA = (siguiente: Vista): void => {
    setVista(siguiente)
    setError("")
    setErrores({})
    setExito("")
    if (siguiente === "login") window.setTimeout(() => correoRef.current?.focus(), 50)
  }

  const capturarError = (ex: unknown): void => {
    if (ex instanceof ApiError) {
      setError(ex.message)
      setErrores(ex.errores ?? {})
      return
    }
    setError(mensajeDeError(ex))
  }

  const entrar = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setError("")
    setErrores({})
    if (!login.correo.trim() || !login.password) {
      setError("Ingresa tu correo y tu contraseña.")
      return
    }
    setEnviando(true)
    try {
      const usuario = await usuariosService.login(login.correo.trim(), login.password)
      localStorage.setItem(CLAVE_RECORDAR, recordarme ? "1" : "0")
      guardarSesionManual(sesionDesdeUsuario(usuario, login.password))
      onCerrar()
      window.location.assign("/perfil")
    } catch (ex) {
      capturarError(ex)
    } finally {
      setEnviando(false)
    }
  }

  const crearCuenta = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setError("")
    setErrores({})
    if (!registro.nombre.trim() || !registro.rut.trim() || !registro.email.trim()) {
      setError("Completa tu nombre, RUT y correo electrónico.")
      return
    }
    if (registro.password !== registro.confirmacion) {
      setErrores({ passwordConfirmacion: "Las contraseñas no coinciden." })
      return
    }
    setEnviando(true)
    try {
      const nombreUsuario =
        registro.email
          .split("@")[0]
          .replace(/[^a-zA-Z0-9._-]/g, "")
          .toLowerCase() || `cliente${Date.now()}`
      const usuario = await usuariosService.registrar({
        nombre: registro.nombre.trim(),
        username: nombreUsuario,
        rut: registro.rut.trim(),
        email: registro.email.trim(),
        telefono: registro.telefono.trim(),
        password: registro.password,
        passwordConfirmacion: registro.confirmacion,
      })
      guardarSesionManual(sesionDesdeUsuario(usuario, registro.password))
      onCerrar()
      window.location.assign("/perfil")
    } catch (ex) {
      capturarError(ex)
    } finally {
      setEnviando(false)
    }
  }

  const recuperar = async (evento: FormEvent): Promise<void> => {
    evento.preventDefault()
    setError("")
    setErrores({})
    setExito("")
    if (!recuperarCorreo.trim()) {
      setError("Ingresa el correo de tu cuenta.")
      return
    }
    setEnviando(true)
    try {
      const respuesta = await usuariosService.recuperar(recuperarCorreo.trim())
      setExito(respuesta.mensaje)
    } catch (ex) {
      capturarError(ex)
    } finally {
      setEnviando(false)
    }
  }

  const salir = (): void => {
    cerrarSesionManual()
    onCerrar()
    window.location.assign("/")
  }

  const sesion = vista === "cuenta" ? leerSesion() : null

  return (
    <>
      <div
        className={`auth-overlay${abierto ? " abierto" : ""}`}
        onClick={onCerrar}
        aria-hidden="true"
      />

      <aside
        className={`auth-drawer${abierto ? " abierto" : ""}`}
        role="dialog"
        aria-modal="true"
        aria-hidden={!abierto}
        aria-labelledby="auth-titulo"
      >
        <header className="auth-head">
          <div className="auth-head-left">
            {(vista === "registro" || vista === "recuperar") && (
              <button
                type="button"
                className="auth-back"
                onClick={() => irA("login")}
                aria-label="Volver a iniciar sesión"
              >
                ←
              </button>
            )}
            <h2 id="auth-titulo">{tituloDeVista(vista)}</h2>
          </div>
          <button type="button" className="auth-close" onClick={onCerrar}>
            <CloseIcon />
            <span>Cerrar</span>
          </button>
        </header>

        <div className="auth-body">
          {error && <p className="auth-alert auth-alert--error">{error}</p>}
          {exito && <p className="auth-alert auth-alert--ok">{exito}</p>}

          {vista === "login" && (
            <form className="auth-form" onSubmit={entrar} noValidate>
              <div className="auth-field">
                <label htmlFor="auth-correo">
                  Correo electrónico <span className="auth-required">*</span>
                </label>
                <input
                  id="auth-correo"
                  ref={correoRef}
                  type="email"
                  autoComplete="email"
                  placeholder="tucorreo@ejemplo.cl"
                  value={login.correo}
                  onChange={(evento) =>
                    setLogin((actual) => ({ ...actual, correo: evento.target.value }))
                  }
                  aria-invalid={Boolean(errores.email)}
                />
                {errores.email && <p className="auth-field-error">{errores.email}</p>}
              </div>

              <div className="auth-field">
                <label htmlFor="auth-password">
                  Contraseña <span className="auth-required">*</span>
                </label>
                <div className="auth-input-wrap">
                  <input
                    id="auth-password"
                    type={verPassword ? "text" : "password"}
                    autoComplete="current-password"
                    placeholder="Tu contraseña"
                    value={login.password}
                    onChange={(evento) =>
                      setLogin((actual) => ({ ...actual, password: evento.target.value }))
                    }
                  />
                  <button
                    type="button"
                    className="auth-eye"
                    aria-label={verPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                    aria-pressed={verPassword}
                    onClick={() => setVerPassword((valor) => !valor)}
                  >
                    {verPassword ? <EyeOffIcon /> : <EyeIcon />}
                  </button>
                </div>
              </div>

              <button type="submit" className="auth-submit" disabled={enviando}>
                {enviando ? "Ingresando…" : "Iniciar sesión"}
              </button>

              <div className="auth-row">
                <label className="auth-check">
                  <input
                    type="checkbox"
                    checked={recordarme}
                    onChange={(evento) => setRecordarme(evento.target.checked)}
                  />
                  <span>Recordarme</span>
                </label>
                <button type="button" className="auth-link" onClick={() => irA("recuperar")}>
                  ¿Perdiste tu contraseña?
                </button>
              </div>

              <hr className="auth-divider" />

              <div className="auth-prompt">
                <span className="auth-avatar" aria-hidden>
                  <UserIcon />
                </span>
                <p>¿No tienes cuenta todavía?</p>
                <button type="button" className="auth-link auth-link--strong" onClick={() => irA("registro")}>
                  Crear una cuenta
                </button>
              </div>
            </form>
          )}

          {vista === "registro" && (
            <form className="auth-form" onSubmit={crearCuenta} noValidate>
              <div className="auth-field">
                <label htmlFor="reg-nombre">
                  Nombre completo <span className="auth-required">*</span>
                </label>
                <input
                  id="reg-nombre"
                  type="text"
                  autoComplete="name"
                  placeholder="Tu nombre y apellido"
                  value={registro.nombre}
                  onChange={(evento) =>
                    setRegistro((actual) => ({ ...actual, nombre: evento.target.value }))
                  }
                  aria-invalid={Boolean(errores.nombre)}
                />
                {errores.nombre && <p className="auth-field-error">{errores.nombre}</p>}
              </div>

              <div className="auth-grid">
                <div className="auth-field">
                  <label htmlFor="reg-rut">
                    RUT <span className="auth-required">*</span>
                  </label>
                  <input
                    id="reg-rut"
                    type="text"
                    placeholder="12.345.678-9"
                    value={registro.rut}
                    onChange={(evento) =>
                      setRegistro((actual) => ({ ...actual, rut: evento.target.value }))
                    }
                    aria-invalid={Boolean(errores.rut)}
                  />
                  {errores.rut && <p className="auth-field-error">{errores.rut}</p>}
                </div>

                <div className="auth-field">
                  <label htmlFor="reg-telefono">Teléfono</label>
                  <input
                    id="reg-telefono"
                    type="tel"
                    autoComplete="tel"
                    placeholder="+56 9 1234 5678"
                    value={registro.telefono}
                    onChange={(evento) =>
                      setRegistro((actual) => ({ ...actual, telefono: evento.target.value }))
                    }
                    aria-invalid={Boolean(errores.telefono)}
                  />
                  {errores.telefono && <p className="auth-field-error">{errores.telefono}</p>}
                </div>
              </div>

              <div className="auth-field">
                <label htmlFor="reg-email">
                  Correo electrónico <span className="auth-required">*</span>
                </label>
                <input
                  id="reg-email"
                  type="email"
                  autoComplete="email"
                  placeholder="tucorreo@ejemplo.cl"
                  value={registro.email}
                  onChange={(evento) =>
                    setRegistro((actual) => ({ ...actual, email: evento.target.value }))
                  }
                  aria-invalid={Boolean(errores.email)}
                />
                {errores.email && <p className="auth-field-error">{errores.email}</p>}
              </div>

              <div className="auth-stack">
                <div className="auth-field">
                  <label htmlFor="reg-password">
                    Contraseña <span className="auth-required">*</span>
                  </label>
                  <div className="auth-input-wrap">
                    <input
                      id="reg-password"
                      type={verPassword ? "text" : "password"}
                      autoComplete="new-password"
                      placeholder="Mínimo 8 caracteres"
                      value={registro.password}
                      onChange={(evento) =>
                        setRegistro((actual) => ({ ...actual, password: evento.target.value }))
                      }
                      aria-invalid={Boolean(errores.password)}
                    />
                    <button
                      type="button"
                      className="auth-eye"
                      aria-label={verPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                      onClick={() => setVerPassword((valor) => !valor)}
                    >
                      {verPassword ? <EyeOffIcon /> : <EyeIcon />}
                    </button>
                  </div>
                  {errores.password && <p className="auth-field-error">{errores.password}</p>}
                </div>

                <div className="auth-field">
                  <label htmlFor="reg-confirmacion">
                    Repetir contraseña <span className="auth-required">*</span>
                  </label>
                  <div className="auth-input-wrap">
                    <input
                      id="reg-confirmacion"
                      type={verConfirmacion ? "text" : "password"}
                      autoComplete="new-password"
                      placeholder="Repite la contraseña"
                      value={registro.confirmacion}
                      onChange={(evento) =>
                        setRegistro((actual) => ({ ...actual, confirmacion: evento.target.value }))
                      }
                      aria-invalid={Boolean(errores.passwordConfirmacion)}
                    />
                    <button
                      type="button"
                      className="auth-eye"
                      aria-label={verConfirmacion ? "Ocultar contraseña" : "Mostrar contraseña"}
                      onClick={() => setVerConfirmacion((valor) => !valor)}
                    >
                      {verConfirmacion ? <EyeOffIcon /> : <EyeIcon />}
                    </button>
                  </div>
                  {errores.passwordConfirmacion && (
                    <p className="auth-field-error">{errores.passwordConfirmacion}</p>
                  )}
                </div>
              </div>

              <p className="auth-hint">
                La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un
                número y un símbolo.
              </p>

              <button type="submit" className="auth-submit" disabled={enviando}>
                {enviando ? "Creando cuenta…" : "Crear cuenta"}
              </button>

              <div className="auth-row auth-row--center">
                <span>¿Ya tienes cuenta?</span>
                <button type="button" className="auth-link" onClick={() => irA("login")}>
                  Inicia sesión
                </button>
              </div>
            </form>
          )}

          {vista === "recuperar" && (
            <form className="auth-form" onSubmit={recuperar} noValidate>
              <p className="auth-text">
                Escribe el correo de tu cuenta y te enviaremos una contraseña temporal para volver
                a entrar.
              </p>
              <div className="auth-field">
                <label htmlFor="rec-correo">
                  Correo electrónico <span className="auth-required">*</span>
                </label>
                <input
                  id="rec-correo"
                  type="email"
                  autoComplete="email"
                  placeholder="tucorreo@ejemplo.cl"
                  value={recuperarCorreo}
                  onChange={(evento) => setRecuperarCorreo(evento.target.value)}
                />
              </div>

              <button type="submit" className="auth-submit" disabled={enviando}>
                {enviando ? "Enviando…" : "Enviar contraseña temporal"}
              </button>

              <div className="auth-row auth-row--center">
                <button type="button" className="auth-link" onClick={() => irA("login")}>
                  ← Volver a iniciar sesión
                </button>
              </div>
            </form>
          )}

          {vista === "cuenta" && sesion && (
            <div className="auth-account">
              <span className="auth-avatar auth-avatar--big" aria-hidden>
                <UserIcon />
              </span>
              <h3>{sesion.nombre}</h3>
              <p>{sesion.email}</p>

              <div className="auth-account-links">
                <Link to="/perfil" onClick={onCerrar}>
                  Mi perfil
                </Link>
                <Link to="/pedidos" onClick={onCerrar}>
                  Mis pedidos
                </Link>
                <Link to="/favoritos" onClick={onCerrar}>
                  Mis favoritos
                </Link>
                <Link to="/carrito" onClick={onCerrar}>
                  Mi carrito
                </Link>
              </div>

              <button type="button" className="auth-submit auth-submit--outline" onClick={salir}>
                Cerrar sesión
              </button>
            </div>
          )}
        </div>
      </aside>
    </>
  )
}
