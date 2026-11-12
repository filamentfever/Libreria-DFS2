package com.libreriajuanin.admin.client;

import com.libreriajuanin.admin.dto.PedidoDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/** Conexión HTTP con codigoms_pedidos: GET /api/pedidos (todos los pedidos). */
@FeignClient(name = "pedidos-client", url = "${clients.pedidos.url}")
public interface PedidosClient {

    @GetMapping("/api/pedidos")
    List<PedidoDTO> listarPedidos();
}
