package com.libreriajuanin.admin.client;

import com.libreriajuanin.admin.dto.ProductoDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/** Conexión HTTP con codigoms_catalogo: GET /api/productos/admin (inventario completo, también inactivos). */
@FeignClient(name = "catalogo-client", url = "${clients.catalogo.url}")
public interface CatalogoClient {

    @GetMapping("/api/productos/admin")
    List<ProductoDTO> listarInventario();
}
