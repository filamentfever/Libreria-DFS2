// Enrutamiento centralizado con createBrowserRouter (react-router v8).
// El área de CLIENTE se anida bajo ClienteLayout; la de ADMIN bajo AdminLayout.
import { AdminPage } from "@/admin/AdminPage"
import { AdminLayout } from "@/admin/layout/AdminLayout"
import { CarritoPage } from "@/client/page/carrito/CarritoPage"
import { CatalogoPage } from "@/client/page/catalogo/CatalogoPage"
import { ContactoPage } from "@/client/page/contacto/ContactoPage"
import { FavoritosPage } from "@/client/page/favoritos/FavoritosPage"
import { HomePage } from "@/client/page/home/HomePage"
import { ClienteLayout } from "@/client/page/layout/ClienteLayout"
import { PedidosPage } from "@/client/page/pedidos/PedidosPage"
import { ProductoPage } from "@/client/page/producto/ProductoPage"
import { PerfilPage } from "@/client/page/usuarios/PerfilPage"
import { createBrowserRouter, Navigate } from "react-router"

export const rootRouter = createBrowserRouter([
  {
    // ========== ÁREA DE CLIENTE ==========
    // Todo lo que cuelga de "/" comparte cabecera + pie de ClienteLayout.
    path: "/",
    element: <ClienteLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "catalogo", element: <CatalogoPage /> },
      { path: "producto/:slug", element: <ProductoPage /> },
      { path: "carrito", element: <CarritoPage /> },
      { path: "favoritos", element: <FavoritosPage /> },
      { path: "pedidos", element: <PedidosPage /> },
      { path: "perfil", element: <PerfilPage /> },
      { path: "contacto", element: <ContactoPage /> },
      // Compatibilidad con la ruta antigua del catálogo.
      { path: "productos", element: <Navigate to="/catalogo" replace /> },
    ],
  },
  {
    // ========== ÁREA DE ADMINISTRACIÓN ==========
    path: "/adm",
    element: <AdminLayout />,
    children: [{ index: true, element: <AdminPage /> }],
  },
  {
    // Cualquier ruta desconocida vuelve a la portada.
    path: "*",
    element: <Navigate to="/" replace />,
  },
])
