package com.libreriajuanin.usuarios.controller;

import com.libreriajuanin.usuarios.dto.ActualizarUsuarioRequestDTO;
import com.libreriajuanin.usuarios.dto.DireccionDTO;
import com.libreriajuanin.usuarios.dto.DireccionRequestDTO;
import com.libreriajuanin.usuarios.dto.LoginRequestDTO;
import com.libreriajuanin.usuarios.dto.RecuperacionRequestDTO;
import com.libreriajuanin.usuarios.dto.RecuperacionResponseDTO;
import com.libreriajuanin.usuarios.dto.RegistroRequestDTO;
import com.libreriajuanin.usuarios.dto.UsuarioResponseDTO;
import com.libreriajuanin.usuarios.service.UsuarioService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@RequestBody RegistroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(dto));
    }

    @PostMapping("/login")
    public UsuarioResponseDTO login(@RequestBody LoginRequestDTO dto) {
        return usuarioService.login(dto);
    }

    @PostMapping("/recuperar")
    public RecuperacionResponseDTO recuperar(@RequestBody RecuperacionRequestDTO dto) {
        return usuarioService.recuperarPassword(dto.getEmail());
    }

    /** Total de usuarios registrados: { "total": 12 } */
    @GetMapping("/total")
    public Map<String, Long> total() {
        return Map.of("total", usuarioService.contarUsuarios());
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO obtener(@PathVariable("id") Long id) {
        return usuarioService.obtenerPorId(id);
    }

    /** Endpoint que consume codigoms_carrito (UsuariosClient). */
    @GetMapping("/nombre/{nombre}")
    public UsuarioResponseDTO buscarPorNombre(@PathVariable("nombre") String nombre) {
        return usuarioService.buscarPorUsername(nombre);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO actualizar(@PathVariable("id") Long id, @RequestBody ActualizarUsuarioRequestDTO dto) {
        return usuarioService.actualizar(id, dto);
    }

    @GetMapping("/{id}/direcciones")
    public List<DireccionDTO> listarDirecciones(@PathVariable("id") Long id) {
        return usuarioService.listarDirecciones(id);
    }

    @PostMapping("/{id}/direcciones")
    public ResponseEntity<List<DireccionDTO>> agregarDireccion(@PathVariable("id") Long id,
                                                               @RequestBody DireccionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.agregarDireccion(id, dto));
    }

    @PutMapping("/{id}/direcciones/{direccionId}")
    public List<DireccionDTO> editarDireccion(@PathVariable("id") Long id,
                                              @PathVariable("direccionId") Long direccionId,
                                              @RequestBody DireccionRequestDTO dto) {
        return usuarioService.editarDireccion(id, direccionId, dto);
    }

    @PutMapping("/{id}/direcciones/{direccionId}/predeterminada")
    public List<DireccionDTO> marcarPredeterminada(@PathVariable("id") Long id,
                                                   @PathVariable("direccionId") Long direccionId) {
        return usuarioService.marcarPredeterminada(id, direccionId);
    }

    @DeleteMapping("/{id}/direcciones/{direccionId}")
    public List<DireccionDTO> eliminarDireccion(@PathVariable("id") Long id,
                                                @PathVariable("direccionId") Long direccionId) {
        return usuarioService.eliminarDireccion(id, direccionId);
    }
}
