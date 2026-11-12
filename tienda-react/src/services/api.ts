// Cliente HTTP mínimo para hablar con el API Gateway.
// La URL se puede cambiar con VITE_API_URL (ver .env.example).
// Por defecto usa el Gateway (8080), que enruta /api/productos al catálogo (8097)
// y /api/carritos al carrito (8083).

export const API_URL: string = (import.meta.env.VITE_API_URL ?? "http://localhost:8080").replace(
  /\/+$/,
  ""
)

interface ErrorBody {
  mensaje?: string
  errores?: Record<string, string>
}

/** Error de la API con el status HTTP y, si existe, el detalle campo a campo. */
export class ApiError extends Error {
  readonly status: number
  readonly errores: Record<string, string> | null

  constructor(status: number, mensaje: string, errores: Record<string, string> | null = null) {
    super(mensaje)
    this.name = "ApiError"
    this.status = status
    this.errores = errores
  }
}

/** Convierte cualquier excepción en un texto que se pueda mostrar al usuario. */
export function mensajeDeError(error: unknown): string {
  if (error instanceof ApiError || error instanceof Error) {
    return error.message
  }
  return "Ocurrió un error inesperado."
}

async function leerCuerpo(response: Response): Promise<unknown> {
  const texto = await response.text()
  if (!texto) return null
  try {
    return JSON.parse(texto) as unknown
  } catch {
    return texto
  }
}

async function pedir<T>(ruta: string, init: RequestInit = {}): Promise<T> {
  let response: Response

  try {
    response = await fetch(`${API_URL}${ruta}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...init.headers,
      },
    })
  } catch {
    throw new Error(
      `No se pudo conectar con el servidor (${API_URL}). ` +
        "Verifica que el API Gateway (8080) y los microservicios estén corriendo."
    )
  }

  const cuerpo = await leerCuerpo(response)

  if (!response.ok) {
    const detalle =
      typeof cuerpo === "object" && cuerpo !== null ? (cuerpo as ErrorBody) : null
    throw new ApiError(
      response.status,
      detalle?.mensaje ?? `El servidor respondió con status ${response.status}`,
      detalle?.errores ?? null
    )
  }

  return cuerpo as T
}

const conCuerpo = (metodo: string, body?: unknown): RequestInit => ({
  method: metodo,
  body: body === undefined ? undefined : JSON.stringify(body),
})

/** Agrega querystring solo con los valores definidos (evita ?q=undefined). */
export function conQuery(
  ruta: string,
  params: Record<string, string | number | boolean | null | undefined>
): string {
  const query = new URLSearchParams()
  for (const [clave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== null && valor !== "") {
      query.set(clave, String(valor))
    }
  }
  const texto = query.toString()
  return texto ? `${ruta}?${texto}` : ruta
}

export const api = {
  get: <T>(ruta: string): Promise<T> => pedir<T>(ruta),
  post: <T>(ruta: string, body?: unknown): Promise<T> =>
    pedir<T>(ruta, conCuerpo("POST", body)),
  put: <T>(ruta: string, body?: unknown): Promise<T> =>
    pedir<T>(ruta, conCuerpo("PUT", body)),
  patch: <T>(ruta: string, body?: unknown): Promise<T> =>
    pedir<T>(ruta, conCuerpo("PATCH", body)),
  delete: <T>(ruta: string): Promise<T> => pedir<T>(ruta, { method: "DELETE" }),
  /** POST con cuerpo de texto plano (importación de CSV del catálogo). */
  postText: <T>(ruta: string, texto: string): Promise<T> =>
    pedir<T>(ruta, {
      method: "POST",
      body: texto,
      headers: { "Content-Type": "text/plain" },
    }),
}
