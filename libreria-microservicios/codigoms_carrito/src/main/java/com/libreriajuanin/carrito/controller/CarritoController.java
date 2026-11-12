package com.libreriajuanin.carrito.controller;

import com.libreriajuanin.carrito.dto.CantidadRequestDTO;
import com.libreriajuanin.carrito.dto.CarritoResponseDTO;
import com.libreriajuanin.carrito.dto.ItemCarritoRequestDTO;
import com.libreriajuanin.carrito.exception.RecursoNoEncontradoException;
import com.libreriajuanin.carrito.service.CarritoService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    /** Body: { "usuario": "juan" } */
    @PostMapping
    public ResponseEntity<CarritoResponseDTO> crear(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.crear(body.get("usuario")));
    }

    @GetMapping("/{id}")
    public CarritoResponseDTO obtener(@PathVariable("id") Long id) {
        return carritoService.obtenerPorId(id)
                .map(carritoService::aDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El carrito " + id + " no existe"));
    }

    @GetMapping("/usuario/{usuario}")
    public CarritoResponseDTO obtenerPorUsuario(@PathVariable("usuario") String usuario) {
        return carritoService.obtenerPorUsuario(usuario)
                .map(carritoService::aDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + usuario + " no tiene carrito"));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CarritoResponseDTO> agregarItem(@PathVariable("id") Long id,
                                                          @RequestBody ItemCarritoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.agregarItem(id, request));
    }

    @PatchMapping("/{id}/items/{itemId}")
    public CarritoResponseDTO cambiarCantidad(@PathVariable("id") Long id,
                                              @PathVariable("itemId") Long itemId,
                                              @RequestBody CantidadRequestDTO request) {
        return carritoService.cambiarCantidad(id, itemId, request.getCantidad());
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public CarritoResponseDTO quitarItem(@PathVariable("id") Long id, @PathVariable("itemId") Long itemId) {
        return carritoService.quitarItem(id, itemId);
    }

    @DeleteMapping("/{id}/items")
    public CarritoResponseDTO vaciar(@PathVariable("id") Long id) {
        return carritoService.vaciar(id);
    }
}
