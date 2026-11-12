package com.libreriajuanin.resenas.client;

import com.libreriajuanin.resenas.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Conexión HTTP con codigoms_usuarios: confirma que el usuario existe y trae su nombre. */
@FeignClient(name = "usuarios-client", url = "${clients.usuarios.url}")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtenerUsuario(@PathVariable("id") Long id);
}
