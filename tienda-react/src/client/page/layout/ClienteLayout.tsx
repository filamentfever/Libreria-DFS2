// Layout maestro del área de CLIENTE: cabecera + contenido dinámico + pie.
import { Outlet } from "react-router"
import { SiteFooter } from "@/client/components/SiteFooter"
import { SiteHeader } from "@/client/components/SiteHeader"

export const ClienteLayout = () => {
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
