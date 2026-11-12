package com.libreriajuanin.envios.controller;

import com.libreriajuanin.envios.dto.CotizacionDTO;
import com.libreriajuanin.envios.dto.EnvioRequestDTO;
import com.libreriajuanin.envios.dto.EnvioResponseDTO;
import com.libreriajuanin.envios.dto.EstadoRequestDTO;
import com.libreriajuanin.envios.dto.TrackingDTO;
import com.libreriajuanin.envios.dto.TrackingRequestDTO;
import com.libreriajuanin.envios.service.EnvioService;
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
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    /** Cotización de despacho: GET /api/envios/cotizacion?comuna=Santiago */
    @GetMapping("/cotizacion")
    public CotizacionDTO cotizar(@RequestParam(name = "comuna") String comuna) {
        return envioService.cotizar(comuna);
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> crear(@RequestBody EnvioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.crear(dto));
    }

    @GetMapping("/{id}")
    public EnvioResponseDTO obtener(@PathVariable("id") Long id) {
        return envioService.obtenerPorId(id);
    }

    @GetMapping("/pedido/{numero}")
    public EnvioResponseDTO obtenerPorPedido(@PathVariable("numero") String numero) {
        return envioService.obtenerPorPedido(numero);
    }

    /** Actualización de estado del envío (panel admin). */
    @PatchMapping("/{id}/estado")
    public EnvioResponseDTO cambiarEstado(@PathVariable("id") Long id, @RequestBody EstadoRequestDTO dto) {
        return envioService.cambiarEstado(id, dto.getEstado());
    }

    @PutMapping("/{id}/tracking")
    public EnvioResponseDTO asignarTracking(@PathVariable("id") Long id, @RequestBody TrackingRequestDTO dto) {
        return envioService.asignarTracking(id, dto);
    }

    @GetMapping("/{id}/tracking")
    public TrackingDTO tracking(@PathVariable("id") Long id) {
        return envioService.obtenerTracking(id);
    }
}
