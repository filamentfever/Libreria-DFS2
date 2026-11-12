package com.libreriajuanin.resenas.controller;

import com.libreriajuanin.resenas.dto.ResenaRequestDTO;
import com.libreriajuanin.resenas.dto.ResenaResponseDTO;
import com.libreriajuanin.resenas.dto.ResumenValoracionDTO;
import com.libreriajuanin.resenas.service.ResenaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @PostMapping
    public ResponseEntity<ResenaResponseDTO> crear(@RequestBody ResenaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.crear(dto));
    }

    @GetMapping("/producto/{idProducto}")
    public List<ResenaResponseDTO> listar(@PathVariable("idProducto") Long idProducto) {
        return resenaService.listarPorProducto(idProducto);
    }

    @GetMapping("/producto/{idProducto}/promedio")
    public ResumenValoracionDTO promedio(@PathVariable("idProducto") Long idProducto) {
        return resenaService.promedio(idProducto);
    }
}
