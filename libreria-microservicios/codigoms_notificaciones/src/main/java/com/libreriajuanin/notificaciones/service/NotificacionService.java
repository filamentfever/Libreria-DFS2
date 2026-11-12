package com.libreriajuanin.notificaciones.service;

import com.libreriajuanin.notificaciones.dto.AlertaRequestDTO;
import com.libreriajuanin.notificaciones.dto.ConfirmacionPedidoRequestDTO;
import com.libreriajuanin.notificaciones.dto.NotificacionRequestDTO;
import com.libreriajuanin.notificaciones.dto.NotificacionResponseDTO;
import com.libreriajuanin.notificaciones.dto.RegistroUsuarioRequestDTO;
import com.libreriajuanin.notificaciones.dto.ResultadoColaDTO;
import com.libreriajuanin.notificaciones.exception.RecursoNoEncontradoException;
import com.libreriajuanin.notificaciones.exception.ReglaNegocioException;
import com.libreriajuanin.notificaciones.exception.ValidacionException;
import com.libreriajuanin.notificaciones.model.EstadoEnvio;
import com.libreriajuanin.notificaciones.model.Notificacion;
import com.libreriajuanin.notificaciones.model.TipoEvento;
import com.libreriajuanin.notificaciones.repository.NotificacionRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cola de correos (HU-05, HU-51, HU-63; notificacionService.ts). */
@Service
@Transactional
public class NotificacionService {

    public static final String ASUNTO_BIENVENIDA = "Bienvenido a nuestra papelería";
    public static final int MAX_INTENTOS = 3;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final NotificacionRepository notificacionRepository;
    private final EmisorCorreo emisorCorreo;

    public NotificacionService(NotificacionRepository notificacionRepository, EmisorCorreo emisorCorreo) {
        this.notificacionRepository = notificacionRepository;
        this.emisorCorreo = emisorCorreo;
    }

    // ------------------------------------------------------------ encolar

    /** Encola una notificación genérica (queda PENDIENTE hasta que se procese la cola). */
    public NotificacionResponseDTO encolar(NotificacionRequestDTO dto) {
        TipoEvento tipo;
        try {
            tipo = TipoEvento.valueOf(dto.getTipoEvento() == null ? "" : dto.getTipoEvento().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("Tipo de evento inválido: " + dto.getTipoEvento());
        }
        return encolar(tipo, dto.getDestinatario(), dto.getAsunto(), dto.getMensaje());
    }

    /** HU-05: correo «Bienvenido a nuestra papelería». */
    public NotificacionResponseDTO encolarRegistroUsuario(RegistroUsuarioRequestDTO dto) {
        String nombre = dto.getNombre() == null || dto.getNombre().isBlank() ? "" : " " + dto.getNombre().trim();
        return encolar(TipoEvento.REGISTRO_USUARIO, dto.getEmail(), ASUNTO_BIENVENIDA,
                "Hola" + nombre + ",\n\n¡Tu cuenta en Librería Juanin se creó correctamente! Ya puedes iniciar sesión y comprar.\n\nGracias por registrarte.");
    }

    /** HU-63: comprobante con el detalle y el total del pedido. */
    public NotificacionResponseDTO encolarConfirmacionPedido(ConfirmacionPedidoRequestDTO dto) {
        if (dto.getNumeroPedido() == null || dto.getNumeroPedido().isBlank()) {
            throw new ValidacionException(Map.of("numeroPedido", "Indica el número de pedido."));
        }
        String total = dto.getTotal() == null ? "" : "\nTotal: $" + dto.getTotal().toPlainString();
        String detalle = dto.getDetalle() == null || dto.getDetalle().isBlank() ? "" : "\n\n" + dto.getDetalle().trim();
        return encolar(TipoEvento.CONFIRMACION_PEDIDO, dto.getEmail(), "Confirmación de tu pedido " + dto.getNumeroPedido().trim(),
                "Recibimos tu pedido " + dto.getNumeroPedido().trim() + "." + total + detalle);
    }

    /** Alertas internas (por ejemplo, stock crítico) o avisos como «tu pedido está listo para retiro». */
    public NotificacionResponseDTO encolarAlerta(AlertaRequestDTO dto) {
        String asunto = dto.getAsunto() == null || dto.getAsunto().isBlank() ? "Alerta" : dto.getAsunto().trim();
        return encolar(TipoEvento.ALERTA_GENERAL, dto.getDestinatario(), asunto, dto.getMensaje());
    }

    // ------------------------------------------------------------ envío

    /** Envía una notificación PENDIENTE o reintenta una FALLIDA. Una ya ENVIADA no se vuelve a enviar. */
    public NotificacionResponseDTO enviar(Long id) {
        Notificacion n = buscar(id);
        if (n.getEstadoEnvio() == EstadoEnvio.ENVIADO) {
            return aDTO(n);
        }
        if (n.getEstadoEnvio() == EstadoEnvio.FALLIDO && n.getIntentos() >= MAX_INTENTOS) {
            throw new ReglaNegocioException("Se superó el máximo de " + MAX_INTENTOS + " intentos de envío");
        }
        n.setIntentos(n.getIntentos() + 1);
        try {
            emisorCorreo.enviar(n.getDestinatario(), n.getAsunto(), n.getMensaje());
            n.setEstadoEnvio(EstadoEnvio.ENVIADO);
            n.setFechaEnvio(LocalDateTime.now());
        } catch (RuntimeException e) {
            n.setEstadoEnvio(EstadoEnvio.FALLIDO);
        }
        return aDTO(notificacionRepository.save(n));
    }

    /** Procesa toda la cola: intenta enviar cada notificación PENDIENTE. */
    public ResultadoColaDTO procesarCola() {
        ResultadoColaDTO r = new ResultadoColaDTO();
        for (Notificacion n : notificacionRepository.findByEstadoEnvio(EstadoEnvio.PENDIENTE)) {
            NotificacionResponseDTO resultado = enviar(n.getId());
            r.setProcesadas(r.getProcesadas() + 1);
            if ("ENVIADO".equals(resultado.getEstadoEnvio())) r.setEnviadas(r.getEnviadas() + 1);
            else r.setFallidas(r.getFallidas() + 1);
        }
        return r;
    }

    // ------------------------------------------------------------ consulta

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listar(String estado, String destinatario) {
        EstadoEnvio filtro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoEnvio.valueOf(estado.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException("Estado inválido: " + estado);
            }
        }
        final EstadoEnvio buscado = filtro;
        return notificacionRepository.findAll().stream()
                .filter(n -> buscado == null || n.getEstadoEnvio() == buscado)
                .filter(n -> destinatario == null || destinatario.isBlank() || n.getDestinatario().equalsIgnoreCase(destinatario.trim()))
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificacionResponseDTO obtener(Long id) {
        return aDTO(buscar(id));
    }

    // ------------------------------------------------------------ auxiliares

    private NotificacionResponseDTO encolar(TipoEvento tipo, String destinatario, String asunto, String mensaje) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (destinatario == null || !EMAIL.matcher(destinatario.trim()).matches()) {
            errores.put("destinatario", "Ingresa un correo válido.");
        }
        if (mensaje == null || mensaje.isBlank()) {
            errores.put("mensaje", "El mensaje no puede estar vacío.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        Notificacion n = new Notificacion();
        n.setTipoEvento(tipo);
        n.setDestinatario(destinatario.trim());
        n.setAsunto(asunto == null || asunto.isBlank() ? tipo.name() : asunto.trim());
        n.setMensaje(mensaje.trim());
        n.setEstadoEnvio(EstadoEnvio.PENDIENTE);
        n.setFechaCreacion(LocalDateTime.now());
        return aDTO(notificacionRepository.save(n));
    }

    public NotificacionResponseDTO aDTO(Notificacion n) {
        NotificacionResponseDTO dto = new NotificacionResponseDTO();
        dto.setId(n.getId());
        dto.setTipoEvento(n.getTipoEvento().name());
        dto.setDestinatario(n.getDestinatario());
        dto.setAsunto(n.getAsunto());
        dto.setMensaje(n.getMensaje());
        dto.setEstadoEnvio(n.getEstadoEnvio().name());
        dto.setIntentos(n.getIntentos());
        dto.setFechaCreacion(n.getFechaCreacion());
        dto.setFechaEnvio(n.getFechaEnvio());
        return dto;
    }

    private Notificacion buscar(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La notificación " + id + " no existe"));
    }
}
