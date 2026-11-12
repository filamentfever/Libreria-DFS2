package com.libreriajuanin.pagos.controller;

import com.libreriajuanin.pagos.dto.CuponValidacionDTO;
import com.libreriajuanin.pagos.dto.DatosTransferenciaDTO;
import com.libreriajuanin.pagos.dto.PagoTarjetaRequestDTO;
import com.libreriajuanin.pagos.dto.ResultadoPagoDTO;
import com.libreriajuanin.pagos.service.PagoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /** Siempre responde 200 con el resultado; el campo "aprobado" indica si se aceptó el pago. */
    @PostMapping("/tarjeta")
    public ResultadoPagoDTO pagarConTarjeta(@RequestBody PagoTarjetaRequestDTO dto) {
        return pagoService.pagarConTarjeta(dto);
    }

    @GetMapping("/cupones/{codigo}")
    public CuponValidacionDTO validarCupon(@PathVariable("codigo") String codigo) {
        return pagoService.validarCupon(codigo);
    }

    @GetMapping("/transferencia/datos")
    public DatosTransferenciaDTO datosTransferencia() {
        return pagoService.datosTransferencia();
    }
}
