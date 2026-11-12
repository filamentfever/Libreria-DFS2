package com.libreriajuanin.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String slug;
    @Column(nullable = false, unique = true)
    private String sku;
    @Column(nullable = false)
    private String nombre;
    @Column(length = 1000)
    private String descripcion;
    @Column(nullable = false)
    private BigDecimal precio;
    private String categoria;
    private String subcategoria;
    private String marca;
    @Column(length = 300)
    private String colores;
    private String imagen;
    private int stock;
    private boolean activo;
    private boolean ocultoPorStock;
    private LocalDate fechaIngreso;

    public Producto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getColores() {
        return colores;
    }

    public void setColores(String colores) {
        this.colores = colores;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isOcultoPorStock() {
        return ocultoPorStock;
    }

    public void setOcultoPorStock(boolean ocultoPorStock) {
        this.ocultoPorStock = ocultoPorStock;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    /** Los colores se guardan separados por coma ("Rojo,Azul"). */
    public List<String> getColoresLista() {
        if (colores == null || colores.isBlank()) return List.of();
        return Arrays.stream(colores.split(",")).map(String::trim).filter(c -> !c.isEmpty()).toList();
    }

    public void setColoresLista(List<String> lista) {
        this.colores = lista == null ? "" : String.join(",", lista);
    }

}
