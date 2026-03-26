package com.grupo1.editorprocesos.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo1.editorprocesos.dto.ActividadDTO;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.TipoActividad;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.ActividadServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ActividadServiceImplTest {

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private com.grupo1.editorprocesos.service.PermisosPoolService permisosPoolService;

    @Mock
    private ProcesoRepository procesoRepository;

    @Mock
    private ArcoRepository arcoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistorialCambiosRepository historialCambiosRepository;

    @Mock
    private LaneService laneService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ActividadServiceImpl actividadService;

    private Empresa empresa;
    private Pool pool;
    private Proceso proceso;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        pool = new Pool();
        pool.setId(1L);
        pool.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(10L);
        proceso.setPool(pool);

        usuario = new Usuario();
        usuario.setId(99L);
        usuario.setEmail("user@empresa.com");
        usuario.setEmpresa(empresa);

        // Estos stubs son utilizados por la mayoría de los tests, pero algunos casos
        // se detienen antes de necesitarlos. Los marcamos como lenient para evitar
        // UnnecessaryStubbingException.
        org.mockito.Mockito.lenient().when(httpServletRequest.getHeader("X-User-Email")).thenReturn(usuario.getEmail());
        org.mockito.Mockito.lenient().when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    @Test
    void crearActividad_conLaneValido_exitoso() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        Lane lane = new Lane();
        lane.setId(3L);
        lane.setProceso(proceso);

        when(laneService.obtenerLaneEntityById(lane.getId())).thenReturn(lane);

        Actividad actividadGuardada = new Actividad();
        actividadGuardada.setId(42L);
        actividadGuardada.setProceso(proceso);
        actividadGuardada.setLane(lane);
        when(actividadRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(actividadGuardada);

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Actividad 1");
        dto.setTipoActividad(TipoActividad.MANUAL);
        dto.setProcesoId(proceso.getId());
        dto.setLaneId(lane.getId());

        ActividadDTO result = actividadService.crearActividad(dto);

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getLaneId()).isEqualTo(lane.getId());
    }

    @Test
    void crearActividad_conLaneDeOtroProceso_falla() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        org.mockito.Mockito.doThrow(new IllegalArgumentException("El lane con ID 3 no pertenece al proceso " + proceso.getId()))
                .when(laneService).validarLanePerteneceAlProceso(3L, proceso.getId());

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Actividad 1");
        dto.setTipoActividad(TipoActividad.MANUAL);
        dto.setProcesoId(proceso.getId());
        dto.setLaneId(3L);

        assertThatThrownBy(() -> actividadService.crearActividad(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pertenece al proceso");
    }

    @Test
    void crearActividad_sinHeaderUsuario_falla() {
        when(httpServletRequest.getHeader("X-User-Email")).thenReturn(null);
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Actividad 1");
        dto.setTipoActividad(TipoActividad.MANUAL);
        dto.setProcesoId(proceso.getId());

        assertThatThrownBy(() -> actividadService.crearActividad(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void editarActividad_exitoso() {
        // Setup
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Viejo Nombre");
        actividad.setTipoActividad(TipoActividad.MANUAL);
        actividad.setPosicionX(100.0);
        actividad.setPosicionY(100.0);
        actividad.setProceso(proceso);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));

        Lane nuevoLane = new Lane();
        nuevoLane.setId(2L);
        nuevoLane.setProceso(proceso);
        when(laneService.obtenerLaneEntityById(2L)).thenReturn(nuevoLane);

        when(actividadRepository.save(org.mockito.ArgumentMatchers.any(Actividad.class))).thenAnswer(i -> i.getArgument(0));

        // Request
        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Nuevo Nombre");
        dto.setTipoActividad(TipoActividad.USUARIO);
        dto.setPosicionX(200.0);
        dto.setPosicionY(200.0);
        dto.setLaneId(2L);

        // Act
        ActividadDTO result = actividadService.editarActividad(1L, dto);

        // Assert
        assertThat(result.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(result.getTipoActividad()).isEqualTo(TipoActividad.USUARIO);
        assertThat(result.getPosicionX()).isEqualTo(200.0);
        assertThat(result.getPosicionY()).isEqualTo(200.0);
        assertThat(result.getLaneId()).isEqualTo(2L);
    }

    @Test
    void obtenerActividadPorId_exitoso() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Actividad Test");
        actividad.setProceso(proceso);
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));

        ActividadDTO result = actividadService.obtenerActividadPorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Actividad Test");
    }

    @Test
    void listarActividadesPorProceso_exitoso() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));
        
        Actividad a1 = new Actividad();
        a1.setId(1L);
        a1.setProceso(proceso);
        Actividad a2 = new Actividad();
        a2.setId(2L);
        a2.setProceso(proceso);
        
        when(actividadRepository.findByProcesoId(proceso.getId())).thenReturn(java.util.List.of(a1, a2));

        java.util.List<ActividadDTO> result = actividadService.listarActividadesPorProceso(proceso.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void eliminarActividad_con1Entrante1Saliente_reconectaArcos() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Medio");
        actividad.setProceso(proceso);
        
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        
        Arco entrante = new Arco();
        entrante.setOrigenId("Inicio");
        entrante.setDestinoId("Medio");
        
        Arco saliente = new Arco();
        saliente.setOrigenId("Medio");
        saliente.setDestinoId("Fin");

        when(arcoRepository.findByDestinoIdAndProcesoId("Medio", proceso.getId())).thenReturn(java.util.List.of(entrante));
        when(arcoRepository.findByOrigenIdAndProcesoId("Medio", proceso.getId())).thenReturn(java.util.List.of(saliente));
        when(arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId("Inicio", "Fin", proceso.getId())).thenReturn(Optional.empty());

        actividadService.eliminarActividad(1L);

        org.mockito.Mockito.verify(arcoRepository).delete(entrante);
        org.mockito.Mockito.verify(arcoRepository).delete(saliente);
        org.mockito.Mockito.verify(arcoRepository).save(org.mockito.ArgumentMatchers.any(Arco.class));
        org.mockito.Mockito.verify(actividadRepository).delete(actividad);
    }

    @Test
    void eliminarActividad_sinArcos_soloElimina() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Medio");
        actividad.setProceso(proceso);
        
        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(arcoRepository.findByDestinoIdAndProcesoId("Medio", proceso.getId())).thenReturn(java.util.List.of());
        when(arcoRepository.findByOrigenIdAndProcesoId("Medio", proceso.getId())).thenReturn(java.util.List.of());

        actividadService.eliminarActividad(1L);

        org.mockito.Mockito.verify(arcoRepository, org.mockito.Mockito.times(2)).deleteAll(java.util.List.of());
        org.mockito.Mockito.verify(actividadRepository).delete(actividad);
    }

    @Test
    void crearActividad_sinLane_exitoso() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));
        
        Actividad actividadGuardada = new Actividad();
        actividadGuardada.setId(43L);
        actividadGuardada.setNombre("Actividad Sin Lane");
        actividadGuardada.setTipoActividad(TipoActividad.MANUAL);
        actividadGuardada.setProceso(proceso);
        when(actividadRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(actividadGuardada);

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Actividad Sin Lane");
        dto.setTipoActividad(TipoActividad.MANUAL);
        dto.setProcesoId(proceso.getId());
        dto.setLaneId(null);

        ActividadDTO result = actividadService.crearActividad(dto);

        assertThat(result.getId()).isEqualTo(43L);
        assertThat(result.getLaneId()).isNull();
    }

    @Test
    void crearActividad_procesoNoEncontrado_falla() {
        when(procesoRepository.findById(999L)).thenReturn(Optional.empty());

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Actividad 1");
        dto.setProcesoId(999L);

        assertThatThrownBy(() -> actividadService.crearActividad(dto))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void crearActividad_tipoNulo_falla() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Actividad 1");
        dto.setTipoActividad(null);
        dto.setProcesoId(proceso.getId());

        assertThatThrownBy(() -> actividadService.crearActividad(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void editarActividad_noEncontrada_falla() {
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        ActividadDTO dto = new ActividadDTO();

        assertThatThrownBy(() -> actividadService.editarActividad(999L, dto))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void editarActividad_sinCambios_exitoso() {
        Actividad actividad = new Actividad();
        actividad.setId(1L);
        actividad.setNombre("Mismo Nombre");
        actividad.setTipoActividad(TipoActividad.MANUAL);
        actividad.setPosicionX(10.0);
        actividad.setPosicionY(10.0);
        actividad.setProceso(proceso);

        when(actividadRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(actividadRepository.save(org.mockito.ArgumentMatchers.any(Actividad.class))).thenAnswer(i -> i.getArgument(0));

        ActividadDTO dto = new ActividadDTO();
        dto.setNombre("Mismo Nombre");
        dto.setTipoActividad(TipoActividad.MANUAL);
        dto.setPosicionX(10.0);
        dto.setPosicionY(10.0);
        dto.setLaneId(null);

        ActividadDTO result = actividadService.editarActividad(1L, dto);

        assertThat(result.getNombre()).isEqualTo("Mismo Nombre");
        org.mockito.Mockito.verify(historialCambiosRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void eliminarActividad_noEncontrada_falla() {
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actividadService.eliminarActividad(999L))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void obtenerActividadPorId_noEncontrada_falla() {
        when(actividadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actividadService.obtenerActividadPorId(999L))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void listarActividadesPorProceso_noEncontrado_falla() {
        when(procesoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> actividadService.listarActividadesPorProceso(999L))
                .isInstanceOf(com.grupo1.editorprocesos.exception.ResourceNotFoundException.class);
    }

    @Test
    void validarUsuarioPertenecAEmpresa_sinEmpresa_falla() {
        Usuario usuarioSinEmpresa = new Usuario();
        usuarioSinEmpresa.setId(100L);
        usuarioSinEmpresa.setEmail("sinempresa@demo.com");

        when(httpServletRequest.getHeader("X-User-Email")).thenReturn(usuarioSinEmpresa.getEmail());
        when(usuarioRepository.findByEmail(usuarioSinEmpresa.getEmail())).thenReturn(Optional.of(usuarioSinEmpresa));
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        ActividadDTO dto = new ActividadDTO();
        dto.setProcesoId(proceso.getId());

        assertThatThrownBy(() -> actividadService.crearActividad(dto))
                .isInstanceOf(UnauthorizedException.class);
    }
}
