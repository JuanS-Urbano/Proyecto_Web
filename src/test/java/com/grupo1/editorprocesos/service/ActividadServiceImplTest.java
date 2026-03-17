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
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.TipoActividad;
import com.grupo1.editorprocesos.repository.ActividadRepository;
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
    private ProcesoRepository procesoRepository;

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
}
