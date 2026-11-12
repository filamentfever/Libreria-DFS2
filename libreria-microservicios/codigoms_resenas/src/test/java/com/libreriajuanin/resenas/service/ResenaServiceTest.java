package com.libreriajuanin.resenas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.resenas.TestDataFactory;
import com.libreriajuanin.resenas.client.CatalogoClient;
import com.libreriajuanin.resenas.client.UsuariosClient;
import com.libreriajuanin.resenas.dto.ResenaRequestDTO;
import com.libreriajuanin.resenas.dto.ResenaResponseDTO;
import com.libreriajuanin.resenas.dto.ResumenValoracionDTO;
import com.libreriajuanin.resenas.exception.RecursoNoEncontradoException;
import com.libreriajuanin.resenas.exception.ValidacionException;
import com.libreriajuanin.resenas.model.Resena;
import com.libreriajuanin.resenas.repository.ResenaRepository;
import feign.FeignException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;
    @Mock
    private CatalogoClient catalogoClient;
    @Mock
    private UsuariosClient usuariosClient;

    @InjectMocks
    private ResenaService resenaService;

    @Test
    void crear_datosValidos_guardaLaResenaConElNombreDelAutor() {
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProducto());
        when(usuariosClient.obtenerUsuario(1L)).thenReturn(TestDataFactory.unUsuario());
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        ResenaResponseDTO r = resenaService.crear(TestDataFactory.unaSolicitud());

        assertThat(r.getPuntuacion()).isEqualTo(5);
        assertThat(r.getAutor()).isEqualTo("Ana Pérez");
        assertThat(r.getFecha()).isNotNull();
    }

    @Test
    void crear_puntuacionFueraDeRango_lanzaValidacionYNoGuarda() {
        ResenaRequestDTO d = TestDataFactory.unaSolicitud();
        d.setPuntuacion(6);

        assertThatThrownBy(() -> resenaService.crear(d))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("puntuacion");
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void crear_puntuacionCero_lanzaValidacion() {
        ResenaRequestDTO d = TestDataFactory.unaSolicitud();
        d.setPuntuacion(0);

        assertThatThrownBy(() -> resenaService.crear(d)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void crear_comentarioVacio_lanzaValidacion() {
        ResenaRequestDTO d = TestDataFactory.unaSolicitud();
        d.setComentario(" ");

        assertThatThrownBy(() -> resenaService.crear(d))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("comentario");
    }

    @Test
    void crear_productoNoExiste_lanzaExcepcion() {
        when(catalogoClient.obtenerProducto(anyLong())).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> resenaService.crear(TestDataFactory.unaSolicitud()))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no existe en el catalogo");
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void crear_usuarioNoExiste_lanzaExcepcion() {
        when(catalogoClient.obtenerProducto(10L)).thenReturn(TestDataFactory.unProducto());
        when(usuariosClient.obtenerUsuario(anyLong())).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> resenaService.crear(TestDataFactory.unaSolicitud()))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no existe en el sistema");
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void promedio_calculaConUnDecimalYLaDistribucion() {
        LocalDateTime ahora = LocalDateTime.now();
        when(resenaRepository.findByIdProductoOrderByFechaDesc(10L)).thenReturn(List.of(
                TestDataFactory.unaResena(5, ahora), TestDataFactory.unaResena(4, ahora), TestDataFactory.unaResena(4, ahora)));

        ResumenValoracionDTO r = resenaService.promedio(10L);

        assertThat(r.getPromedio()).isEqualTo(4.3);
        assertThat(r.getCantidad()).isEqualTo(3L);
        assertThat(r.getDistribucion().get(4)).isEqualTo(2L);
        assertThat(r.getDistribucion().get(1)).isEqualTo(0L);
    }

    @Test
    void promedio_sinResenas_esNulo() {
        when(resenaRepository.findByIdProductoOrderByFechaDesc(10L)).thenReturn(List.of());

        ResumenValoracionDTO r = resenaService.promedio(10L);

        assertThat(r.getPromedio()).isNull();
        assertThat(r.getCantidad()).isZero();
    }

    @Test
    void listarPorProducto_devuelveLasResenasEnElOrdenDelRepositorio() {
        LocalDateTime ahora = LocalDateTime.now();
        when(resenaRepository.findByIdProductoOrderByFechaDesc(10L)).thenReturn(List.of(
                TestDataFactory.unaResena(5, ahora), TestDataFactory.unaResena(3, ahora.minusDays(2))));

        List<ResenaResponseDTO> r = resenaService.listarPorProducto(10L);

        assertThat(r).hasSize(2);
        assertThat(r.get(0).getPuntuacion()).isEqualTo(5);
    }
}
