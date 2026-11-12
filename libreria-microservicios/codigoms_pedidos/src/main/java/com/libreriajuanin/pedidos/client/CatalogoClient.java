package com.libreriajuanin.pedidos.client;

import com.libreriajuanin.pedidos.dto.ProductoDTO;
import com.libreriajuanin.pedidos.dto.StockRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Conexión HTTP con codigoms_catalogo. */
@FeignClient(name = "catalogo-client", url = "${clients.catalogo.url}")
public interface CatalogoClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable("id") Long id);

    @PostMapping("/api/productos/{id}/descontar-stock")
    ProductoDTO descontarStock(@PathVariable("id") Long id, @RequestBody StockRequestDTO dto);
}
