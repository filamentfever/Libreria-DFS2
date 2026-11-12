package com.libreriajuanin.envios.controller;

import com.libreriajuanin.envios.dto.InstruccionesRetiroDTO;
import com.libreriajuanin.envios.dto.PuntoRetiroDTO;
import com.libreriajuanin.envios.dto.PuntoRetiroRequestDTO;
import com.libreriajuanin.envios.dto.SeleccionRetiroDTO;
import com.libreriajuanin.envios.service.RetiroService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/retiros")
public class RetiroController {

    private final RetiroService retiroService;

    public RetiroController(RetiroService retiroService) {
        this.retiroService = retiroService;
    }

    /** Consulta de puntos de retiro: GET /api/retiros/puntos?ciudad=Santiago */
    @GetMapping("/puntos")
    public List<PuntoRetiroDTO> puntos(@RequestParam(name = "ciudad", required = false) String ciudad) {
        return retiroService.listarPuntos(ciudad);
    }

    @GetMapping("/ciudades")
    public List<String> ciudades() {
        return retiroService.listarCiudades();
    }

    @GetMapping("/puntos/{id}")
    public PuntoRetiroDTO obtener(@PathVariable("id") Long id) {
        return retiroService.obtenerPunto(id);
    }

    /** Elegir sucursal: envío $0 y dirección temporal autocompletada. */
    @PostMapping("/puntos/{id}/seleccionar")
    public SeleccionRetiroDTO seleccionar(@PathVariable("id") Long id) {
        return retiroService.seleccionar(id);
    }

    /** Instrucciones y código de retiro de un pedido: GET /api/retiros/instrucciones?pedido=ORD-1001&punto=1 */
    @GetMapping("/instrucciones")
    public InstruccionesRetiroDTO instrucciones(@RequestParam(name = "pedido") String pedido,
                                                @RequestParam(name = "punto") Long punto) {
        return retiroService.instrucciones(pedido, punto);
    }

    @PostMapping("/puntos")
    public ResponseEntity<PuntoRetiroDTO> crear(@RequestBody PuntoRetiroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(retiroService.crearPunto(dto));
    }

    /** Body: { "activo": false } */
    @PatchMapping("/puntos/{id}/activo")
    public PuntoRetiroDTO cambiarActivo(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> body) {
        return retiroService.cambiarActivo(id, Boolean.TRUE.equals(body.get("activo")));
    }
}
