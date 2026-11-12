package com.libreriajuanin.envios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String pedidoNumero;
    private String destinatario;
    @Column(nullable = false)
    private String direccion;
    @Column(nullable = false)
    private String comuna;
    private BigDecimal costo;
    @Enumerated(EnumType.STRING)
    private EstadoDespacho estadoDespacho;
    private String numeroSeguimiento;
    private String transportista;
    @Column(length = 200)
    private String notas;
    private int diasMin;
    private int diasMax;
    private LocalDateTime fechaCreacion;

    public Envio() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPedidoNumero() {
        return pedidoNumero;
    }

    public void setPedidoNumero(String pedidoNumero) {
        this.pedidoNumero = pedidoNumero;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public EstadoDespacho getEstadoDespacho() {
        return estadoDespacho;
    }

    public void setEstadoDespacho(EstadoDespacho estadoDespacho) {
        this.estadoDespacho = estadoDespacho;
    }

    public String getNumeroSeguimiento() {
        return numeroSeguimiento;
    }

    public void setNumeroSeguimiento(String numeroSeguimiento) {
        this.numeroSeguimiento = numeroSeguimiento;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public int getDiasMin() {
        return diasMin;
    }

    public void setDiasMin(int diasMin) {
        this.diasMin = diasMin;
    }

    public int getDiasMax() {
        return diasMax;
    }

    public void setDiasMax(int diasMax) {
        this.diasMax = diasMax;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

}
