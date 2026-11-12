package com.libreriajuanin.resenas.client;

import com.libreriajuanin.resenas.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Conexión HTTP con codigoms_catalogo: confirma que el producto existe. */
@FeignClient(name = "catalogo-client", url = "${clients.catalogo.url}")
public interface CatalogoClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProducto(@PathVariable("id") Long id);
}
