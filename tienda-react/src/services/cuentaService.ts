// Compatibilidad: la lógica de cuenta vive ahora en usuariosService
// (un archivo de servicio por microservicio).
export {
  cerrarSesion,
  guardarSesion,
  leerSesion,
  obtenerOCrearUsuario,
  usuariosService,
} from "@/services/usuariosService"
export type { Direccion, RegistroPayload, Sesion, Usuario } from "@/services/usuariosService"
