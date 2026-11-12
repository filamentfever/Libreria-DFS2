package com.libreriajuanin.catalogo.dto;

import java.util.List;

public class CategoriaDTO {

    private String nombre;
    private List<String> subcategorias;

    public CategoriaDTO() {
    }

    public CategoriaDTO(String nombre, List<String> subcategorias) {
        this.nombre = nombre;
        this.subcategorias = subcategorias;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getSubcategorias() {
        return subcategorias;
    }

    public void setSubcategorias(List<String> subcategorias) {
        this.subcategorias = subcategorias;
    }

}
