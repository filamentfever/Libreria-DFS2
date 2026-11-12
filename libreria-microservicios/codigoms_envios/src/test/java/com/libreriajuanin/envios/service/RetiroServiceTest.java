package com.libreriajuanin.envios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.envios.TestDataFactory;
import com.libreriajuanin.envios.dto.InstruccionesRetiroDTO;
import com.libreriajuanin.envios.dto.PuntoRetiroDTO;
import com.libreriajuanin.envios.dto.PuntoRetiroRequestDTO;
import com.libreriajuanin.envios.dto.SeleccionRetiroDTO;
import com.libreriajuanin.envios.exception.RecursoNoEncontradoException;
import com.libreriajuanin.envios.exception.ReglaNegocioException;
import com.libreriajuanin.envios.exception.ValidacionException;
import com.libreriajuanin.envios.model.PuntoRetiro;
import com.libreriajuanin.envios.repository.PuntoRetiroRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetiroServiceTest {

    @Mock
    private PuntoRetiroRepository puntoRetiroRepository;

    @InjectMocks
    private RetiroService retiroService;

    @Test
    void listarPuntos_devuelveSoloLosActivos() {
        when(puntoRetiroRepository.findByActivoTrue()).thenReturn(List.of(
                TestDataFactory.unPunto(1L, "Santiago", true), TestDataFactory.unPunto(2L, "Concepción", true)));

        List<PuntoRetiroDTO> r = retiroService.listarPuntos(null);

        assertThat(r).hasSize(2);
    }

    @Test
    void listarPuntos_filtraPorCiudadIgnorandoTildes() {
        when(puntoRetiroRepository.findByActivoTrue()).thenReturn(List.of(
                TestDataFactory.unPunto(1L, "Santiago", true), TestDataFactory.unPunto(2L, "Concepción", true)));

        List<PuntoRetiroDTO> r = retiroService.listarPuntos("concepcion");

        assertThat(r).hasSize(1);
        assertThat(r.get(0).getCiudad()).isEqualTo("Concepción");
    }

    @Test
    void listarCiudades_sinRepetirYOrdenadas() {
        when(puntoRetiroRepository.findByActivoTrue()).thenReturn(List.of(
                TestDataFactory.unPunto(1L, "Santiago", true), TestDataFactory.unPunto(2L, "Concepción", true),
                TestDataFactory.unPunto(3L, "Santiago", true)));

        assertThat(retiroService.listarCiudades()).containsExactly("Concepción", "Santiago");
    }

    @Test
    void seleccionar_puntoActivo_envioCuestaCeroYAutocompletaLaDireccion() {
        when(puntoRetiroRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.unPunto(1L, "Santiago", true)));

        SeleccionRetiroDTO s = retiroService.seleccionar(1L);

        assertThat(s.getCostoEnvio()).isEqualByComparingTo("0");
        assertThat(s.getDireccionTemporal()).isEqualTo("Calle Ejemplo 100, Santiago");
        assertThat(s.getHorario()).isEqualTo("Lun-Vie: 9:00 - 18:00");
    }

    @Test
    void seleccionar_puntoInactivo_lanzaExcepcion() {
        when(puntoRetiroRepository.findById(4L)).thenReturn(Optional.of(TestDataFactory.unPunto(4L, "Santiago", false)));

        assertThatThrownBy(() -> retiroService.seleccionar(4L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sucursal disponible");
    }

    @Test
    void seleccionar_puntoInexistente_lanzaExcepcion() {
        when(puntoRetiroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> retiroService.seleccionar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void generarCodigo_esDeterministaYTienePrefijoRet() {
        String a = retiroService.generarCodigo("ORD-1001");

        assertThat(a).startsWith("RET-");
        assertThat(a).isEqualTo(retiroService.generarCodigo("ORD-1001"));
        assertThat(a).isNotEqualTo(retiroService.generarCodigo("ORD-1002"));
    }

    @Test
    void instrucciones_incluyenDireccionHorarioYCodigo() {
        when(puntoRetiroRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.unPunto(1L, "Santiago", true)));

        InstruccionesRetiroDTO i = retiroService.instrucciones("ORD-1001", 1L);

        assertThat(i.getTexto()).contains("Calle Ejemplo 100, Santiago");
        assertThat(i.getTexto()).contains("Lun-Vie: 9:00 - 18:00");
        assertThat(i.getTexto()).contains(i.getCodigo());
    }

    @Test
    void crearPunto_datosValidos_loGuardaActivo() {
        PuntoRetiroRequestDTO d = new PuntoRetiroRequestDTO();
        d.setSucursal("Sucursal Nueva");
        d.setDireccion("Calle Nueva 123");
        d.setCiudad("Temuco");
        d.setHorario("Lun-Vie: 9:00 - 18:00");
        when(puntoRetiroRepository.save(any(PuntoRetiro.class))).thenAnswer(inv -> inv.getArgument(0));

        PuntoRetiroDTO r = retiroService.crearPunto(d);

        assertThat(r.isActivo()).isTrue();
        assertThat(r.getCiudad()).isEqualTo("Temuco");
    }

    @Test
    void crearPunto_datosIncompletos_lanzaValidacion() {
        assertThatThrownBy(() -> retiroService.crearPunto(new PuntoRetiroRequestDTO()))
                .isInstanceOf(ValidacionException.class);
        verify(puntoRetiroRepository, never()).save(any());
    }
}
