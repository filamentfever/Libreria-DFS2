// Layout maestro del área de ADMINISTRACIÓN.
// Conserva la misma cabecera y pie de la tienda para que el cambio de área sea coherente.
import { Outlet } from "react-router"
import { SiteFooter } from "@/client/components/SiteFooter"
import { SiteHeader } from "@/client/components/SiteHeader"

export const AdminLayout = () => {
  return (
    <>
      <SiteHeader />
      <main>
        <Outlet />
      </main>
      <SiteFooter />
    </>
  )
}
