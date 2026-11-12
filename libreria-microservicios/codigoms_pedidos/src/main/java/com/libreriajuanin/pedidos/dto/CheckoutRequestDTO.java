package com.libreriajuanin.pedidos.dto;

public class CheckoutRequestDTO {

    private Long carritoId;
    private String medioPago;
    private String cuponCodigo;
    private String entrega;
    private String comuna;
    private String direccion;
    private String sucursal;
    private TarjetaDTO tarjeta;

    public CheckoutRequestDTO() {
    }

    public Long getCarritoId() {
        return carritoId;
    }

    public void setCarritoId(Long carritoId) {
        this.carritoId = carritoId;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getCuponCodigo() {
        return cuponCodigo;
    }

    public void setCuponCodigo(String cuponCodigo) {
        this.cuponCodigo = cuponCodigo;
    }

    public String getEntrega() {
        return entrega;
    }

    public void setEntrega(String entrega) {
        this.entrega = entrega;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public TarjetaDTO getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(TarjetaDTO tarjeta) {
        this.tarjeta = tarjeta;
    }

}
