package com.libreriajuanin.pagos.dto;

import java.util.Map;

public class ResultadoPagoDTO {

    private boolean aprobado;
    private String codigoAutorizacion;
    private String motivo;
    private String mensaje;
    private boolean puedeReintentar;
    private Map<String, String> errores;

    public ResultadoPagoDTO() {
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }

    public void setCodigoAutorizacion(String codigoAutorizacion) {
        this.codigoAutorizacion = codigoAutorizacion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isPuedeReintentar() {
        return puedeReintentar;
    }

    public void setPuedeReintentar(boolean puedeReintentar) {
        this.puedeReintentar = puedeReintentar;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public void setErrores(Map<String, String> errores) {
        this.errores = errores;
    }

}
