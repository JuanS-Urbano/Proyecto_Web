package com.grupo1.editorprocesos.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo1.editorprocesos.dto.GatewayDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.bpmn.Gateway;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.TipoGateway;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.repository.GatewayRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.service.ProcesoService;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.GatewayServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GatewayServiceImplTest {

    @Mock
    private GatewayRepository gatewayRepository;

    @Mock
    private com.grupo1.editorprocesos.service.PermisosPoolService permisosPoolService;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistorialCambiosRepository historialCambiosRepository;

    @Mock
    private ArcoRepository arcoRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private GatewayServiceImpl gatewayService;

    private Empresa empresa;
    private Usuario usuario;
    private Pool pool;
    private Proceso proceso;
    private Gateway gateway;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("test@empresa.com");
        usuario.setEmpresa(empresa);

        pool = new Pool();
        pool.setId(2L);
        pool.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(5L);
        proceso.setPool(pool);

        gateway = new Gateway();
        gateway.setId(15L);
        gateway.setNombre("GatewayX");
        gateway.setTipoGateway(TipoGateway.EXCLUSIVO);
        gateway.setProceso(proceso);

        org.mockito.Mockito.lenient().when(httpServletRequest.getHeader("X-User-Email")).thenReturn("test@empresa.com");
        org.mockito.Mockito.lenient().when(usuarioRepository.findByEmail("test@empresa.com")).thenReturn(Optional.of(usuario));
    }

    @Test
    void crearGateway_exitoso() {
        GatewayDTO dto = new GatewayDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setNombre("NuevoGateway");
        dto.setTipoGateway(TipoGateway.EXCLUSIVO);

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(gatewayRepository.findByNombreAndProcesoId("NuevoGateway", 5L)).thenReturn(Optional.empty());
        when(gatewayRepository.save(any(Gateway.class))).thenReturn(gateway);

        GatewayDTO result = gatewayService.crearGateway(dto);

        assertThat(result.getId()).isEqualTo(15L);
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void crearGateway_nombreDuplicado() {
        GatewayDTO dto = new GatewayDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setNombre("GatewayX");
        dto.setTipoGateway(TipoGateway.EXCLUSIVO);

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(gatewayRepository.findByNombreAndProcesoId("GatewayX", 5L)).thenReturn(Optional.of(new Gateway()));

        assertThatThrownBy(() -> gatewayService.crearGateway(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un gateway con el nombre");
    }

    @Test
    void crearGateway_tipoGatewayNulo() {
        GatewayDTO dto = new GatewayDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));
        dto.setNombre("GatewayX");
        // tipoGateway is null

        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);

        assertThatThrownBy(() -> gatewayService.crearGateway(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de gateway es requerido");
    }

    @Test
    void editarGateway_exitoso() {
        GatewayDTO request = new GatewayDTO();
        request.setNombre("GatewayY");

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(gatewayRepository.findByNombreAndProcesoId("GatewayY", 5L)).thenReturn(Optional.empty());
        when(gatewayRepository.save(gateway)).thenReturn(gateway);

        GatewayDTO result = gatewayService.editarGateway(15L, request);

        assertThat(result.getNombre()).isEqualTo("GatewayY");
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void eliminarGateway_conSaneamientoSingular() {
        Arco entrante = new Arco();
        entrante.setOrigenId("Act1");
        entrante.setDestinoId("GatewayX");
        entrante.setProceso(proceso);

        Arco saliente = new Arco();
        saliente.setOrigenId("GatewayX");
        saliente.setDestinoId("Act2");
        saliente.setProceso(proceso);

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(arcoRepository.findByDestinoIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of(entrante));
        when(arcoRepository.findByOrigenIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of(saliente));
        when(arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId("Act1", "Act2", 5L)).thenReturn(Optional.empty());

        gatewayService.eliminarGateway(15L);

        verify(arcoRepository).delete(entrante);
        verify(arcoRepository).delete(saliente);
        verify(arcoRepository).save(any(Arco.class)); // El nuevo arco saneado
        verify(gatewayRepository).delete(gateway);
    }

    @Test
    void eliminarGateway_multiplesArcos() {
        Arco entrante = new Arco();
        entrante.setOrigenId("Act1");
        
        Arco saliente1 = new Arco();
        saliente1.setDestinoId("Act2");
        
        Arco saliente2 = new Arco();
        saliente2.setDestinoId("Act3");

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(arcoRepository.findByDestinoIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of(entrante));
        when(arcoRepository.findByOrigenIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of(saliente1, saliente2));

        gatewayService.eliminarGateway(15L);

        verify(arcoRepository, org.mockito.Mockito.times(2)).deleteAll(any(List.class)); // Borra entrantes y salientes
        verify(arcoRepository, org.mockito.Mockito.times(0)).save(any(Arco.class)); // No se conecta nada
        verify(gatewayRepository).delete(gateway);
    }

    @Test
    void editarGateway_nombreDuplicado() {
        GatewayDTO request = new GatewayDTO();
        request.setNombre("GatewayZ");

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(gatewayRepository.findByNombreAndProcesoId("GatewayZ", 5L)).thenReturn(Optional.of(new Gateway()));

        assertThatThrownBy(() -> gatewayService.editarGateway(15L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void editarGateway_sinCambios() {
        GatewayDTO request = new GatewayDTO();
        // Mismos valores que el actual
        request.setNombre("GatewayX");
        request.setTipoGateway(TipoGateway.EXCLUSIVO);

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(gatewayRepository.save(gateway)).thenReturn(gateway);

        GatewayDTO result = gatewayService.editarGateway(15L, request);

        assertThat(result.getNombre()).isEqualTo("GatewayX");
        // No deberÃ­a registrar historial porque no hubo cambios
        verify(historialCambiosRepository, org.mockito.Mockito.never()).save(any(HistorialCambios.class));
    }

    @Test
    void obtenerGatewayPorId_exitoso() {
        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));

        GatewayDTO result = gatewayService.obtenerGatewayPorId(15L);

        assertThat(result.getId()).isEqualTo(15L);
        assertThat(result.getNombre()).isEqualTo("GatewayX");
    }

    @Test
    void obtenerGatewayPorId_noEncontrado() {
        when(gatewayRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gatewayService.obtenerGatewayPorId(15L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listarGatewaysPorProceso_exitoso() {
        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);
        when(gatewayRepository.findByProcesoId(5L)).thenReturn(List.of(gateway));

        List<GatewayDTO> result = gatewayService.listarGatewaysPorProceso(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(15L);
    }

    @Test
    void eliminarGateway_noEncontrado() {
        when(gatewayRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gatewayService.eliminarGateway(15L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarGateway_conSaneamientoSingular_arcoYaExiste() {
        Arco entrante = new Arco();
        entrante.setOrigenId("Act1");
        entrante.setDestinoId("GatewayX");
        entrante.setProceso(proceso);

        Arco saliente = new Arco();
        saliente.setOrigenId("GatewayX");
        saliente.setDestinoId("Act2");
        saliente.setProceso(proceso);

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(arcoRepository.findByDestinoIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of(entrante));
        when(arcoRepository.findByOrigenIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of(saliente));
        // ya existe
        when(arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId("Act1", "Act2", 5L)).thenReturn(Optional.of(new Arco()));

        gatewayService.eliminarGateway(15L);

        verify(arcoRepository).delete(entrante);
        verify(arcoRepository).delete(saliente);
        verify(arcoRepository, org.mockito.Mockito.never()).save(any(Arco.class)); // No lo crea
        verify(gatewayRepository).delete(gateway);
    }

    @Test
    void eliminarGateway_sinArcos_soloElimina() {
        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(arcoRepository.findByDestinoIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of());
        when(arcoRepository.findByOrigenIdAndProcesoId("GatewayX", 5L)).thenReturn(List.of());

        gatewayService.eliminarGateway(15L);

        verify(arcoRepository, org.mockito.Mockito.times(2)).deleteAll(List.of());
        verify(gatewayRepository).delete(gateway);
    }

    @Test
    void obtenerUsuarioActual_sinHeader() {
        when(httpServletRequest.getHeader("X-User-Email")).thenReturn(null);
        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);

        GatewayDTO dto = new GatewayDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));

        assertThatThrownBy(() -> gatewayService.crearGateway(dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("No se proporcionó el header");
    }

    @Test
    void validarUsuarioPertenecAEmpresa_otraEmpresa() {
        Usuario usuarioOtra = new Usuario();
        usuarioOtra.setId(100L);
        usuarioOtra.setEmail("otra@emp.com");
        Empresa otraEm = new Empresa();
        otraEm.setId(99L);
        usuarioOtra.setEmpresa(otraEm);

        when(httpServletRequest.getHeader("X-User-Email")).thenReturn("otra@emp.com");
        when(usuarioRepository.findByEmail("otra@emp.com")).thenReturn(Optional.of(usuarioOtra));
        when(procesoService.obtenerEntityById(5L)).thenReturn(proceso);

        GatewayDTO dto = new GatewayDTO();
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));

        assertThatThrownBy(() -> gatewayService.crearGateway(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void editarGateway_cambioPosicionXY() {
        GatewayDTO request = new GatewayDTO();
        request.setNombre("GatewayX");
        request.setTipoGateway(TipoGateway.EXCLUSIVO);
        request.setPosicionX(500.0);
        request.setPosicionY(600.0);

        when(gatewayRepository.findById(15L)).thenReturn(Optional.of(gateway));
        when(gatewayRepository.save(gateway)).thenReturn(gateway);

        GatewayDTO result = gatewayService.editarGateway(15L, request);

        assertThat(result.getPosicionX()).isEqualTo(500.0);
        assertThat(result.getPosicionY()).isEqualTo(600.0);
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }
}
