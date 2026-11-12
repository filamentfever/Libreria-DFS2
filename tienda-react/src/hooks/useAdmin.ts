// Hook de MSCI-ADMIN: dashboard y métricas del negocio.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { adminService } from "@/services/adminService"
import type { Dashboard, EstadoStock, ReporteVentas } from "@/services/adminService"

export function useAdmin() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [stock, setStock] = useState<EstadoStock | null>(null)
  const [ventas, setVentas] = useState<ReporteVentas | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState("")
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let activo = true
    setCargando(true)
    setError("")

    Promise.all([
      adminService.dashboard(),
      adminService.stock(),
      adminService.ventas(),
    ])
      .then(([resumen, estadoStock, reporteVentas]) => {
        if (!activo) return
        setDashboard(resumen)
        setStock(estadoStock)
        setVentas(reporteVentas)
      })
      .catch((ex) => {
        if (activo) setError(mensajeDeError(ex))
      })
      .finally(() => {
        if (activo) setCargando(false)
      })

    return () => {
      activo = false
    }
  }, [version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])

  return { dashboard, stock, ventas, cargando, error, recargar }
}
