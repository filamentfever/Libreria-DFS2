package com.libreriajuanin.admin.client;

import com.libreriajuanin.admin.dto.TotalUsuariosDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/** Conexión HTTP con codigoms_usuarios: GET /api/usuarios/total. */
@FeignClient(name = "usuarios-client", url = "${clients.usuarios.url}")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/total")
    TotalUsuariosDTO total();
}
