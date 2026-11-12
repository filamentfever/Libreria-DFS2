package com.libreriajuanin.envios.service;

import com.libreriajuanin.envios.dto.InstruccionesRetiroDTO;
import com.libreriajuanin.envios.dto.PuntoRetiroDTO;
import com.libreriajuanin.envios.dto.PuntoRetiroRequestDTO;
import com.libreriajuanin.envios.dto.SeleccionRetiroDTO;
import com.libreriajuanin.envios.exception.RecursoNoEncontradoException;
import com.libreriajuanin.envios.exception.ReglaNegocioException;
import com.libreriajuanin.envios.exception.ValidacionException;
import com.libreriajuanin.envios.model.PuntoRetiro;
import com.libreriajuanin.envios.repository.PuntoRetiroRepository;
import com.libreriajuanin.envios.util.TarifasDespacho;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retiro en tienda (HU-46 a HU-50; retiroService.ts). */
@Service
@Transactional
public class RetiroService {

    private final PuntoRetiroRepository puntoRetiroRepository;

    public RetiroService(PuntoRetiroRepository puntoRetiroRepository) {
        this.puntoRetiroRepository = puntoRetiroRepository;
    }

    /** HU-47 y HU-49: solo puntos activos, opcionalmente de una ciudad (ignora tildes y mayúsculas). */
    @Transactional(readOnly = true)
    public List<PuntoRetiroDTO> listarPuntos(String ciudad) {
        String q = TarifasDespacho.normalizar(ciudad);
        return puntoRetiroRepository.findByActivoTrue().stream()
                .filter(p -> q.isEmpty() || TarifasDespacho.normalizar(p.getCiudad()).equals(q))
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarCiudades() {
        return puntoRetiroRepository.findByActivoTrue().stream()
                .map(PuntoRetiro::getCiudad)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    @Transactional(readOnly = true)
    public PuntoRetiroDTO obtenerPunto(Long id) {
        return aDTO(buscar(id));
    }

    /** HU-46 y HU-49: al elegir sucursal el envío cuesta $0 y se autocompleta la dirección temporal. */
    @Transactional(readOnly = true)
    public SeleccionRetiroDTO seleccionar(Long id) {
        PuntoRetiro punto = buscar(id);
        if (!punto.isActivo()) {
            throw new ReglaNegocioException("Selecciona una sucursal disponible");
        }
        SeleccionRetiroDTO dto = new SeleccionRetiroDTO();
        dto.setPuntoId(punto.getId());
        dto.setSucursal(punto.getSucursal());
        dto.setCostoEnvio(BigDecimal.ZERO);
        dto.setDireccionTemporal(punto.getDireccion() + ", " + punto.getCiudad());
        dto.setHorario(punto.getHorario());
        return dto;
    }

    /** HU-48: código de retiro asociado al pedido (determinista: mismo pedido, mismo código). */
    public String generarCodigo(String pedidoNumero) {
        if (pedidoNumero == null || pedidoNumero.isBlank()) {
            throw new ReglaNegocioException("Indica el número de pedido");
        }
        long h = 7;
        for (char ch : pedidoNumero.trim().toCharArray()) {
            h = (h * 31 + ch) & 0xFFFFFFFFL;
        }
        String base36 = Long.toString(h, 36).toUpperCase();
        String relleno = ("000000" + base36);
        return "RET-" + relleno.substring(relleno.length() - 6);
    }

    /** HU-48: dirección, horario y código que se incluyen en el comprobante. */
    @Transactional(readOnly = true)
    public InstruccionesRetiroDTO instrucciones(String pedidoNumero, Long puntoId) {
        PuntoRetiro punto = buscar(puntoId);
        String codigo = generarCodigo(pedidoNumero);
        String direccion = punto.getDireccion() + ", " + punto.getCiudad();
        InstruccionesRetiroDTO dto = new InstruccionesRetiroDTO();
        dto.setPedidoNumero(pedidoNumero.trim());
        dto.setCodigo(codigo);
        dto.setDireccion(direccion);
        dto.setHorario(punto.getHorario());
        dto.setTexto("Retira tu pedido en " + punto.getSucursal() + " (" + direccion + "). Horario: "
                + punto.getHorario() + ". Presenta el código " + codigo + " y tu documento de identidad.");
        return dto;
    }

    /** Alta de un punto de retiro (panel admin). */
    public PuntoRetiroDTO crearPunto(PuntoRetiroRequestDTO dto) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (dto.getSucursal() == null || dto.getSucursal().isBlank()) errores.put("sucursal", "Ingresa el nombre de la sucursal.");
        if (dto.getDireccion() == null || dto.getDireccion().trim().length() < 5) errores.put("direccion", "Ingresa una dirección válida.");
        if (dto.getCiudad() == null || dto.getCiudad().isBlank()) errores.put("ciudad", "Ingresa la ciudad.");
        if (dto.getHorario() == null || dto.getHorario().isBlank()) errores.put("horario", "Ingresa el horario de atención.");
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        PuntoRetiro punto = new PuntoRetiro(dto.getSucursal().trim(), dto.getDireccion().trim(), dto.getCiudad().trim(), dto.getHorario().trim(), true);
        return aDTO(puntoRetiroRepository.save(punto));
    }

    /** Activa o desactiva un punto (los inactivos no se listan). */
    public PuntoRetiroDTO cambiarActivo(Long id, boolean activo) {
        PuntoRetiro punto = buscar(id);
        punto.setActivo(activo);
        return aDTO(puntoRetiroRepository.save(punto));
    }

    public PuntoRetiroDTO aDTO(PuntoRetiro p) {
        PuntoRetiroDTO dto = new PuntoRetiroDTO();
        dto.setId(p.getId());
        dto.setSucursal(p.getSucursal());
        dto.setDireccion(p.getDireccion());
        dto.setCiudad(p.getCiudad());
        dto.setHorario(p.getHorario());
        dto.setActivo(p.isActivo());
        return dto;
    }

    private PuntoRetiro buscar(Long id) {
        return puntoRetiroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El punto de retiro " + id + " no existe"));
    }
}
