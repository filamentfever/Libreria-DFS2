package com.libreriajuanin.carrito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CarritoResponseDTO {

    private Long id;
    private String usuario;
    private LocalDateTime fechaCreacion;
    private List<ItemCarritoResponseDTO> items = new ArrayList<>();
    private int cantidadTotal;
    private BigDecimal total;

    public CarritoResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<ItemCarritoResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemCarritoResponseDTO> items) {
        this.items = items;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

}
