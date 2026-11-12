package com.libreriajuanin.envios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.envios.TestDataFactory;
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
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @InjectMocks
    private EnvioService envioService;

    // ------------------------------------------------------------ cotización

    @Test
    void cotizar_comunaConCobertura_devuelveCostoYPlazo() {
        CotizacionDTO c = envioService.cotizar("santiago");

        assertThat(c.getCosto()).isEqualByComparingTo("5900");
        assertThat(c.getMensajeEntrega()).isEqualTo("Entrega estimada entre 2 y 4 días hábiles");
    }

    @Test
    void cotizar_ignoraTildesYMayusculas() {
        assertThat(envioService.cotizar("CONCEPCION").getComuna()).isEqualTo("Concepción");
    }

    @Test
    void cotizar_comunaSinCobertura_lanzaExcepcion() {
        assertThatThrownBy(() -> envioService.cotizar("Atlantis"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no tenemos despacho");
    }

    @Test
    void cotizar_comunaVacia_pideLaComuna() {
        assertThatThrownBy(() -> envioService.cotizar(" "))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ingresa tu comuna");
    }

    // ------------------------------------------------------------ crear

    @Test
    void crear_datosValidos_guardaEnvioPendienteConElCostoDeLaComuna() {
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioResponseDTO r = envioService.crear(TestDataFactory.unaSolicitud());

        assertThat(r.getEstadoDespacho()).isEqualTo("PENDIENTE");
        assertThat(r.getCosto()).isEqualByComparingTo("5900");
        assertThat(r.getComuna()).isEqualTo("Santiago");
        assertThat(r.getNotas()).isEqualTo("dejar en conserjería");
    }

    @Test
    void crear_datosIncompletos_lanzaValidacionConUnErrorPorCampo() {
        EnvioRequestDTO d = new EnvioRequestDTO();
        d.setDestinatario("");
        d.setDireccion("");
        d.setComuna("");

        assertThatThrownBy(() -> envioService.crear(d))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("destinatario")
                .hasMessageContaining("direccion")
                .hasMessageContaining("comuna");
        verify(envioRepository, never()).save(any());
    }

    @Test
    void crear_notasDeMasDe200Caracteres_lanzaValidacion() {
        EnvioRequestDTO d = TestDataFactory.unaSolicitud();
        d.setNotas("x".repeat(201));

        assertThatThrownBy(() -> envioService.crear(d))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("notas");
    }

    // ------------------------------------------------------------ estados y tracking

    @Test
    void cambiarEstado_aEnTransitoSinSeguimiento_lanzaExcepcion() {
        Envio envio = TestDataFactory.unEnvio(EstadoDespacho.EN_PREPARACION);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        assertThatThrownBy(() -> envioService.cambiarEstado(1L, "EN_TRANSITO"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("número de seguimiento");
        verify(envioRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_aEnTransitoConSeguimiento_loGuarda() {
        Envio envio = TestDataFactory.unEnvio(EstadoDespacho.EN_PREPARACION);
        envio.setNumeroSeguimiento("ABC123");
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioResponseDTO r = envioService.cambiarEstado(1L, "en transito");

        assertThat(r.getEstadoDespacho()).isEqualTo("EN_TRANSITO");
    }

    @Test
    void cambiarEstado_envioEntregado_yaNoCambia() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.unEnvio(EstadoDespacho.ENTREGADO)));

        assertThatThrownBy(() -> envioService.cambiarEstado(1L, "PENDIENTE"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no puede cambiar de estado");
    }

    @Test
    void cambiarEstado_estadoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> envioService.cambiarEstado(1L, "PERDIDO"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Estado inválido");
        verify(envioRepository, never()).findById(any());
    }

    @Test
    void asignarTracking_transportistaConocido_guardaCodigo() {
        Envio envio = TestDataFactory.unEnvio(EstadoDespacho.EN_PREPARACION);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));
        TrackingRequestDTO dto = new TrackingRequestDTO();
        dto.setTransportista("starken");
        dto.setCodigo(" ABC123 ");

        EnvioResponseDTO r = envioService.asignarTracking(1L, dto);

        assertThat(r.getTransportista()).isEqualTo("Starken");
        assertThat(r.getNumeroSeguimiento()).isEqualTo("ABC123");
    }

    @Test
    void asignarTracking_transportistaDesconocido_lanzaExcepcion() {
        TrackingRequestDTO dto = new TrackingRequestDTO();
        dto.setTransportista("PalomaMensajera");
        dto.setCodigo("X1");

        assertThatThrownBy(() -> envioService.asignarTracking(1L, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Transportista desconocido");
    }

    @Test
    void obtenerTracking_soloVisibleCuandoEstaEnTransito() {
        Envio preparando = TestDataFactory.unEnvio(EstadoDespacho.EN_PREPARACION);
        preparando.setTransportista("Chilexpress");
        preparando.setNumeroSeguimiento("CX-99");
        when(envioRepository.findById(1L)).thenReturn(Optional.of(preparando));

        assertThatThrownBy(() -> envioService.obtenerTracking(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("disponible cuando el envío esté en tránsito");

        preparando.setEstadoDespacho(EstadoDespacho.EN_TRANSITO);
        TrackingDTO t = envioService.obtenerTracking(1L);

        assertThat(t.getCodigo()).isEqualTo("CX-99");
        assertThat(t.getUrl()).contains("CX-99");
    }

    @Test
    void obtenerPorPedido_inexistente_lanzaExcepcion() {
        when(envioRepository.findByPedidoNumeroIgnoreCase("ORD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> envioService.obtenerPorPedido("ORD-9999"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
