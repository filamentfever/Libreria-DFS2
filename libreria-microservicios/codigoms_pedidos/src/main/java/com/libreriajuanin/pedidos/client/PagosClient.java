package com.libreriajuanin.pedidos.client;

import com.libreriajuanin.pedidos.dto.CuponValidacionDTO;
import com.libreriajuanin.pedidos.dto.PagoTarjetaRequestDTO;
import com.libreriajuanin.pedidos.dto.ResultadoPagoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Conexión HTTP con codigoms_pagos. */
@FeignClient(name = "pagos-client", url = "${clients.pagos.url}")
public interface PagosClient {

    @PostMapping("/api/pagos/tarjeta")
    ResultadoPagoDTO pagarConTarjeta(@RequestBody PagoTarjetaRequestDTO dto);

    @GetMapping("/api/pagos/cupones/{codigo}")
    CuponValidacionDTO validarCupon(@PathVariable("codigo") String codigo);
}
