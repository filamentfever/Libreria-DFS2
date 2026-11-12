package com.libreriajuanin.pedidos.dto;

public class StockRequestDTO {

    private Integer cantidad;

    public StockRequestDTO() {
    }

    public StockRequestDTO(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

}
