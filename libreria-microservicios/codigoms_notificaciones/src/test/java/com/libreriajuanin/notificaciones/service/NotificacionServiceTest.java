package com.libreriajuanin.notificaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.notificaciones.TestDataFactory;
import com.libreriajuanin.notificaciones.dto.AlertaRequestDTO;
import com.libreriajuanin.notificaciones.dto.ConfirmacionPedidoRequestDTO;
import com.libreriajuanin.notificaciones.dto.NotificacionRequestDTO;
import com.libreriajuanin.notificaciones.dto.NotificacionResponseDTO;
import com.libreriajuanin.notificaciones.dto.RegistroUsuarioRequestDTO;
import com.libreriajuanin.notificaciones.dto.ResultadoColaDTO;
import com.libreriajuanin.notificaciones.exception.ReglaNegocioException;
import com.libreriajuanin.notificaciones.exception.ValidacionException;
import com.libreriajuanin.notificaciones.model.EstadoEnvio;
import com.libreriajuanin.notificaciones.model.Notificacion;
import com.libreriajuanin.notificaciones.repository.NotificacionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;
    @Mock
    private EmisorCorreo emisorCorreo;

    @InjectMocks
    private NotificacionService notificacionService;

    private void guardadoDevuelveLoMismo() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ------------------------------------------------------------ encolar

    @Test
    void encolarRegistroUsuario_creaNotificacionPendienteConElAsuntoDeBienvenida() {
        guardadoDevuelveLoMismo();
        RegistroUsuarioRequestDTO dto = new RegistroUsuarioRequestDTO();
        dto.setNombre("Ana Pérez");
        dto.setEmail("ana@mail.cl");

        NotificacionResponseDTO r = notificacionService.encolarRegistroUsuario(dto);

        assertThat(r.getEstadoEnvio()).isEqualTo("PENDIENTE");
        assertThat(r.getTipoEvento()).isEqualTo("REGISTRO_USUARIO");
        assertThat(r.getAsunto()).isEqualTo("Bienvenido a nuestra papelería");
        assertThat(r.getMensaje()).contains("Ana Pérez");
        verify(emisorCorreo, never()).enviar(anyString(), anyString(), anyString());
    }

    @Test
    void encolarConfirmacionPedido_incluyeNumeroYTotal() {
        guardadoDevuelveLoMismo();
        ConfirmacionPedidoRequestDTO dto = new ConfirmacionPedidoRequestDTO();
        dto.setEmail("ana@mail.cl");
        dto.setNumeroPedido("ORD-1001");
        dto.setTotal(new BigDecimal("45880"));
        dto.setDetalle("2 x Cuaderno Miku");

        NotificacionResponseDTO r = notificacionService.encolarConfirmacionPedido(dto);

        assertThat(r.getAsunto()).isEqualTo("Confirmación de tu pedido ORD-1001");
        assertThat(r.getMensaje()).contains("Total: $45880");
        assertThat(r.getMensaje()).contains("2 x Cuaderno Miku");
    }

    @Test
    void encolarConfirmacionPedido_sinNumero_lanzaValidacion() {
        ConfirmacionPedidoRequestDTO dto = new ConfirmacionPedidoRequestDTO();
        dto.setEmail("ana@mail.cl");

        assertThatThrownBy(() -> notificacionService.encolarConfirmacionPedido(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("numeroPedido");
    }

    @Test
    void encolarAlerta_correoInvalido_lanzaValidacionYNoGuarda() {
        AlertaRequestDTO dto = new AlertaRequestDTO();
        dto.setDestinatario("no-es-correo");
        dto.setMensaje("Stock crítico");

        assertThatThrownBy(() -> notificacionService.encolarAlerta(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("destinatario");
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void encolar_mensajeVacio_lanzaValidacion() {
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setTipoEvento("ALERTA_STOCK");
        dto.setDestinatario("ana@mail.cl");
        dto.setMensaje(" ");

        assertThatThrownBy(() -> notificacionService.encolar(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("mensaje");
    }

    @Test
    void encolar_tipoDeEventoDesconocido_lanzaExcepcion() {
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setTipoEvento("LLUVIA_DE_MENSAJES");
        dto.setDestinatario("ana@mail.cl");
        dto.setMensaje("hola");

        assertThatThrownBy(() -> notificacionService.encolar(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Tipo de evento inválido");
    }

    // ------------------------------------------------------------ enviar

    @Test
    void enviar_pendiente_loMarcaEnviadoYRegistraLaFecha() {
        Notificacion n = TestDataFactory.unaNotificacion(1L, EstadoEnvio.PENDIENTE, 0);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(n));
        guardadoDevuelveLoMismo();

        NotificacionResponseDTO r = notificacionService.enviar(1L);

        assertThat(r.getEstadoEnvio()).isEqualTo("ENVIADO");
        assertThat(r.getIntentos()).isEqualTo(1);
        assertThat(r.getFechaEnvio()).isNotNull();
        verify(emisorCorreo).enviar("ana@mail.cl", "Alerta", "Stock crítico");
    }

    @Test
    void enviar_siElEmisorFalla_quedaFallidaYCuentaElIntento() {
        Notificacion n = TestDataFactory.unaNotificacion(1L, EstadoEnvio.PENDIENTE, 0);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(n));
        guardadoDevuelveLoMismo();
        org.mockito.Mockito.doThrow(new RuntimeException("SMTP caído")).when(emisorCorreo).enviar(anyString(), anyString(), anyString());

        NotificacionResponseDTO r = notificacionService.enviar(1L);

        assertThat(r.getEstadoEnvio()).isEqualTo("FALLIDO");
        assertThat(r.getIntentos()).isEqualTo(1);
    }

    @Test
    void enviar_yaEnviada_noSeVuelveAEnviar() {
        Notificacion n = TestDataFactory.unaNotificacion(1L, EstadoEnvio.ENVIADO, 1);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(n));

        NotificacionResponseDTO r = notificacionService.enviar(1L);

        assertThat(r.getEstadoEnvio()).isEqualTo("ENVIADO");
        verify(emisorCorreo, never()).enviar(anyString(), anyString(), anyString());
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void enviar_fallidaConTresIntentos_lanzaExcepcion() {
        Notificacion n = TestDataFactory.unaNotificacion(1L, EstadoEnvio.FALLIDO, 3);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> notificacionService.enviar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("máximo");
    }

    @Test
    void procesarCola_enviaLasPendientesEInformaElResultado() {
        Notificacion a = TestDataFactory.unaNotificacion(1L, EstadoEnvio.PENDIENTE, 0);
        Notificacion b = TestDataFactory.unaNotificacion(2L, EstadoEnvio.PENDIENTE, 0);
        when(notificacionRepository.findByEstadoEnvio(EstadoEnvio.PENDIENTE)).thenReturn(List.of(a, b));
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(a));
        when(notificacionRepository.findById(2L)).thenReturn(Optional.of(b));
        guardadoDevuelveLoMismo();

        ResultadoColaDTO r = notificacionService.procesarCola();

        assertThat(r.getProcesadas()).isEqualTo(2);
        assertThat(r.getEnviadas()).isEqualTo(2);
        assertThat(r.getFallidas()).isZero();
    }

    // ------------------------------------------------------------ listar

    @Test
    void listar_filtraPorEstadoYDestinatario() {
        Notificacion a = TestDataFactory.unaNotificacion(1L, EstadoEnvio.PENDIENTE, 0);
        Notificacion b = TestDataFactory.unaNotificacion(2L, EstadoEnvio.ENVIADO, 1);
        when(notificacionRepository.findAll()).thenReturn(List.of(a, b));

        assertThat(notificacionService.listar("enviado", null)).hasSize(1);
        assertThat(notificacionService.listar(null, "ANA@mail.cl")).hasSize(2);
        assertThat(notificacionService.listar(null, "otra@mail.cl")).isEmpty();
    }

    @Test
    void listar_estadoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> notificacionService.listar("QUIEN_SABE", null))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
