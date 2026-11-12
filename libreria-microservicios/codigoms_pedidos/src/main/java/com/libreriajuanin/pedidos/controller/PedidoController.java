package com.libreriajuanin.pedidos.controller;

import com.libreriajuanin.pedidos.dto.CheckoutRequestDTO;
import com.libreriajuanin.pedidos.dto.ComprobanteTransferenciaRequestDTO;
import com.libreriajuanin.pedidos.dto.CotizacionEnvioDTO;
import com.libreriajuanin.pedidos.dto.EstadoRequestDTO;
import com.libreriajuanin.pedidos.dto.PedidoResponseDTO;
import com.libreriajuanin.pedidos.exception.ReglaNegocioException;
import com.libreriajuanin.pedidos.model.EstadoPedido;
import com.libreriajuanin.pedidos.service.PedidoService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Checkout: toma el carrito, cobra, crea el pedido, descuenta stock y vacía el carrito. */
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(@RequestBody CheckoutRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(dto));
    }

    /** Panel admin: ?estado=ENVIADO&desde=2026-03-01&hasta=2026-03-31 */
    @GetMapping
    public List<PedidoResponseDTO> listar(
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        EstadoPedido filtro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoPedido.valueOf(estado.trim().toUpperCase().replace(' ', '_'));
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException("Estado inválido: " + estado);
            }
        }
        return pedidoService.listar(filtro, desde, hasta);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO obtener(@PathVariable("id") Long id) {
        return pedidoService.obtenerPorId(id);
    }

    @GetMapping("/numero/{numero}")
    public PedidoResponseDTO obtenerPorNumero(@PathVariable("numero") String numero) {
        return pedidoService.obtenerPorNumero(numero);
    }

    @GetMapping("/usuario/{usuario}")
    public List<PedidoResponseDTO> historial(@PathVariable("usuario") String usuario) {
        return pedidoService.listarPorUsuario(usuario);
    }

    @GetMapping("/envio/{comuna}")
    public CotizacionEnvioDTO cotizarEnvio(@PathVariable("comuna") String comuna) {
        return pedidoService.cotizarEnvio(comuna);
    }

    @PatchMapping("/{id}/estado")
    public PedidoResponseDTO cambiarEstado(@PathVariable("id") Long id, @RequestBody EstadoRequestDTO dto) {
        return pedidoService.cambiarEstado(id, dto.getEstado());
    }

    @PutMapping("/{id}/comprobante-transferencia")
    public PedidoResponseDTO comprobanteTransferencia(@PathVariable("id") Long id,
                                                      @RequestBody ComprobanteTransferenciaRequestDTO dto) {
        return pedidoService.adjuntarComprobanteTransferencia(id, dto.getArchivo());
    }
}
