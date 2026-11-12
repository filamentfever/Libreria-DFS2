package com.libreriajuanin.pedidos;

import com.libreriajuanin.pedidos.dto.CarritoDTO;
import com.libreriajuanin.pedidos.dto.CheckoutRequestDTO;
import com.libreriajuanin.pedidos.dto.ItemCarritoDTO;
import com.libreriajuanin.pedidos.dto.ProductoDTO;
import com.libreriajuanin.pedidos.dto.ResultadoPagoDTO;
import com.libreriajuanin.pedidos.dto.TarjetaDTO;
import com.libreriajuanin.pedidos.model.EstadoPedido;
import com.libreriajuanin.pedidos.model.ItemPedido;
import com.libreriajuanin.pedidos.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    /** Carrito 1 de "juan": 2 x Cuaderno Miku a $19.990 (subtotal $39.980). */
    public static CarritoDTO unCarritoDTO() {
        CarritoDTO c = new CarritoDTO();
        c.setId(1L);
        c.setUsuario("juan");
        ItemCarritoDTO item = new ItemCarritoDTO();
        item.setId(5L);
        item.setProductoId(10L);
        item.setNombreProducto("Cuaderno Miku");
        item.setPrecioUnitario(new BigDecimal("19990.00"));
        item.setCantidad(2);
        c.getItems().add(item);
        return c;
    }

    public static CarritoDTO unCarritoVacioDTO() {
        CarritoDTO c = new CarritoDTO();
        c.setId(1L);
        c.setUsuario("juan");
        return c;
    }

    public static ProductoDTO unProductoDTO(int stock) {
        ProductoDTO p = new ProductoDTO();
        p.setId(10L);
        p.setNombre("Cuaderno Miku");
        p.setPrecio(new BigDecimal("19990.00"));
        p.setStock(stock);
        p.setActivo(stock > 0);
        return p;
    }

    /** Checkout a domicilio en Santiago (envío $5.900) pagando con tarjeta. */
    public static CheckoutRequestDTO unCheckoutDomicilio() {
        CheckoutRequestDTO c = new CheckoutRequestDTO();
        c.setCarritoId(1L);
        c.setMedioPago("TARJETA");
        c.setEntrega("DOMICILIO");
        c.setComuna("Santiago");
        c.setDireccion("Calle Uno 123");
        TarjetaDTO t = new TarjetaDTO();
        t.setTitular("Juan Pérez");
        t.setNumero("4111 1111 1111 1111");
        t.setVencimiento("12/99");
        t.setCvv("123");
        c.setTarjeta(t);
        return c;
    }

    public static CheckoutRequestDTO unCheckoutRetiro() {
        CheckoutRequestDTO c = unCheckoutDomicilio();
        c.setEntrega("RETIRO");
        c.setSucursal("Sucursal Centro");
        c.setComuna(null);
        c.setDireccion(null);
        return c;
    }

    public static ResultadoPagoDTO pagoAprobado() {
        ResultadoPagoDTO r = new ResultadoPagoDTO();
        r.setAprobado(true);
        r.setCodigoAutorizacion("AUT-123456");
        r.setMensaje("Pago aprobado.");
        return r;
    }

    public static ResultadoPagoDTO pagoRechazado() {
        ResultadoPagoDTO r = new ResultadoPagoDTO();
        r.setAprobado(false);
        r.setMotivo("saldo_insuficiente");
        r.setMensaje("Pago rechazado: saldo insuficiente en la tarjeta.");
        r.setPuedeReintentar(true);
        return r;
    }

    public static Pedido unPedido(Long id, EstadoPedido estado, LocalDate fecha) {
        Pedido p = new Pedido();
        p.setId(id);
        p.setNumero("ORD-" + (1000 + id));
        p.setUsuario("juan");
        p.setFecha(fecha);
        p.setEstado(estado);
        p.setEntrega("DOMICILIO");
        p.setMedioPago("TARJETA");
        p.setSubtotal(new BigDecimal("10000"));
        p.setDescuento(BigDecimal.ZERO);
        p.setEnvio(BigDecimal.ZERO);
        p.setNeto(new BigDecimal("8403"));
        p.setIva(new BigDecimal("1597"));
        p.setTotal(new BigDecimal("10000"));
        p.getItems().add(new ItemPedido(10L, "Cuaderno Miku", new BigDecimal("5000"), 2, p));
        return p;
    }
}
