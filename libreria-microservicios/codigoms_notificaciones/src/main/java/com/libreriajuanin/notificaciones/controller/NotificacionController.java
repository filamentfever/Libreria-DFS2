package com.libreriajuanin.notificaciones.controller;

import com.libreriajuanin.notificaciones.dto.AlertaRequestDTO;
import com.libreriajuanin.notificaciones.dto.ConfirmacionPedidoRequestDTO;
import com.libreriajuanin.notificaciones.dto.NotificacionRequestDTO;
import com.libreriajuanin.notificaciones.dto.NotificacionResponseDTO;
import com.libreriajuanin.notificaciones.dto.RegistroUsuarioRequestDTO;
import com.libreriajuanin.notificaciones.dto.ResultadoColaDTO;
import com.libreriajuanin.notificaciones.service.NotificacionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping
    public ResponseEntity<NotificacionResponseDTO> encolar(@RequestBody NotificacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.encolar(dto));
    }

    @PostMapping("/registro-usuario")
    public ResponseEntity<NotificacionResponseDTO> registroUsuario(@RequestBody RegistroUsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.encolarRegistroUsuario(dto));
    }

    @PostMapping("/confirmacion-pedido")
    public ResponseEntity<NotificacionResponseDTO> confirmacionPedido(@RequestBody ConfirmacionPedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.encolarConfirmacionPedido(dto));
    }

    @PostMapping("/alerta")
    public ResponseEntity<NotificacionResponseDTO> alerta(@RequestBody AlertaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificacionService.encolarAlerta(dto));
    }

    /** Envía (o reintenta) una notificación concreta. */
    @PostMapping("/{id}/enviar")
    public NotificacionResponseDTO enviar(@PathVariable("id") Long id) {
        return notificacionService.enviar(id);
    }

    /** Envía todas las pendientes. */
    @PostMapping("/procesar-cola")
    public ResultadoColaDTO procesarCola() {
        return notificacionService.procesarCola();
    }

    @GetMapping
    public List<NotificacionResponseDTO> listar(@RequestParam(name = "estado", required = false) String estado,
                                                @RequestParam(name = "destinatario", required = false) String destinatario) {
        return notificacionService.listar(estado, destinatario);
    }

    @GetMapping("/{id}")
    public NotificacionResponseDTO obtener(@PathVariable("id") Long id) {
        return notificacionService.obtener(id);
    }
}
