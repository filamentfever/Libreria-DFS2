package com.libreriajuanin.admin;

import com.libreriajuanin.admin.dto.ItemPedidoDTO;
import com.libreriajuanin.admin.dto.PedidoDTO;
import com.libreriajuanin.admin.dto.ProductoDTO;
import com.libreriajuanin.admin.dto.TotalUsuariosDTO;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static ItemPedidoDTO unItem(Long productoId, String nombre, String precio, int cantidad) {
        ItemPedidoDTO i = new ItemPedidoDTO();
        i.setProductoId(productoId);
        i.setNombreProducto(nombre);
        i.setPrecioUnitario(new BigDecimal(precio));
        i.setCantidad(cantidad);
        i.setSubtotal(new BigDecimal(precio).multiply(BigDecimal.valueOf(cantidad)));
        return i;
    }

    public static PedidoDTO unPedido(Long id, String estado, boolean pagoConfirmado, LocalDate fecha, String total, ItemPedidoDTO... items) {
        PedidoDTO p = new PedidoDTO();
        p.setId(id);
        p.setNumero("ORD-" + (1000 + id));
        p.setFecha(fecha);
        p.setEstado(estado);
        p.setPagoConfirmado(pagoConfirmado);
        p.setTotal(new BigDecimal(total));
        for (ItemPedidoDTO item : items) p.getItems().add(item);
        return p;
    }

    public static ProductoDTO unProducto(Long id, String nombre, int stock, boolean activo) {
        ProductoDTO p = new ProductoDTO();
        p.setId(id);
        p.setNombre(nombre);
        p.setSku("SKU-" + id);
        p.setStock(stock);
        p.setActivo(activo);
        return p;
    }

    public static TotalUsuariosDTO totalUsuarios(long total) {
        TotalUsuariosDTO t = new TotalUsuariosDTO();
        t.setTotal(total);
        return t;
    }
}
