package com.libreriajuanin.usuarios.service;

import com.libreriajuanin.usuarios.dto.ActualizarUsuarioRequestDTO;
import com.libreriajuanin.usuarios.dto.DireccionDTO;
import com.libreriajuanin.usuarios.dto.DireccionRequestDTO;
import com.libreriajuanin.usuarios.dto.LoginRequestDTO;
import com.libreriajuanin.usuarios.dto.RecuperacionResponseDTO;
import com.libreriajuanin.usuarios.dto.RegistroRequestDTO;
import com.libreriajuanin.usuarios.dto.UsuarioResponseDTO;
import com.libreriajuanin.usuarios.exception.RecursoNoEncontradoException;
import com.libreriajuanin.usuarios.exception.ReglaNegocioException;
import com.libreriajuanin.usuarios.exception.ValidacionException;
import com.libreriajuanin.usuarios.model.Direccion;
import com.libreriajuanin.usuarios.model.Usuario;
import com.libreriajuanin.usuarios.repository.UsuarioRepository;
import com.libreriajuanin.usuarios.util.Validaciones;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cuentas de usuario (HU-01 a HU-09). */
@Service
@Transactional
public class UsuarioService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------ HU-01

    /** HU-01: valida todos los campos y, si están bien, guarda la cuenta (sin iniciar sesión). */
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (dto.getNombre() == null || dto.getNombre().trim().length() < 3) {
            errores.put("nombre", "Ingresa tu nombre completo (mínimo 3 caracteres).");
        }
        if (!Validaciones.rutValido(dto.getRut())) {
            errores.put("rut", "El RUT no es válido.");
        } else if (usuarioRepository.existsByRut(Validaciones.limpiarRut(dto.getRut()))) {
            errores.put("rut", "Ya existe una cuenta con este RUT.");
        }
        if (!Validaciones.emailValido(dto.getEmail())) {
            errores.put("email", "Ingresa un correo válido.");
        } else if (usuarioRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            errores.put("email", "Ya existe una cuenta con este correo.");
        }
        if (!Validaciones.telefonoValido(dto.getTelefono())) {
            errores.put("telefono", "Ingresa un teléfono válido.");
        }
        if (!Validaciones.passwordValida(dto.getPassword())) {
            errores.put("password", "La contraseña no cumple los requisitos: "
                    + String.join(", ", Validaciones.reglasPasswordIncumplidas(dto.getPassword())) + ".");
        }
        if (dto.getPassword() == null || !dto.getPassword().equals(dto.getPasswordConfirmacion())) {
            errores.put("passwordConfirmacion", "Las contraseñas no coinciden.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setRut(Validaciones.limpiarRut(dto.getRut()));
        usuario.setEmail(dto.getEmail().trim());
        usuario.setTelefono(dto.getTelefono() == null || dto.getTelefono().isBlank() ? null : dto.getTelefono().trim());
        usuario.setUsername(generarUsername(dto));
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setFechaRegistro(LocalDateTime.now());
        return aDTO(usuarioRepository.save(usuario));
    }

    // ------------------------------------------------------------ HU-03

    /** HU-03: credenciales correctas → devuelve el usuario; si no, un mensaje genérico. */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.getEmail() == null ? "" : dto.getEmail().trim())
                .orElseThrow(() -> new ReglaNegocioException("Correo o contraseña incorrectos"));
        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new ReglaNegocioException("Correo o contraseña incorrectos");
        }
        return aDTO(usuario);
    }

    // ------------------------------------------------------------ HU-02

    /** HU-02: genera una contraseña temporal (válida) y la guarda cifrada. */
    public RecuperacionResponseDTO recuperarPassword(String email) {
        if (!Validaciones.emailValido(email)) {
            throw new ReglaNegocioException("Ingresa un correo válido");
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta registrada con ese correo"));
        String temporal = generarPasswordTemporal();
        usuario.setPasswordHash(passwordEncoder.encode(temporal));
        usuario.setDebeCambiarPassword(true);
        usuarioRepository.save(usuario);
        RecuperacionResponseDTO respuesta = new RecuperacionResponseDTO();
        respuesta.setMensaje("Generamos una contraseña temporal para tu cuenta.");
        // Prototipo sin servidor de correo: se devuelve para mostrarla en pantalla.
        respuesta.setPasswordTemporal(temporal);
        return respuesta;
    }

    // ------------------------------------------------------- consultas

    /** Total de usuarios registrados (lo consume el dashboard de codigoms_admin). */
    @Transactional(readOnly = true)
    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    /** Usado por codigoms_carrito (UsuariosClient): busca por nombre de usuario. */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorUsername(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .map(this::aDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario '" + username + "' no existe"));
    }

    // ------------------------------------------------------------ HU-09

    /** HU-09: edita nombre, correo y teléfono. */
    public UsuarioResponseDTO actualizar(Long id, ActualizarUsuarioRequestDTO dto) {
        Usuario usuario = buscar(id);
        Map<String, String> errores = new LinkedHashMap<>();
        if (dto.getNombre() == null || dto.getNombre().trim().length() < 3) {
            errores.put("nombre", "Ingresa tu nombre completo (mínimo 3 caracteres).");
        }
        if (!Validaciones.emailValido(dto.getEmail())) {
            errores.put("email", "Ingresa un correo válido.");
        } else if (!dto.getEmail().trim().equalsIgnoreCase(usuario.getEmail())
                && usuarioRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            errores.put("email", "Ya existe una cuenta con este correo.");
        }
        if (!Validaciones.telefonoValido(dto.getTelefono())) {
            errores.put("telefono", "Ingresa un teléfono válido.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(dto.getEmail().trim());
        usuario.setTelefono(dto.getTelefono() == null || dto.getTelefono().isBlank() ? null : dto.getTelefono().trim());
        return aDTO(usuarioRepository.save(usuario));
    }

    // ------------------------------------------------- HU-06 y HU-07

    @Transactional(readOnly = true)
    public List<DireccionDTO> listarDirecciones(Long usuarioId) {
        return buscar(usuarioId).getDirecciones().stream().map(this::aDTO).toList();
    }

    /** HU-06/HU-07: agrega una dirección; la primera queda como predeterminada. */
    public List<DireccionDTO> agregarDireccion(Long usuarioId, DireccionRequestDTO dto) {
        Usuario usuario = buscar(usuarioId);
        validarDireccion(dto.getDireccion());
        boolean primera = usuario.getDirecciones().isEmpty();
        boolean predeterminada = primera || dto.isPredeterminada();
        if (predeterminada) {
            usuario.getDirecciones().forEach(d -> d.setPredeterminada(false));
        }
        usuario.getDirecciones().add(new Direccion(
                textoOAlias(dto.getAlias()), dto.getDireccion().trim(), dto.getComuna(), predeterminada, usuario));
        return guardarYListar(usuario);
    }

    /** HU-07: edita una dirección existente. */
    public List<DireccionDTO> editarDireccion(Long usuarioId, Long direccionId, DireccionRequestDTO dto) {
        Usuario usuario = buscar(usuarioId);
        Direccion direccion = buscarDireccion(usuario, direccionId);
        validarDireccion(dto.getDireccion());
        direccion.setAlias(textoOAlias(dto.getAlias()));
        direccion.setDireccion(dto.getDireccion().trim());
        direccion.setComuna(dto.getComuna());
        return guardarYListar(usuario);
    }

    /** HU-07: elimina una dirección; si era la predeterminada, la primera restante pasa a serlo. */
    public List<DireccionDTO> eliminarDireccion(Long usuarioId, Long direccionId) {
        Usuario usuario = buscar(usuarioId);
        Direccion direccion = buscarDireccion(usuario, direccionId);
        usuario.getDirecciones().remove(direccion);
        if (direccion.isPredeterminada() && !usuario.getDirecciones().isEmpty()) {
            usuario.getDirecciones().get(0).setPredeterminada(true);
        }
        return guardarYListar(usuario);
    }

    /** HU-07: marca una dirección como predeterminada (solo una a la vez). */
    public List<DireccionDTO> marcarPredeterminada(Long usuarioId, Long direccionId) {
        Usuario usuario = buscar(usuarioId);
        Direccion elegida = buscarDireccion(usuario, direccionId);
        usuario.getDirecciones().forEach(d -> d.setPredeterminada(d == elegida));
        return guardarYListar(usuario);
    }

    // ---------------------------------------------------------- auxiliares

    public UsuarioResponseDTO aDTO(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setNombre(u.getNombre());
        dto.setRut(Validaciones.formatearRut(u.getRut()));
        dto.setEmail(u.getEmail());
        dto.setTelefono(u.getTelefono());
        dto.setDebeCambiarPassword(u.isDebeCambiarPassword());
        u.getDirecciones().forEach(d -> dto.getDirecciones().add(aDTO(d)));
        return dto;
    }

    private DireccionDTO aDTO(Direccion d) {
        DireccionDTO dto = new DireccionDTO();
        dto.setId(d.getId());
        dto.setAlias(d.getAlias());
        dto.setDireccion(d.getDireccion());
        dto.setComuna(d.getComuna());
        dto.setPredeterminada(d.isPredeterminada());
        return dto;
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario " + id + " no existe"));
    }

    private Direccion buscarDireccion(Usuario usuario, Long direccionId) {
        return usuario.getDirecciones().stream()
                .filter(d -> direccionId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("La dirección " + direccionId + " no existe"));
    }

    private List<DireccionDTO> guardarYListar(Usuario usuario) {
        return usuarioRepository.save(usuario).getDirecciones().stream().map(this::aDTO).toList();
    }

    private void validarDireccion(String direccion) {
        if (direccion == null || direccion.trim().length() < 5) {
            throw new ReglaNegocioException("Ingresa una dirección válida (calle y número)");
        }
    }

    private String textoOAlias(String alias) {
        return alias == null || alias.isBlank() ? "Dirección" : alias.trim();
    }

    /** Si no se indica nombre de usuario se deriva del correo y se hace único con un número. */
    private String generarUsername(RegistroRequestDTO dto) {
        String base = dto.getUsername() != null && !dto.getUsername().isBlank()
                ? dto.getUsername().trim()
                : dto.getEmail().trim().split("@")[0];
        base = base.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        if (base.isEmpty()) base = "usuario";
        String candidato = base;
        int n = 2;
        while (usuarioRepository.existsByUsernameIgnoreCase(candidato)) {
            candidato = base + n++;
        }
        return candidato;
    }

    /** Cumple todas las reglas de contraseña: mayúscula, minúsculas, número y símbolo. */
    private String generarPasswordTemporal() {
        String minus = "abcdefghjkmnpqrstuvwxyz";
        String mayus = "ABCDEFGHJKMNPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        sb.append(mayus.charAt(ALEATORIO.nextInt(mayus.length())));
        for (int i = 0; i < 4; i++) sb.append(minus.charAt(ALEATORIO.nextInt(minus.length())));
        sb.append(1000 + ALEATORIO.nextInt(9000));
        sb.append("!@#$%".charAt(ALEATORIO.nextInt(5)));
        return sb.toString();
    }
}
