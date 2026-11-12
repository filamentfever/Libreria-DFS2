package com.libreriajuanin.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.libreriajuanin.usuarios.TestDataFactory;
import com.libreriajuanin.usuarios.dto.ActualizarUsuarioRequestDTO;
import com.libreriajuanin.usuarios.dto.DireccionDTO;
import com.libreriajuanin.usuarios.dto.DireccionRequestDTO;
import com.libreriajuanin.usuarios.dto.LoginRequestDTO;
import com.libreriajuanin.usuarios.dto.RecuperacionResponseDTO;
import com.libreriajuanin.usuarios.dto.RegistroRequestDTO;
import com.libreriajuanin.usuarios.dto.UsuarioResponseDTO;
import com.libreriajuanin.usuarios.exception.RecursoNoEncontradoException;
import com.libreriajuanin.usuarios.exception.ReglaNegocioException;
import com.libreriajuanin.usuarios.exception.ValidacionException;
import com.libreriajuanin.usuarios.model.Direccion;
import com.libreriajuanin.usuarios.model.Usuario;
import com.libreriajuanin.usuarios.repository.UsuarioRepository;
import com.libreriajuanin.usuarios.util.Validaciones;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    // ------------------------------------------------------------ HU-01 registrar

    @Test
    void registrar_datosValidos_guardaUsuarioConPasswordCifrada() {
        when(passwordEncoder.encode("Clave123!")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponseDTO resultado = usuarioService.registrar(TestDataFactory.unRegistro());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash-bcrypt");
        assertThat(resultado.getEmail()).isEqualTo("ana@mail.cl");
        assertThat(resultado.getRut()).isEqualTo("11.111.111-1");
        assertThat(resultado.getUsername()).isEqualTo("ana");
    }

    @Test
    void registrar_rutInvalido_lanzaValidacionYNoGuarda() {
        RegistroRequestDTO dto = TestDataFactory.unRegistro();
        dto.setRut("11.111.111-9");

        assertThatThrownBy(() -> usuarioService.registrar(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("rut");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_correoYaRegistrado_lanzaValidacion() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@mail.cl")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(TestDataFactory.unRegistro()))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("Ya existe una cuenta con este correo");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrar_passwordsNoCoinciden_lanzaValidacion() {
        RegistroRequestDTO dto = TestDataFactory.unRegistro();
        dto.setPasswordConfirmacion("Otra123!");

        assertThatThrownBy(() -> usuarioService.registrar(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("passwordConfirmacion");
    }

    @Test
    void registrar_passwordDebil_lanzaValidacion() {
        RegistroRequestDTO dto = TestDataFactory.unRegistro();
        dto.setPassword("abc");
        dto.setPasswordConfirmacion("abc");

        assertThatThrownBy(() -> usuarioService.registrar(dto))
                .isInstanceOf(ValidacionException.class)
                .hasMessageContaining("password");
    }

    @Test
    void registrar_usernameOcupado_agregaSufijoNumerico() {
        when(usuarioRepository.existsByUsernameIgnoreCase("ana")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponseDTO resultado = usuarioService.registrar(TestDataFactory.unRegistro());

        assertThat(resultado.getUsername()).isEqualTo("ana2");
    }

    // ------------------------------------------------------------ HU-03 login

    @Test
    void login_credencialesCorrectas_retornaUsuario() {
        Usuario usuario = TestDataFactory.unUsuario();
        when(usuarioRepository.findByEmailIgnoreCase("ana@mail.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123!", "hash-guardado")).thenReturn(true);
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("ana@mail.cl");
        dto.setPassword("Clave123!");

        UsuarioResponseDTO resultado = usuarioService.login(dto);

        assertThat(resultado.getNombre()).isEqualTo("Ana Pérez");
    }

    @Test
    void login_passwordIncorrecta_lanzaExcepcion() {
        when(usuarioRepository.findByEmailIgnoreCase("ana@mail.cl")).thenReturn(Optional.of(TestDataFactory.unUsuario()));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("ana@mail.cl");
        dto.setPassword("mala");

        assertThatThrownBy(() -> usuarioService.login(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("incorrectos");
    }

    @Test
    void login_correoNoRegistrado_lanzaExcepcion() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@mail.cl")).thenReturn(Optional.empty());
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("nadie@mail.cl");
        dto.setPassword("Clave123!");

        assertThatThrownBy(() -> usuarioService.login(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    // ------------------------------------------------------------ HU-02 recuperar

    @Test
    void recuperarPassword_correoRegistrado_generaTemporalValidaYLaGuarda() {
        Usuario usuario = TestDataFactory.unUsuario();
        when(usuarioRepository.findByEmailIgnoreCase("ana@mail.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(anyString())).thenReturn("hash-temporal");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        RecuperacionResponseDTO resultado = usuarioService.recuperarPassword("ana@mail.cl");

        assertThat(Validaciones.passwordValida(resultado.getPasswordTemporal())).isTrue();
        assertThat(usuario.getPasswordHash()).isEqualTo("hash-temporal");
        assertThat(usuario.isDebeCambiarPassword()).isTrue();
    }

    @Test
    void recuperarPassword_correoNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@mail.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.recuperarPassword("nadie@mail.cl"))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // ------------------------------------------------------------ HU-09 actualizar

    @Test
    void actualizar_datosValidos_cambiaNombreCorreoYTelefono() {
        Usuario usuario = TestDataFactory.unUsuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCase("nuevo@mail.cl")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        ActualizarUsuarioRequestDTO dto = new ActualizarUsuarioRequestDTO();
        dto.setNombre("Ana María");
        dto.setEmail("nuevo@mail.cl");
        dto.setTelefono("+56 9 8765 4321");

        UsuarioResponseDTO resultado = usuarioService.actualizar(1L, dto);

        assertThat(resultado.getNombre()).isEqualTo("Ana María");
        assertThat(resultado.getEmail()).isEqualTo("nuevo@mail.cl");
        assertThat(resultado.getTelefono()).isEqualTo("+56 9 8765 4321");
    }

    @Test
    void actualizar_correoDeOtraCuenta_lanzaValidacion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.unUsuario()));
        when(usuarioRepository.existsByEmailIgnoreCase("otro@mail.cl")).thenReturn(true);
        ActualizarUsuarioRequestDTO dto = new ActualizarUsuarioRequestDTO();
        dto.setNombre("Ana Pérez");
        dto.setEmail("otro@mail.cl");

        assertThatThrownBy(() -> usuarioService.actualizar(1L, dto))
                .isInstanceOf(ValidacionException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // ------------------------------------------------------------ HU-06/07 direcciones

    @Test
    void agregarDireccion_laPrimeraQuedaPredeterminada() {
        Usuario usuario = TestDataFactory.unUsuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        DireccionRequestDTO dto = new DireccionRequestDTO();
        dto.setAlias("Casa");
        dto.setDireccion("Av. Siempre Viva 123");

        List<DireccionDTO> resultado = usuarioService.agregarDireccion(1L, dto);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).isPredeterminada()).isTrue();
    }

    @Test
    void agregarDireccion_nuevaPredeterminada_quitaLaAnterior() {
        Usuario usuario = TestDataFactory.unUsuario();
        Direccion casa = new Direccion("Casa", "Calle Uno 111", null, true, usuario);
        usuario.getDirecciones().add(casa);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        DireccionRequestDTO dto = new DireccionRequestDTO();
        dto.setAlias("Oficina");
        dto.setDireccion("Calle Dos 222");
        dto.setPredeterminada(true);

        usuarioService.agregarDireccion(1L, dto);

        assertThat(usuario.getDirecciones()).hasSize(2);
        assertThat(casa.isPredeterminada()).isFalse();
        assertThat(usuario.getDirecciones().get(1).isPredeterminada()).isTrue();
    }

    @Test
    void agregarDireccion_direccionCorta_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.unUsuario()));
        DireccionRequestDTO dto = new DireccionRequestDTO();
        dto.setDireccion("ab");

        assertThatThrownBy(() -> usuarioService.agregarDireccion(1L, dto))
                .isInstanceOf(ReglaNegocioException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void eliminarDireccion_siEraPredeterminada_laSiguienteLoEs() {
        Usuario usuario = TestDataFactory.unUsuario();
        Direccion casa = new Direccion("Casa", "Calle Uno 111", null, true, usuario);
        casa.setId(1L);
        Direccion oficina = new Direccion("Oficina", "Calle Dos 222", null, false, usuario);
        oficina.setId(2L);
        usuario.getDirecciones().add(casa);
        usuario.getDirecciones().add(oficina);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.eliminarDireccion(1L, 1L);

        assertThat(usuario.getDirecciones()).hasSize(1);
        assertThat(oficina.isPredeterminada()).isTrue();
    }

    // ------------------------------------------------------------ búsqueda por nombre

    @Test
    void buscarPorUsername_noExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsernameIgnoreCase("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorUsername("fantasma"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no existe");
    }

        @Test
        void contarUsuarios_devuelveElTotalRegistrado() {
            when(usuarioRepository.count()).thenReturn(3L);

            assertThat(usuarioService.contarUsuarios()).isEqualTo(3L);
        }
    }
