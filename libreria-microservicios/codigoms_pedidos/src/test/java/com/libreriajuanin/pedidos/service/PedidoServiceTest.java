package com.libreriajuanin.pedidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.libreriajuanin.pedidos.TestDataFactory;
import com.libreriajuanin.pedidos.client.CarritoClient;
import com.libreriajuanin.pedidos.client.CatalogoClient;
import com.libreriajuanin.pedidos.client.PagosClient;
import com.libreriajuanin.pedidos.dto.CheckoutRequestDTO;
import com.libreriajuanin.pedidos.dto.CuponValidacionDTO;
import com.libreriajuanin.pedidos.dto.PagoTarjetaRequestDTO;
import com.libreriajuanin.pedidos.dto.PedidoResponseDTO;
import com.libreriajuanin.pedidos.dto.StockRequestDTO;
import com.libreriajuanin.pedidos.exception.PagoRechazadoException;
import com.libreriajuanin.pedidos.exception.RecursoNoEncontradoException;
import com.libreriajuanin.pedidos.exception.ReglaNegocioException;
import com.libreriajuanin.pedidos.model.EstadoPedido;
import com.libreriajuanin.pedidos.model.Pedido;
import com.libreriajuanin.pedidos.repository.PedidoRepository;
import feign.FeignException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** PedidoService colabora con 1 repositorio y 3 clientes Feign: todos mockeados. */
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private CarritoClient carritoClient;
    @Mock
    private CatalogoClient catalogoClient;
    @Mock
    private PagosClient pagosClient;

    @InjectMocks
    private PedidoService pedidoService;


    /** save() asigna el id 1 la primera vez, como haría la base de datos. */
    private void guardadoAsignaId() {
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            if (p.getId() == null) p.setId(1L);
            return p;
        });
    }

    // ============================================================ crearPedido

    @Test
    void crearPedido_tarjetaAprobada_guardaPedidoDescuentaStockYVaciaCarrito() {
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));
        when(pagosClient.pagarConTarjeta(any(PagoTarjetaRequestDTO.class))).thenReturn(TestDataFactory.pagoAprobado());
        guardadoAsignaId();

        PedidoResponseDTO r = pedidoService.crearPedido(TestDataFactory.unCheckoutDomicilio());

        assertThat(r.getNumero()).isEqualTo("ORD-1001");
        assertThat(r.getEstado()).isEqualTo("NUEVO");
        assertThat(r.isPagoConfirmado()).isTrue();
        assertThat(r.getCodigoAutorizacion()).isEqualTo("AUT-123456");
        assertThat(r.getSubtotal()).isEqualByComparingTo("39980");
        assertThat(r.getEnvio()).isEqualByComparingTo("5900");
        assertThat(r.getTotal()).isEqualByComparingTo("45880");
        // El IVA va incluido: neto + IVA = total
        assertThat(r.getNeto().add(r.getIva())).isEqualByComparingTo(r.getTotal());
        assertThat(r.getItems()).hasSize(1);
        verify(catalogoClient).descontarStock(eq(10L), any(StockRequestDTO.class));
        verify(carritoClient).vaciarCarrito(1L);
    }

    @Test
    void crearPedido_seCobraElTotalConEnvioIncluido() {
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));
        when(pagosClient.pagarConTarjeta(any(PagoTarjetaRequestDTO.class))).thenReturn(TestDataFactory.pagoAprobado());
        guardadoAsignaId();

        pedidoService.crearPedido(TestDataFactory.unCheckoutDomicilio());

        ArgumentCaptor<PagoTarjetaRequestDTO> captor = ArgumentCaptor.forClass(PagoTarjetaRequestDTO.class);
        verify(pagosClient).pagarConTarjeta(captor.capture());
        assertThat(captor.getValue().getMonto()).isEqualByComparingTo("45880");
        assertThat(captor.getValue().getReferencia()).isEqualTo("CARRITO-1");
    }

    @Test
    void crearPedido_conCuponValido_aplicaElDescuentoYRetiroNoCobraEnvio() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutRetiro();
        checkout.setCuponCodigo("VUELTAACLASES");
        CuponValidacionDTO cupon = new CuponValidacionDTO();
        cupon.setValido(true);
        cupon.setCodigo("VUELTAACLASES");
        cupon.setPorcentaje(15);
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));
        when(pagosClient.validarCupon("VUELTAACLASES")).thenReturn(cupon);
        when(pagosClient.pagarConTarjeta(any(PagoTarjetaRequestDTO.class))).thenReturn(TestDataFactory.pagoAprobado());
        guardadoAsignaId();

        PedidoResponseDTO r = pedidoService.crearPedido(checkout);

        assertThat(r.getDescuento()).isEqualByComparingTo("5997"); // 15% de 39.980
        assertThat(r.getEnvio()).isEqualByComparingTo("0");
        assertThat(r.getTotal()).isEqualByComparingTo("33983");
        assertThat(r.getCupon()).isEqualTo("VUELTAACLASES");
    }

    @Test
    void crearPedido_pagoRechazado_lanzaExcepcionYNoCreaNiDescuentaNiVacia() {
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));
        when(pagosClient.pagarConTarjeta(any(PagoTarjetaRequestDTO.class))).thenReturn(TestDataFactory.pagoRechazado());

        assertThatThrownBy(() -> pedidoService.crearPedido(TestDataFactory.unCheckoutDomicilio()))
                .isInstanceOf(PagoRechazadoException.class)
                .hasMessageContaining("saldo insuficiente");

        verify(pedidoRepository, never()).save(any());
        verify(catalogoClient, never()).descontarStock(any(), any());
        verify(carritoClient, never()).vaciarCarrito(any());
    }

    @Test
    void crearPedido_transferencia_quedaPendienteYNoCobra() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutRetiro();
        checkout.setMedioPago("TRANSFERENCIA");
        checkout.setTarjeta(null);
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));
        guardadoAsignaId();

        PedidoResponseDTO r = pedidoService.crearPedido(checkout);

        assertThat(r.getEstado()).isEqualTo("PENDIENTE");
        assertThat(r.isPagoConfirmado()).isFalse();
        verifyNoInteractions(pagosClient);
    }

    @Test
    void crearPedido_carritoVacio_lanzaExcepcion() {
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoVacioDTO());

        assertThatThrownBy(() -> pedidoService.crearPedido(TestDataFactory.unCheckoutDomicilio()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vacío");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_carritoNoExiste_lanzaExcepcion() {
        when(carritoClient.obtenerCarrito(1L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> pedidoService.crearPedido(TestDataFactory.unCheckoutDomicilio()))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    void crearPedido_stockInsuficiente_lanzaExcepcionAntesDeCobrar() {
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO()); // pide 2
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(1)); // queda 1

        assertThatThrownBy(() -> pedidoService.crearPedido(TestDataFactory.unCheckoutDomicilio()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");
        verifyNoInteractions(pagosClient);
    }

    @Test
    void crearPedido_comunaSinCobertura_lanzaExcepcion() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutDomicilio();
        checkout.setComuna("Atlantis");
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));

        assertThatThrownBy(() -> pedidoService.crearPedido(checkout))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no tenemos despacho");
        verifyNoInteractions(pagosClient);
    }

    @Test
    void crearPedido_domicilioSinDireccion_lanzaExcepcion() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutDomicilio();
        checkout.setDireccion(" ");
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));

        assertThatThrownBy(() -> pedidoService.crearPedido(checkout))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("dirección");
    }

    @Test
    void crearPedido_retiroSinSucursal_lanzaExcepcion() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutRetiro();
        checkout.setSucursal(null);
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));

        assertThatThrownBy(() -> pedidoService.crearPedido(checkout))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sucursal");
    }

    @Test
    void crearPedido_cuponInvalido_lanzaExcepcionAntesDeCobrar() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutRetiro();
        checkout.setCuponCodigo("NOEXISTE");
        CuponValidacionDTO invalido = new CuponValidacionDTO();
        invalido.setValido(false);
        invalido.setMensaje("El cupón no es válido.");
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProductoDTO(10));
        when(pagosClient.validarCupon("NOEXISTE")).thenReturn(invalido);

        assertThatThrownBy(() -> pedidoService.crearPedido(checkout))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("El cupón no es válido");
        verify(pagosClient, never()).pagarConTarjeta(any());
    }

    @Test
    void crearPedido_medioDePagoInvalido_lanzaExcepcion() {
        CheckoutRequestDTO checkout = TestDataFactory.unCheckoutDomicilio();
        checkout.setMedioPago("CHEQUE");
        when(carritoClient.obtenerCarrito(1L)).thenReturn(TestDataFactory.unCarritoDTO());

        assertThatThrownBy(() -> pedidoService.crearPedido(checkout))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("medio de pago");
    }

    // ============================================================ estados (admin)

    @Test
    void cambiarEstado_estadoValido_loGuarda() {
        Pedido pedido = TestDataFactory.unPedido(1L, EstadoPedido.NUEVO, LocalDate.now());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO r = pedidoService.cambiarEstado(1L, "armando caja");

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.ARMANDO_CAJA);
        assertThat(r.getEstadoCliente()).isEqualTo("Pendiente");
    }

    @Test
    void cambiarEstado_estadoInvalido_lanzaExcepcionYNoGuarda() {
        assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, "PERDIDO"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Estado inválido");
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_pedidoNoExiste_lanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.cambiarEstado(99L, "ENVIADO"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void estadoParaCliente_traduceAPendienteListoOEnviado() {
        assertThat(pedidoService.estadoParaCliente(EstadoPedido.NUEVO)).isEqualTo("Pendiente");
        assertThat(pedidoService.estadoParaCliente(EstadoPedido.ARMANDO_CAJA)).isEqualTo("Pendiente");
        assertThat(pedidoService.estadoParaCliente(EstadoPedido.LISTO_PARA_RETIRO)).isEqualTo("Listo");
        assertThat(pedidoService.estadoParaCliente(EstadoPedido.ENVIADO)).isEqualTo("Enviado");
        assertThat(pedidoService.estadoParaCliente(EstadoPedido.ENTREGADO)).isEqualTo("Enviado");
        assertThat(pedidoService.estadoParaCliente(EstadoPedido.CANCELADO)).isEqualTo("Cancelado");
    }

    @Test
    void adjuntarComprobanteTransferencia_sacaElPedidoDePausa() {
        Pedido pedido = TestDataFactory.unPedido(1L, EstadoPedido.PENDIENTE, LocalDate.now());
        pedido.setMedioPago("TRANSFERENCIA");
        pedido.setPagoConfirmado(false);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.adjuntarComprobanteTransferencia(1L, "comprobante.pdf");

        assertThat(pedido.isPagoConfirmado()).isTrue();
        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.NUEVO);
        assertThat(pedido.getComprobanteTransferencia()).isEqualTo("comprobante.pdf");
    }

    @Test
    void adjuntarComprobanteTransferencia_pedidoPagadoConTarjeta_lanzaExcepcion() {
        Pedido pedido = TestDataFactory.unPedido(1L, EstadoPedido.NUEVO, LocalDate.now());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.adjuntarComprobanteTransferencia(1L, "x.pdf"))
                .isInstanceOf(ReglaNegocioException.class);
        verify(pedidoRepository, never()).save(any());
    }

    // ============================================================ consultas

    @Test
    void listar_filtraPorEstado() {
        Pedido nuevo = TestDataFactory.unPedido(1L, EstadoPedido.NUEVO, LocalDate.now());
        Pedido enviado = TestDataFactory.unPedido(2L, EstadoPedido.ENVIADO, LocalDate.now());
        when(pedidoRepository.findAll()).thenReturn(List.of(nuevo, enviado));

        List<PedidoResponseDTO> r = pedidoService.listar(EstadoPedido.ENVIADO, null, null);

        assertThat(r).hasSize(1);
        assertThat(r.get(0).getEstado()).isEqualTo("ENVIADO");
    }

    @Test
    void listar_filtraPorRangoDeFechasConExtremosIncluidos() {
        Pedido a = TestDataFactory.unPedido(1L, EstadoPedido.NUEVO, LocalDate.of(2026, 3, 1));
        Pedido b = TestDataFactory.unPedido(2L, EstadoPedido.NUEVO, LocalDate.of(2026, 3, 15));
        Pedido c = TestDataFactory.unPedido(3L, EstadoPedido.NUEVO, LocalDate.of(2026, 4, 2));
        when(pedidoRepository.findAll()).thenReturn(List.of(a, b, c));

        List<PedidoResponseDTO> marzo = pedidoService.listar(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(marzo).hasSize(2);
    }

    @Test
    void listar_rangoInvertido_devuelveVacio() {
        assertThat(pedidoService.listar(null, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 3, 1))).isEmpty();
    }

    @Test
    void listarPorUsuario_devuelveElHistorialDelCliente() {
        Pedido p = TestDataFactory.unPedido(1L, EstadoPedido.NUEVO, LocalDate.now());
        when(pedidoRepository.findByUsuarioOrderByFechaDescIdDesc("juan")).thenReturn(List.of(p));

        List<PedidoResponseDTO> r = pedidoService.listarPorUsuario("juan");

        assertThat(r).hasSize(1);
        assertThat(r.get(0).getNumero()).isEqualTo("ORD-1001");
        assertThat(r.get(0).getItems().get(0).getSubtotal()).isEqualByComparingTo("10000");
    }

    @Test
    void obtenerPorNumero_inexistente_lanzaExcepcion() {
        when(pedidoRepository.findByNumeroIgnoreCase("ORD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPorNumero("ORD-9999"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cotizarEnvio_comunaConCobertura_devuelveCostoYPlazo() {
        var c = pedidoService.cotizarEnvio("santiago");

        assertThat(c.getCosto()).isEqualByComparingTo("5900");
        assertThat(c.getMensajeEntrega()).isEqualTo("Entrega estimada entre 2 y 4 días hábiles");
    }

    @Test
    void cotizarEnvio_comunaSinCobertura_lanzaExcepcion() {
        assertThatThrownBy(() -> pedidoService.cotizarEnvio("Atlantis"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
