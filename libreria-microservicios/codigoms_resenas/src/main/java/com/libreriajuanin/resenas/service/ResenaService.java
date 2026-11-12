package com.libreriajuanin.resenas.service;

import com.libreriajuanin.resenas.client.CatalogoClient;
import com.libreriajuanin.resenas.client.UsuariosClient;
import com.libreriajuanin.resenas.dto.ResenaRequestDTO;
import com.libreriajuanin.resenas.dto.ResenaResponseDTO;
import com.libreriajuanin.resenas.dto.ResumenValoracionDTO;
import com.libreriajuanin.resenas.dto.UsuarioDTO;
import com.libreriajuanin.resenas.exception.RecursoNoEncontradoException;
import com.libreriajuanin.resenas.exception.ValidacionException;
import com.libreriajuanin.resenas.model.Resena;
import com.libreriajuanin.resenas.repository.ResenaRepository;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Valoraciones y reseñas (HU-75, HU-76, HU-77; resenaService.ts). */
@Service
@Transactional
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final CatalogoClient catalogoClient;
    private final UsuariosClient usuariosClient;

    public ResenaService(ResenaRepository resenaRepository, CatalogoClient catalogoClient, UsuariosClient usuariosClient) {
        this.resenaRepository = resenaRepository;
        this.catalogoClient = catalogoClient;
        this.usuariosClient = usuariosClient;
    }

    /** HU-75: valida estrellas (1 a 5) y comentario; confirma producto y usuario en sus microservicios. */
    public ResenaResponseDTO crear(ResenaRequestDTO dto) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (dto.getIdProducto() == null) errores.put("idProducto", "Indica el producto.");
        if (dto.getIdUsuario() == null) errores.put("idUsuario", "Indica el usuario.");
        if (dto.getPuntuacion() == null || dto.getPuntuacion() < 1 || dto.getPuntuacion() > 5) {
            errores.put("puntuacion", "Selecciona una calificación de 1 a 5 estrellas.");
        }
        if (dto.getComentario() == null || dto.getComentario().trim().length() < 3) {
            errores.put("comentario", "Escribe un comentario de al menos 3 caracteres.");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        try {
            catalogoClient.obtenerProducto(dto.getIdProducto());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El producto con id " + dto.getIdProducto() + " no existe en el catalogo");
        }
        UsuarioDTO usuario;
        try {
            usuario = usuariosClient.obtenerUsuario(dto.getIdUsuario());
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El usuario con id " + dto.getIdUsuario() + " no existe en el sistema");
        }

        Resena resena = new Resena();
        resena.setIdProducto(dto.getIdProducto());
        resena.setIdUsuario(dto.getIdUsuario());
        resena.setAutor(usuario == null ? null : usuario.getNombre());
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setComentario(dto.getComentario().trim());
        resena.setFecha(LocalDateTime.now());
        return aDTO(resenaRepository.save(resena));
    }

    /** HU-77: listado ordenado desde la reseña más reciente. */
    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> listarPorProducto(Long idProducto) {
        return resenaRepository.findByIdProductoOrderByFechaDesc(idProducto).stream().map(this::aDTO).toList();
    }

    /** HU-76: promedio con 1 decimal (null si no hay reseñas) y cuántas hay de cada puntuación. */
    @Transactional(readOnly = true)
    public ResumenValoracionDTO promedio(Long idProducto) {
        List<Resena> resenas = resenaRepository.findByIdProductoOrderByFechaDesc(idProducto);
        ResumenValoracionDTO r = new ResumenValoracionDTO();
        r.setIdProducto(idProducto);
        for (int estrellas = 5; estrellas >= 1; estrellas--) {
            final int e = estrellas;
            r.getDistribucion().put(e, resenas.stream().filter(x -> x.getPuntuacion() == e).count());
        }
        r.setCantidad(resenas.size());
        if (resenas.isEmpty()) {
            r.setPromedio(null);
            return r;
        }
        double suma = resenas.stream().mapToInt(Resena::getPuntuacion).sum();
        r.setPromedio(Math.round(suma / resenas.size() * 10) / 10.0);
        return r;
    }

    public ResenaResponseDTO aDTO(Resena r) {
        ResenaResponseDTO dto = new ResenaResponseDTO();
        dto.setId(r.getId());
        dto.setIdProducto(r.getIdProducto());
        dto.setIdUsuario(r.getIdUsuario());
        dto.setAutor(r.getAutor());
        dto.setPuntuacion(r.getPuntuacion());
        dto.setComentario(r.getComentario());
        dto.setFecha(r.getFecha());
        return dto;
    }
}
