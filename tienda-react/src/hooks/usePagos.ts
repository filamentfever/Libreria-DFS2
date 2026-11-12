// Hook de MSCI-PAGOS: cobro simulado con tarjeta, cupones y datos de transferencia.
import { useCallback, useEffect, useState } from "react"
import { mensajeDeError } from "@/services/api"
import { pagosService } from "@/services/pagosService"
import type {
  CuponValidacion,
  DatosTransferencia,
  PagoTarjetaPayload,
  ResultadoPago,
} from "@/services/pagosService"

export function usePagos() {
  const [resultado, setResultado] = useState<ResultadoPago | null>(null)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState("")
  const [cupon, setCupon] = useState<CuponValidacion | null>(null)
  const [datosTransferencia, setDatosTransferencia] = useState<DatosTransferencia | null>(null)

  useEffect(() => {
    let activo = true
    pagosService
      .datosTransferencia()
      .then((datos) => {
        if (activo) setDatosTransferencia(datos)
      })
      .catch(() => {
        // Los datos de transferencia son informativos; no bloquean el checkout.
      })
    return () => {
      activo = false
    }
  }, [])

  const pagarTarjeta = useCallback(async (payload: PagoTarjetaPayload) => {
    setProcesando(true)
    setError("")
    setResultado(null)
    try {
      const respuesta = await pagosService.pagarTarjeta(payload)
      setResultado(respuesta)
      return respuesta
    } catch (ex) {
      const texto = mensajeDeError(ex)
      setError(texto)
      throw new Error(texto)
    } finally {
      setProcesando(false)
    }
  }, [])

  const validarCupon = useCallback(async (codigo: string) => {
    setError("")
    setCupon(null)
    try {
      const respuesta = await pagosService.validarCupon(codigo)
      setCupon(respuesta)
      return respuesta
    } catch (ex) {
      setError(mensajeDeError(ex))
      return null
    }
  }, [])

  return {
    resultado,
    procesando,
    error,
    cupon,
    datosTransferencia,
    pagarTarjeta,
    validarCupon,
    limpiar: () => {
      setResultado(null)
      setError("")
      setCupon(null)
    },
  }
}
