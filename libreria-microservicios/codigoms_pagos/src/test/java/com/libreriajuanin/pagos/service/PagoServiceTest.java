package com.libreriajuanin.pagos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.pagos.TestDataFactory;
import com.libreriajuanin.pagos.dto.CuponValidacionDTO;
import com.libreriajuanin.pagos.dto.PagoTarjetaRequestDTO;
import com.libreriajuanin.pagos.dto.ResultadoPagoDTO;
import com.libreriajuanin.pagos.model.Cupon;
import com.libreriajuanin.pagos.model.Pago;
import com.libreriajuanin.pagos.repository.CuponRepository;
import com.libreriajuanin.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private CuponRepository cuponRepository;

    @InjectMocks
    private PagoService pagoService;

    // ------------------------------------------------------- HU-53 validación

    @Test
    void validarTarjeta_datosCorrectos_noHayErrores() {
        assertThat(pagoService.validarTarjeta(TestDataFactory.unaTarjeta())).isEmpty();
    }

    @Test
    void validarTarjeta_datosInvalidos_hayUnErrorPorCampo() {
        PagoTarjetaRequestDTO t = new PagoTarjetaRequestDTO();
        t.setTitular("");
        t.setNumero("12");
        t.setVencimiento("13/99");
        t.setCvv("1");

        Map<String, String> errores = pagoService.validarTarjeta(t);

        assertThat(errores).containsKeys("titular", "numero", "vencimiento", "cvv");
    }

    @Test
    void validarTarjeta_vencida_marcaElVencimiento() {
        PagoTarjetaRequestDTO t = TestDataFactory.unaTarjeta();
        t.setVencimiento("01/20");

        assertThat(pagoService.validarTarjeta(t)).containsEntry("vencimiento", "La tarjeta está vencida.");
    }

    // ------------------------------------------------------- HU-52 / 54 / 58

    @Test
    void pagarConTarjeta_tarjetaValida_apruebaYRegistraElPago() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        ResultadoPagoDTO r = pagoService.pagarConTarjeta(TestDataFactory.unaTarjeta());

        assertThat(r.isAprobado()).isTrue();
        assertThat(r.getCodigoAutorizacion()).startsWith("AUT-");
        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("APROBADO");
        assertThat(captor.getValue().getReferencia()).isEqualTo("ORD-TEST");
    }

    @Test
    void pagarConTarjeta_terminadaEn0000_rechazaPorSaldoInsuficiente() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        PagoTarjetaRequestDTO t = TestDataFactory.unaTarjeta();
        t.setNumero("4000 0000 0000 0000");

        ResultadoPagoDTO r = pagoService.pagarConTarjeta(t);

        assertThat(r.isAprobado()).isFalse();
        assertThat(r.getMotivo()).isEqualTo("saldo_insuficiente");
        assertThat(r.isPuedeReintentar()).isTrue();
        assertThat(r.getMensaje()).contains("saldo insuficiente");
    }

    @Test
    void pagarConTarjeta_terminadaEn9999_rechazaPorErrorTemporal() {
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        PagoTarjetaRequestDTO t = TestDataFactory.unaTarjeta();
        t.setNumero("4000 0000 0000 9999");

        ResultadoPagoDTO r = pagoService.pagarConTarjeta(t);

        assertThat(r.getMotivo()).isEqualTo("error_temporal");
        assertThat(r.isPuedeReintentar()).isTrue();
    }

    @Test
    void pagarConTarjeta_datosInvalidos_noRegistraNada() {
        PagoTarjetaRequestDTO t = TestDataFactory.unaTarjeta();
        t.setCvv("1");

        ResultadoPagoDTO r = pagoService.pagarConTarjeta(t);

        assertThat(r.isAprobado()).isFalse();
        assertThat(r.getMotivo()).isEqualTo("datos_invalidos");
        assertThat(r.getErrores()).containsKey("cvv");
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void pagarConTarjeta_montoCero_rechazaSinReintento() {
        PagoTarjetaRequestDTO t = TestDataFactory.unaTarjeta();
        t.setMonto(BigDecimal.ZERO);

        ResultadoPagoDTO r = pagoService.pagarConTarjeta(t);

        assertThat(r.getMotivo()).isEqualTo("monto_invalido");
        assertThat(r.isPuedeReintentar()).isFalse();
        verify(pagoRepository, never()).save(any());
    }

    // ------------------------------------------------------- HU-57 cupones

    @Test
    void validarCupon_codigoValido_devuelveElPorcentaje() {
        when(cuponRepository.findByCodigoIgnoreCase("VUELTAACLASES"))
                .thenReturn(Optional.of(new Cupon("VUELTAACLASES", 15, true, null)));

        CuponValidacionDTO r = pagoService.validarCupon(" vueltaaclases ");

        assertThat(r.isValido()).isTrue();
        assertThat(r.getPorcentaje()).isEqualTo(15);
    }

    @Test
    void validarCupon_inexistenteOInactivo_noEsValido() {
        when(cuponRepository.findByCodigoIgnoreCase("NOEXISTE")).thenReturn(Optional.empty());
        when(cuponRepository.findByCodigoIgnoreCase("APAGADO"))
                .thenReturn(Optional.of(new Cupon("APAGADO", 10, false, null)));

        assertThat(pagoService.validarCupon("NOEXISTE").isValido()).isFalse();
        assertThat(pagoService.validarCupon("APAGADO").isValido()).isFalse();
    }

    @Test
    void validarCupon_vencido_noEsValido() {
        when(cuponRepository.findByCodigoIgnoreCase("VIEJO"))
                .thenReturn(Optional.of(new Cupon("VIEJO", 10, true, LocalDate.now().minusDays(1))));

        CuponValidacionDTO r = pagoService.validarCupon("VIEJO");

        assertThat(r.isValido()).isFalse();
        assertThat(r.getMensaje()).contains("vencido");
    }

    @Test
    void validarCupon_vacio_pideElCodigo() {
        assertThat(pagoService.validarCupon("  ").isValido()).isFalse();
        verify(cuponRepository, never()).findByCodigoIgnoreCase(any());
    }

    @Test
    void descuento_restaElPorcentajeDelSubtotal() {
        assertThat(pagoService.descuento(new BigDecimal("20000"), 15)).isEqualByComparingTo("3000");
    }

    // ------------------------------------------------------- HU-56 transferencia

    @Test
    void datosTransferencia_incluyeRutBancoCuentaYAviso() {
        var d = pagoService.datosTransferencia();

        assertThat(d.getRut()).isNotBlank();
        assertThat(d.getBanco()).isNotBlank();
        assertThat(d.getNumeroCuenta()).isNotBlank();
        assertThat(d.getAviso()).contains("en pausa");
    }
}
