package com.libreriajuanin.pedidos.dto;

import java.util.ArrayList;
import java.util.List;

public class CarritoDTO {

    private Long id;
    private String usuario;
    private List<ItemCarritoDTO> items = new ArrayList<>();

    public CarritoDTO() {
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

    public List<ItemCarritoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemCarritoDTO> items) {
        this.items = items;
    }

}
