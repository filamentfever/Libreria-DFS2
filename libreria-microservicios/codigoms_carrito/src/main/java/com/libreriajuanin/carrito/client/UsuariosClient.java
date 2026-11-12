package com.libreriajuanin.carrito.client;

import com.libreriajuanin.carrito.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Conexión HTTP con codigoms_usuarios: GET /api/usuarios/nombre/{nombre}. Lanza FeignException.NotFound si no existe. */
@FeignClient(name = "usuarios-client", url = "${clients.usuarios.url}")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/nombre/{nombre}")
    UsuarioDTO buscarPorNombre(@PathVariable("nombre") String nombre);
}
