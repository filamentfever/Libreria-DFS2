// ============================================================
// MSCI-USUARIOS · cuenta, sesión y direcciones de despacho
// API Gateway: /api/usuarios/**
// ============================================================

import { api, ApiError } from "@/services/api"

export interface Direccion {
  id: number
  alias: string
  direccion: string
  comuna: string
  predeterminada: boolean
}

export interface Usuario {
  id: number
  username: string
  nombre: string
  rut: string
  email: string
  telefono: string | null
  debeCambiarPassword?: boolean
  direcciones?: Direccion[]
}

export interface RegistroPayload {
  nombre: string
  username: string
  rut: string
  email: string
  telefono: string
  password: string
  passwordConfirmacion: string
}

export interface ActualizarUsuarioPayload {
  nombre: string
  email: string
  telefono: string
}

export interface DireccionPayload {
  alias: string
  direccion: string
  comuna: string
  predeterminada: boolean
}

export interface Sesion {
  nombre: string
  username: string
  email: string
  rut: string
  telefono: string
  password: string
}

export const CLAVE_SESION = "libreria.sesion"

const SESION_DEMO: Sesion = {
  nombre: "Cliente Demo",
  username: "demo",
  email: "demo@libreria.cl",
  rut: "11111111-1",
  telefono: "+56911111111",
  password: "Demo1234!",
}

/** Dígito verificador chileno (módulo 11). */
function calcularDv(cuerpo: string): string {
  let suma = 0
  let multiplo = 2
  for (let i = cuerpo.length - 1; i >= 0; i--) {
    suma += Number(cuerpo[i]) * multiplo
    multiplo = multiplo === 7 ? 2 : multiplo + 1
  }
  const resto = 11 - (suma % 11)
  if (resto === 11) return "0"
  if (resto === 10) return "K"
  return String(resto)
}

function generarRut(): string {
  let cuerpo = String(Math.floor(Math.random() * 9) + 1)
  for (let i = 0; i < 7; i++) {
    cuerpo += String(Math.floor(Math.random() * 10))
  }
  return `${cuerpo}-${calcularDv(cuerpo)}`
}

function crearSesionUnica(): Sesion {
  const sufijo = `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`
  return {
    nombre: "Cliente Invitado",
    username: `invitado_${sufijo}`,
    email: `invitado_${sufijo}@libreria.cl`,
    rut: generarRut(),
    telefono: "+56911111111",
    password: "Demo1234!",
  }
}

export function leerSesion(): Sesion | null {
  try {
    const texto = localStorage.getItem(CLAVE_SESION)
    if (!texto) return null
    const sesion = JSON.parse(texto) as Partial<Sesion>
    if (!sesion.username || !sesion.email || !sesion.password) return null
    return {
      nombre: sesion.nombre ?? "Cliente",
      username: sesion.username,
      email: sesion.email,
      rut: sesion.rut ?? generarRut(),
      telefono: sesion.telefono ?? "+56911111111",
      password: sesion.password,
    }
  } catch {
    return null
  }
}

export function guardarSesion(sesion: Sesion): void {
  localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
}

/** Elimina la sesión local; en el siguiente uso se creará/recuperará la demo. */
export function cerrarSesion(): void {
  localStorage.removeItem(CLAVE_SESION)
}

/** Marca la sesión como iniciada manualmente desde el panel lateral. */
export const CLAVE_SESION_MANUAL = "libreria.sesion.manual"

export function guardarSesionManual(sesion: Sesion): void {
  guardarSesion(sesion)
  localStorage.setItem(CLAVE_SESION_MANUAL, "1")
}

export function esSesionManual(): boolean {
  return localStorage.getItem(CLAVE_SESION_MANUAL) === "1"
}

/** Cierra la sesión manual y deja que la app recupere su sesión de invitado. */
export function cerrarSesionManual(): void {
  cerrarSesion()
  localStorage.removeItem(CLAVE_SESION_MANUAL)
}

export const usuariosService = {
  registrar: (payload: RegistroPayload): Promise<Usuario> =>
    api.post<Usuario>("/api/usuarios/registro", payload),

  login: (email: string, password: string): Promise<Usuario> =>
    api.post<Usuario>("/api/usuarios/login", { email, password }),

  recuperar: (email: string): Promise<{ mensaje: string }> =>
    api.post<{ mensaje: string }>("/api/usuarios/recuperar", { email }),

  obtener: (id: number): Promise<Usuario> => api.get<Usuario>(`/api/usuarios/${id}`),

  buscarPorNombre: (nombre: string): Promise<Usuario> =>
    api.get<Usuario>(`/api/usuarios/nombre/${encodeURIComponent(nombre)}`),

  total: (): Promise<{ total: number }> => api.get<{ total: number }>("/api/usuarios/total"),

  actualizar: (id: number, payload: ActualizarUsuarioPayload): Promise<Usuario> =>
    api.put<Usuario>(`/api/usuarios/${id}`, payload),

  listarDirecciones: (id: number): Promise<Direccion[]> =>
    api.get<Direccion[]>(`/api/usuarios/${id}/direcciones`),

  agregarDireccion: (id: number, payload: DireccionPayload): Promise<Direccion[]> =>
    api.post<Direccion[]>(`/api/usuarios/${id}/direcciones`, payload),

  editarDireccion: (
    id: number,
    direccionId: number,
    payload: DireccionPayload
  ): Promise<Direccion[]> =>
    api.put<Direccion[]>(`/api/usuarios/${id}/direcciones/${direccionId}`, payload),

  marcarPredeterminada: (id: number, direccionId: number): Promise<Direccion[]> =>
    api.put<Direccion[]>(`/api/usuarios/${id}/direcciones/${direccionId}/predeterminada`),

  eliminarDireccion: (id: number, direccionId: number): Promise<Direccion[]> =>
    api.delete<Direccion[]>(`/api/usuarios/${id}/direcciones/${direccionId}`),
}

async function registrar(sesion: Sesion): Promise<Usuario> {
  return usuariosService.registrar({
    nombre: sesion.nombre,
    username: sesion.username,
    rut: sesion.rut,
    email: sesion.email,
    telefono: sesion.telefono,
    password: sesion.password,
    passwordConfirmacion: sesion.password,
  })
}

/**
 * Devuelve el usuario con el que opera el carrito.
 * 1. Si ya hay una sesión guardada, la reutiliza.
 * 2. Si no, intenta la cuenta demo; si el username ya existe la recupera.
 * 3. Si el correo/RUT de la demo está ocupado por otra cuenta, crea una cuenta única.
 */
export async function obtenerOCrearUsuario(): Promise<Usuario> {
  const guardada = leerSesion()
  const sesion = guardada ?? SESION_DEMO

  try {
    const usuario = await usuariosService.buscarPorNombre(sesion.username)
    guardarSesion(sesion)
    return usuario
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 404) throw error
  }

  try {
    const usuario = await registrar(sesion)
    guardarSesion(sesion)
    return usuario
  } catch (error) {
    if (!(error instanceof ApiError)) throw error

    try {
      const usuario = await usuariosService.login(sesion.email, sesion.password)
      guardarSesion(sesion)
      return usuario
    } catch {
      const nueva = crearSesionUnica()
      const usuario = await registrar(nueva)
      guardarSesion(nueva)
      return usuario
    }
  }
}
