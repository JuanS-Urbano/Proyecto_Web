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
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.empty());

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");

        assertThatThrownBy(() -> laneService.crearLane(proceso.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proceso no encontrado");
    }

    @Test
    void crearLane_errorNombreVacio() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        LaneDTO request = new LaneDTO();
        request.setNombre("  ");

        assertThatThrownBy(() -> laneService.crearLane(proceso.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del lane");
    }

    @Test
    void crearLane_errorRolProcesoNoExiste() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));
        when(rolProcesoRepository.findById(7L)).thenReturn(Optional.empty());

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");
        request.setRolProcesoId(7L);

        assertThatThrownBy(() -> laneService.crearLane(proceso.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("RolProceso no encontrado");
    }

    @Test
    void crearLane_errorRolProcesoEmpresaDiferente() {
        when(procesoRepository.findById(proceso.getId())).thenReturn(Optional.of(proceso));

        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);

        RolProceso rolProceso = new RolProceso();
        rolProceso.setId(5L);
        rolProceso.setEmpresa(otraEmpresa);
        when(rolProcesoRepository.findById(rolProceso.getId())).thenReturn(Optional.of(rolProceso));

        LaneDTO request = new LaneDTO();
        request.setNombre("Mi lane");
        request.setRolProcesoId(rolProceso.getId());

        assertThatThrownBy(() -> laneService.crearLane(proceso.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RolProceso no pertenece");
    }
}
