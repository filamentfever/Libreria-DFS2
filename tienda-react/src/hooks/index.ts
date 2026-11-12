// Un hook de frontend por cada microservicio.
export {
  useProductos,
  useCatalogo,
  useProducto,
  useCategorias,
  useDestacados,
} from "@/hooks/useProductos"
export { useCarrito } from "@/hooks/useCarrito"
export { useUsuarios } from "@/hooks/useUsuarios"
export { usePagos } from "@/hooks/usePagos"
export { useFavoritos } from "@/hooks/useFavoritos"
export { usePedidos } from "@/hooks/usePedidos"
export { useCotizacionEnvio, usePuntosRetiro, useEnvio } from "@/hooks/useEnvios"
export { useResenas } from "@/hooks/useResenas"
export { useNotificaciones } from "@/hooks/useNotificaciones"
export { useAdmin } from "@/hooks/useAdmin"
export { useInventario } from "@/hooks/useInventario"
