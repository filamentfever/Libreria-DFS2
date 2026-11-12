package com.libreriajuanin.carrito.client;

import com.libreriajuanin.carrito.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Conexión HTTP con codigoms_catalogo: GET /api/productos/{id}. Lanza FeignException.NotFound si no existe. */
@FeignClient(name = "codigoms-catalogo")
public interface CatalogoClient {

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerProductoPorId(@PathVariable("id") Long id);
}