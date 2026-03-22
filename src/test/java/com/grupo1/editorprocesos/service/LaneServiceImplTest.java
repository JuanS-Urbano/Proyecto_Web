package com.grupo1.editorprocesos.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo1.editorprocesos.dto.LaneDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.Lane;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import com.grupo1.editorprocesos.repository.LaneRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.RolProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.LaneServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class LaneServiceImplTest {

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private ProcesoRepository procesoRepository;

    @Mock
    private RolProcesoRepository rolProcesoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private LaneServiceImpl laneService;

    private Empresa empresa;
    private Pool pool;
    private Proceso proceso;
    private Usuario usuario;

    @BeforeEach
    void setup() {
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
        usuario.setEmail("user@company.com");
        usuario.setEmpresa(empresa);

        // Estos stubbings se aplican en la mayoría de los tests, pero algunos tests
        // terminan antes de necesitarlos, por eso se definen como lenient.
        org.mockito.Mockito.lenient().when(httpServletRequest.getHeader("X-User-Email")).thenReturn(usuario.getEmail());
        org.mockito.Mockito.lenient().when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    @Test
    void crearLane_exitoso() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        RolProceso rolProceso = new RolProceso();
        rolProceso.setId(5L);
        rolProceso.setEmpresa(empresa);
        when(rolProcesoRepository.findById(rolProceso.getId())).thenReturn(Optional.of(rolProceso));

        Lane laneGuardado = new Lane();
        laneGuardado.setId(123L);
        laneGuardado.setNombre("Mi lane");
        laneGuardado.setRolProceso(rolProceso);
        when(laneRepository.save(any())).thenReturn(laneGuardado);

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");
        request.setRolProcesoId(rolProceso.getId());

        LaneDTO result = laneService.crearLane(proceso.getId(), request);

        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getNombre()).isEqualTo("Mi lane");
        assertThat(result.getRolProcesoId()).isEqualTo(rolProceso.getId());

        ArgumentCaptor<Lane> captor = ArgumentCaptor.forClass(Lane.class);
        verify(laneRepository).save(captor.capture());
        assertThat(captor.getValue().getProceso().getId()).isEqualTo(proceso.getId());
    }

    @Test
    void crearLane_errorProcesoNoExiste() {
        Long procesoId = proceso.getId();
        when(procesoRepository.findById(procesoId)).thenReturn(Optional.empty());

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");

        assertThatThrownBy(() -> laneService.crearLane(procesoId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proceso no encontrado");
    }

    @Test
    void crearLane_errorNombreVacio() {
        Long procesoId = proceso.getId();
        when(procesoRepository.findById(procesoId)).thenReturn(Optional.of(proceso));

        LaneDTO request = new LaneDTO();
        request.setNombre("  ");

        assertThatThrownBy(() -> laneService.crearLane(procesoId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del lane");
    }

    @Test
    void crearLane_errorRolProcesoNoExiste() {
        Long procesoId = proceso.getId();
        when(procesoRepository.findById(procesoId)).thenReturn(Optional.of(proceso));
        when(rolProcesoRepository.findById(7L)).thenReturn(Optional.empty());

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");
        request.setRolProcesoId(7L);

        assertThatThrownBy(() -> laneService.crearLane(procesoId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("RolProceso no encontrado");
    }

    @Test
    void crearLane_errorRolProcesoEmpresaDiferente() {
        Long procesoId = proceso.getId();
        when(procesoRepository.findById(procesoId)).thenReturn(Optional.of(proceso));

        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);

        RolProceso rolProceso = new RolProceso();
        rolProceso.setId(5L);
        rolProceso.setEmpresa(otraEmpresa);
        when(rolProcesoRepository.findById(rolProceso.getId())).thenReturn(Optional.of(rolProceso));

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");
        request.setRolProcesoId(rolProceso.getId());

        assertThatThrownBy(() -> laneService.crearLane(procesoId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RolProceso no pertenece");
    }

    @Test
    void listarLanesPorProceso_exitoso() {
        when(procesoRepository.findById(10L)).thenReturn(Optional.of(proceso));
        Lane lane = new Lane();
        lane.setId(1L);
        when(laneRepository.findByProcesoId(10L)).thenReturn(java.util.List.of(lane));

        java.util.List<LaneDTO> result = laneService.listarLanesPorProceso(10L);

        assertThat(result).hasSize(1);
    }

    @Test
    void obtenerLanePorId_exitoso() {
        Lane lane = new Lane();
        lane.setId(1L);
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));

        LaneDTO result = laneService.obtenerLanePorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void validarLanePerteneceAlProceso_exitoso() {
        Lane lane = new Lane();
        lane.setId(1L);
        lane.setProceso(proceso);
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));

        // Verificar que no lanza excepciÃ³n
        org.assertj.core.api.Assertions.assertThatCode(() -> laneService.validarLanePerteneceAlProceso(1L, 10L))
            .doesNotThrowAnyException();
    }

    @Test
    void validarLanePerteneceAlProceso_fallaPorDiferenteProceso() {
        Lane lane = new Lane();
        lane.setId(1L);
        Proceso otroProceso = new Proceso();
        otroProceso.setId(99L);
        lane.setProceso(otroProceso);
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));

        assertThatThrownBy(() -> laneService.validarLanePerteneceAlProceso(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void editarLane_exitoso() {
        Lane lane = new Lane();
        lane.setId(1L);
        lane.setProceso(proceso);
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));
        when(laneRepository.save(any(Lane.class))).thenReturn(lane);

        LaneDTO request = new LaneDTO();
        request.setNombre("Nuevo Nombre");
        request.setOrden(2);
        request.setPosicionX(150.0);
        request.setPosicionY(150.0);

        LaneDTO result = laneService.editarLane(1L, request);

        assertThat(result).isNotNull();
        verify(laneRepository).save(any(Lane.class));
    }

    @Test
    void eliminarLane_exitoso() {
        Lane lane = new Lane();
        lane.setId(1L);
        lane.setProceso(proceso);
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));
        
        // Mock de dependencias
        com.grupo1.editorprocesos.repository.ActividadRepository actividadRepository =
                org.mockito.Mockito.mock(com.grupo1.editorprocesos.repository.ActividadRepository.class);
        when(actividadRepository.findByLaneId(1L)).thenReturn(java.util.Collections.emptyList());

        // Inyectamos esto
        laneService = new LaneServiceImpl(laneRepository, procesoRepository, rolProcesoRepository,
                usuarioRepository, actividadRepository, httpServletRequest);

        laneService.eliminarLane(1L);

        verify(laneRepository).delete(lane);
    }

    @Test
    void eliminarLane_fallaConActividades() {
        Lane lane = new Lane();
        lane.setId(1L);
        lane.setProceso(proceso);
        when(laneRepository.findById(1L)).thenReturn(Optional.of(lane));
        
        // Mock de dependencias
        com.grupo1.editorprocesos.repository.ActividadRepository actividadRepository =
                org.mockito.Mockito.mock(com.grupo1.editorprocesos.repository.ActividadRepository.class);
        com.grupo1.editorprocesos.model.entity.bpmn.Actividad actividad = new com.grupo1.editorprocesos.model.entity.bpmn.Actividad();
        when(actividadRepository.findByLaneId(1L)).thenReturn(java.util.List.of(actividad));

        // Inyectamos esto
        laneService = new LaneServiceImpl(laneRepository, procesoRepository, rolProcesoRepository,
                usuarioRepository, actividadRepository, httpServletRequest);

        assertThatThrownBy(() -> laneService.eliminarLane(1L))
                .isInstanceOf(IllegalStateException.class);
    }
}
