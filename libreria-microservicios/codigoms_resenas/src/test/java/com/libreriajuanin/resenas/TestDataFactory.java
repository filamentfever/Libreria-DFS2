package com.libreriajuanin.resenas;

import com.libreriajuanin.resenas.dto.ProductoDTO;
import com.libreriajuanin.resenas.dto.ResenaRequestDTO;
import com.libreriajuanin.resenas.dto.UsuarioDTO;
import com.libreriajuanin.resenas.model.Resena;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static ResenaRequestDTO unaSolicitud() {
        ResenaRequestDTO d = new ResenaRequestDTO();
        d.setIdProducto(10L);
        d.setIdUsuario(1L);
        d.setPuntuacion(5);
        d.setComentario("Excelente cuaderno");
        return d;
    }

    public static ProductoDTO unProducto() {
        ProductoDTO p = new ProductoDTO();
        p.setId(10L);
        p.setNombre("Cuaderno Miku");
        return p;
    }

    public static UsuarioDTO unUsuario() {
        UsuarioDTO u = new UsuarioDTO();
        u.setId(1L);
        u.setNombre("Ana Pérez");
        return u;
    }

    public static Resena unaResena(int puntuacion, LocalDateTime fecha) {
        Resena r = new Resena();
        r.setIdProducto(10L);
        r.setIdUsuario(1L);
        r.setPuntuacion(puntuacion);
        r.setComentario("Comentario de " + puntuacion + " estrellas");
        r.setFecha(fecha);
        return r;
    }
}
