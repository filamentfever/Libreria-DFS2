package com.libreriajuanin.envios.dto;

public class TrackingRequestDTO {

    private String transportista;
    private String codigo;

    public TrackingRequestDTO() {
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

}
