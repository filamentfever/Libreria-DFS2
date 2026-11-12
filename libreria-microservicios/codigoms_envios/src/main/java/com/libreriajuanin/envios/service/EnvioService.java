package com.libreriajuanin.envios.service;

import com.libreriajuanin.envios.dto.CotizacionDTO;
import com.libreriajuanin.envios.dto.EnvioRequestDTO;
import com.libreriajuanin.envios.dto.EnvioResponseDTO;
import com.libreriajuanin.envios.dto.TrackingDTO;
import com.libreriajuanin.envios.dto.TrackingRequestDTO;
import com.libreriajuanin.envios.exception.RecursoNoEncontradoException;
import com.libreriajuanin.envios.exception.ReglaNegocioException;
import com.libreriajuanin.envios.exception.ValidacionException;
import com.libreriajuanin.envios.model.EstadoDespacho;
import com.libreriajuanin.envios.model.Envio;
import com.libreriajuanin.envios.repository.EnvioRepository;
import com.libreriajuanin.envios.util.TarifasDespacho;
import com.libreriajuanin.envios.util.Transportistas;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Despachos a domicilio (HU-39, HU-40, HU-42, HU-43, HU-44, HU-45). */
@Service
@Transactional
public class EnvioService {

    public static final int MAX_LARGO_NOTAS = 200;

    private final EnvioRepository envioRepository;

    public EnvioService(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    /** HU-39, HU-40 y HU-44: costo y plazo de despacho de una comuna. */
    @Transactional(readOnly = true)
    public CotizacionDTO cotizar(String comuna) {
        if (comuna == null || comuna.isBlank()) {
            throw new ReglaNegocioException("Ingresa tu comuna para calcular el envío");
        }
        return TarifasDespacho.cotizar(comuna)
                .orElseThrow(() -> new RecursoNoEncontradoException("Por ahora no tenemos despacho a esa comuna"));
    }

    /** HU-42: valida los datos y crea el despacho (queda PENDIENTE con el costo de la comuna). */
    public EnvioResponseDTO crear(EnvioRequestDTO dto) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (dto.getPedidoNumero() == null || dto.getPedidoNumero().isBlank()) {
            errores.put("pedidoNumero", "Indica el número de pedido.");
        }
        if (dto.getDestinatario() == null || dto.getDestinatario().trim().length() < 3) {
            errores.put("destinatario", "Ingresa tu nombre completo (mínimo 3 caracteres).");
        }
        if (dto.getDireccion() == null || dto.getDireccion().trim().length() < 5) {
            errores.put("direccion", "Ingresa una dirección válida (calle y número).");
        }
        CotizacionDTO cotizacion = null;
        if (dto.getComuna() == null || dto.getComuna().isBlank()) {
            errores.put("comuna", "Selecciona tu comuna.");
        } else {
            cotizacion = TarifasDespacho.cotizar(dto.getComuna()).orElse(null);
            if (cotizacion == null) errores.put("comuna", "Por ahora no tenemos despacho a esa comuna.");
        }
        if (dto.getNotas() != null && dto.getNotas().length() > MAX_LARGO_NOTAS) {
            errores.put("notas", "Las notas no pueden superar " + MAX_LARGO_NOTAS + " caracteres.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        Envio envio = new Envio();
        envio.setPedidoNumero(dto.getPedidoNumero().trim());
        envio.setDestinatario(dto.getDestinatario().trim());
        envio.setDireccion(dto.getDireccion().trim());
        envio.setComuna(cotizacion.getComuna());
        envio.setCosto(cotizacion.getCosto());
        envio.setDiasMin(cotizacion.getDiasMin());
        envio.setDiasMax(cotizacion.getDiasMax());
        envio.setNotas(dto.getNotas() == null || dto.getNotas().isBlank() ? null : dto.getNotas().trim());
        envio.setEstadoDespacho(EstadoDespacho.PENDIENTE);
        envio.setFechaCreacion(LocalDateTime.now());
        return aDTO(envioRepository.save(envio));
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerPorPedido(String pedidoNumero) {
        return envioRepository.findByPedidoNumeroIgnoreCase(pedidoNumero == null ? "" : pedidoNumero.trim())
                .map(this::aDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("El pedido " + pedidoNumero + " no tiene despacho"));
    }

    /**
     * Actualiza el estado del despacho. Reglas: un envío ENTREGADO o CANCELADO ya no cambia, y para pasar a
     * EN_TRANSITO hay que haber asignado el número de seguimiento.
     */
    public EnvioResponseDTO cambiarEstado(Long id, String estado) {
        EstadoDespacho nuevo;
        try {
            nuevo = EstadoDespacho.valueOf(estado == null ? "" : estado.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("Estado inválido: " + estado);
        }
        Envio envio = buscar(id);
        EstadoDespacho actual = envio.getEstadoDespacho();
        if (actual == EstadoDespacho.ENTREGADO || actual == EstadoDespacho.CANCELADO) {
            throw new ReglaNegocioException("El envío ya está " + actual + " y no puede cambiar de estado");
        }
        if (nuevo == EstadoDespacho.EN_TRANSITO && (envio.getNumeroSeguimiento() == null || envio.getNumeroSeguimiento().isBlank())) {
            throw new ReglaNegocioException("Asigna el número de seguimiento antes de despachar el envío");
        }
        envio.setEstadoDespacho(nuevo);
        return aDTO(envioRepository.save(envio));
    }

    /** HU-43: asigna transportista y código de seguimiento. */
    public EnvioResponseDTO asignarTracking(Long id, TrackingRequestDTO dto) {
        String[] transportista = Transportistas.buscar(dto.getTransportista())
                .orElseThrow(() -> new ReglaNegocioException("Transportista desconocido: " + dto.getTransportista()));
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            throw new ReglaNegocioException("Ingresa el código de seguimiento");
        }
        Envio envio = buscar(id);
        envio.setTransportista(transportista[0]);
        envio.setNumeroSeguimiento(dto.getCodigo().trim());
        return aDTO(envioRepository.save(envio));
    }

    /** HU-43: el enlace de seguimiento solo se muestra cuando el envío está en tránsito o entregado. */
    @Transactional(readOnly = true)
    public TrackingDTO obtenerTracking(Long id) {
        Envio envio = buscar(id);
        boolean visible = envio.getEstadoDespacho() == EstadoDespacho.EN_TRANSITO
                || envio.getEstadoDespacho() == EstadoDespacho.ENTREGADO;
        if (!visible || envio.getNumeroSeguimiento() == null) {
            throw new ReglaNegocioException("El seguimiento estará disponible cuando el envío esté en tránsito");
        }
        String[] t = Transportistas.buscar(envio.getTransportista()).orElseThrow(
                () -> new ReglaNegocioException("El envío no tiene transportista asignado"));
        TrackingDTO dto = new TrackingDTO();
        dto.setTransportista(t[0]);
        dto.setCodigo(envio.getNumeroSeguimiento());
        dto.setUrl(t[1] + URLEncoder.encode(envio.getNumeroSeguimiento(), StandardCharsets.UTF_8));
        return dto;
    }

    public EnvioResponseDTO aDTO(Envio e) {
        EnvioResponseDTO dto = new EnvioResponseDTO();
        dto.setId(e.getId());
        dto.setPedidoNumero(e.getPedidoNumero());
        dto.setDestinatario(e.getDestinatario());
        dto.setDireccion(e.getDireccion());
        dto.setComuna(e.getComuna());
        dto.setCosto(e.getCosto());
        dto.setEstadoDespacho(e.getEstadoDespacho().name());
        dto.setNumeroSeguimiento(e.getNumeroSeguimiento());
        dto.setTransportista(e.getTransportista());
        dto.setNotas(e.getNotas());
        dto.setMensajeEntrega("Entrega estimada entre " + e.getDiasMin() + " y " + e.getDiasMax() + " días hábiles");
        dto.setFechaCreacion(e.getFechaCreacion());
        return dto;
    }

    private Envio buscar(Long id) {
        return envioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El envío " + id + " no existe"));
    }
}
