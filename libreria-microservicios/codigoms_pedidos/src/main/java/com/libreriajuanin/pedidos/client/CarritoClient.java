package com.libreriajuanin.pedidos.client;

import com.libreriajuanin.pedidos.dto.CarritoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Conexión HTTP con codigoms_carrito. */
@FeignClient(name = "carrito-client", url = "${clients.carrito.url}")
public interface CarritoClient {

    @GetMapping("/api/carritos/{id}")
    CarritoDTO obtenerCarrito(@PathVariable("id") Long id);

    @DeleteMapping("/api/carritos/{id}/items")
    CarritoDTO vaciarCarrito(@PathVariable("id") Long id);
}
