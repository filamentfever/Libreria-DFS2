package com.libreriajuanin.pedidos.model;

/** Estados del pedido (HU-60, HU-70 a HU-73). */
public enum EstadoPedido {
    NUEVO,
    PENDIENTE,
    ARMANDO_CAJA,
    LISTO_PARA_RETIRO,
    ENVIADO,
    ENTREGADO,
    CANCELADO
}
