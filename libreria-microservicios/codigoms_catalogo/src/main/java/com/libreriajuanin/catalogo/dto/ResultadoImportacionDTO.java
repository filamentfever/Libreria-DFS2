package com.libreriajuanin.catalogo.dto;

import java.util.ArrayList;
import java.util.List;

public class ResultadoImportacionDTO {

    private int insertados;
    private int actualizados;
    private List<ErrorImportacionDTO> errores = new ArrayList<>();

    public ResultadoImportacionDTO() {
    }

    public int getInsertados() {
        return insertados;
    }

    public void setInsertados(int insertados) {
        this.insertados = insertados;
    }

    public int getActualizados() {
        return actualizados;
    }

    public void setActualizados(int actualizados) {
        this.actualizados = actualizados;
    }

    public List<ErrorImportacionDTO> getErrores() {
        return errores;
    }

    public void setErrores(List<ErrorImportacionDTO> errores) {
        this.errores = errores;
    }

}
