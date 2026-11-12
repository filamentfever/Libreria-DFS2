package com.libreriajuanin.pagos.service;

import com.libreriajuanin.pagos.dto.CuponValidacionDTO;
import com.libreriajuanin.pagos.dto.DatosTransferenciaDTO;
import com.libreriajuanin.pagos.dto.PagoTarjetaRequestDTO;
import com.libreriajuanin.pagos.dto.ResultadoPagoDTO;
import com.libreriajuanin.pagos.model.Cupon;
import com.libreriajuanin.pagos.model.Pago;
import com.libreriajuanin.pagos.repository.CuponRepository;
import com.libreriajuanin.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Pasarela de pago simulada (HU-52 a HU-58). */
@Service
@Transactional
public class PagoService {

    public static final String MENSAJE_SALDO_INSUFICIENTE =
            "Pago rechazado: saldo insuficiente en la tarjeta. Puedes reintentar con otra tarjeta.";
    public static final String MENSAJE_ERROR_TEMPORAL =
            "Pago rechazado por un error temporal del banco. Intenta nuevamente en unos minutos.";

    private static final Pattern VENCIMIENTO = Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2})$");
    private static final Random ALEATORIO = new Random();

    private final PagoRepository pagoRepository;
    private final CuponRepository cuponRepository;

    public PagoService(PagoRepository pagoRepository, CuponRepository cuponRepository) {
        this.pagoRepository = pagoRepository;
        this.cuponRepository = cuponRepository;
    }

    /** HU-53: valida formato de titular, número, vencimiento (MM/AA, no vencida) y CVV. */
    public Map<String, String> validarTarjeta(PagoTarjetaRequestDTO p) {
        Map<String, String> e = new LinkedHashMap<>();
        if (p.getTitular() == null || p.getTitular().trim().length() < 3) {
            e.put("titular", "Ingresa el nombre del titular como aparece en la tarjeta.");
        }
        String digitos = soloDigitos(p.getNumero());
        if (p.getNumero() == null || !p.getNumero().matches("[\\d ]+") || digitos.length() < 13 || digitos.length() > 19) {
            e.put("numero", "El número de tarjeta debe tener entre 13 y 19 dígitos.");
        }
        Matcher m = VENCIMIENTO.matcher(p.getVencimiento() == null ? "" : p.getVencimiento());
        if (!m.matches()) {
            e.put("vencimiento", "Usa el formato MM/AA.");
        } else {
            YearMonth vence = YearMonth.of(2000 + Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)));
            if (vence.isBefore(YearMonth.now())) {
                e.put("vencimiento", "La tarjeta está vencida.");
            }
        }
        if (p.getCvv() == null || !p.getCvv().matches("\\d{3,4}")) {
            e.put("cvv", "El CVV debe tener 3 o 4 dígitos.");
        }
        return e;
    }

    /**
     * HU-52, HU-54 y HU-58. Reglas del simulador:
     * tarjeta terminada en 0000 → saldo insuficiente; terminada en 9999 → error temporal;
     * cualquier otra con formato válido → aprobada (ej. 4111 1111 1111 1111).
     */
    public ResultadoPagoDTO pagarConTarjeta(PagoTarjetaRequestDTO dto) {
        Map<String, String> errores = validarTarjeta(dto);
        if (!errores.isEmpty()) {
            ResultadoPagoDTO r = rechazo("datos_invalidos", "Revisa los datos de la tarjeta.", true);
            r.setErrores(errores);
            return r;
        }
        if (dto.getMonto() == null || dto.getMonto().signum() <= 0) {
            return rechazo("monto_invalido", "El monto a pagar no es válido.", false);
        }

        String fin = soloDigitos(dto.getNumero());
        fin = fin.substring(fin.length() - 4);
        ResultadoPagoDTO resultado;
        if ("0000".equals(fin)) {
            resultado = rechazo("saldo_insuficiente", MENSAJE_SALDO_INSUFICIENTE, true);
        } else if ("9999".equals(fin)) {
            resultado = rechazo("error_temporal", MENSAJE_ERROR_TEMPORAL, true);
        } else {
            resultado = new ResultadoPagoDTO();
            resultado.setAprobado(true);
            resultado.setCodigoAutorizacion("AUT-" + (100000 + ALEATORIO.nextInt(900000)));
            resultado.setMensaje("Pago aprobado.");
        }
        registrar(dto, resultado);
        return resultado;
    }

    /** HU-57: el cupón debe existir, estar activo y no vencido. */
    @Transactional(readOnly = true)
    public CuponValidacionDTO validarCupon(String codigo) {
        CuponValidacionDTO r = new CuponValidacionDTO();
        String c = codigo == null ? "" : codigo.trim().toUpperCase();
        if (c.isEmpty()) {
            r.setMensaje("Ingresa un código de cupón.");
            return r;
        }
        Optional<Cupon> encontrado = cuponRepository.findByCodigoIgnoreCase(c);
        if (encontrado.isEmpty() || !encontrado.get().isActivo()) {
            r.setMensaje("El cupón no es válido.");
            return r;
        }
        Cupon cupon = encontrado.get();
        if (cupon.getVence() != null && cupon.getVence().isBefore(LocalDate.now())) {
            r.setMensaje("El cupón está vencido.");
            return r;
        }
        r.setValido(true);
        r.setCodigo(cupon.getCodigo());
        r.setPorcentaje(cupon.getPorcentaje());
        r.setMensaje("Cupón aplicado: " + cupon.getPorcentaje() + "% de descuento.");
        return r;
    }

    /** HU-56: datos de ejemplo; reemplazar por los de la cuenta real del comercio. */
    public DatosTransferenciaDTO datosTransferencia() {
        DatosTransferenciaDTO d = new DatosTransferenciaDTO();
        d.setTitular("Librería Juanin (demo)");
        d.setRut("11.111.111-1");
        d.setBanco("Banco de Ejemplo");
        d.setTipoCuenta("Cuenta corriente");
        d.setNumeroCuenta("000-00000-00");
        d.setEmail("pagos@libreriajuanin.example");
        d.setAviso("Tu pedido queda en pausa hasta que subas el comprobante de transferencia.");
        return d;
    }

    // ---------- auxiliares

    private ResultadoPagoDTO rechazo(String motivo, String mensaje, boolean puedeReintentar) {
        ResultadoPagoDTO r = new ResultadoPagoDTO();
        r.setAprobado(false);
        r.setMotivo(motivo);
        r.setMensaje(mensaje);
        r.setPuedeReintentar(puedeReintentar);
        return r;
    }

    private void registrar(PagoTarjetaRequestDTO dto, ResultadoPagoDTO resultado) {
        Pago pago = new Pago();
        pago.setReferencia(dto.getReferencia());
        pago.setMonto(dto.getMonto());
        pago.setMedio("TARJETA");
        pago.setEstado(resultado.isAprobado() ? "APROBADO" : "RECHAZADO");
        pago.setCodigoAutorizacion(resultado.getCodigoAutorizacion());
        pago.setMotivo(resultado.getMotivo());
        pago.setFecha(LocalDateTime.now());
        pagoRepository.save(pago);
    }

    private String soloDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    BigDecimal descuento(BigDecimal subtotal, int porcentaje) {
        return subtotal.multiply(BigDecimal.valueOf(porcentaje)).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
    }
}
