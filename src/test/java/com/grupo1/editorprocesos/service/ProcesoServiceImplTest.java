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
import org.modelmapper.ModelMapper;

import com.grupo1.editorprocesos.dto.ProcesoDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.service.impl.ProcesoServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ProcesoServiceImplTest {

    @Mock
    private ProcesoRepository procesoRepository;

    @Mock
    private com.grupo1.editorprocesos.service.PermisosPoolService permisosPoolService;

    @Mock
    private PoolService poolService;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private com.grupo1.editorprocesos.service.UsuarioActualService usuarioActualService;

    @Mock
    private HistorialCambiosRepository historialCambiosRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ProcesoServiceImpl procesoService;

    private Empresa empresa;
    private Usuario usuario;
    private Pool pool;
    private Proceso proceso;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("test@empresa.com");
        usuario.setEmpresa(empresa);
        usuario.setRolSistema(RolSistema.EDITOR);

        pool = new Pool();
        pool.setId(2L);
        pool.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(5L);
        proceso.setNombre("Proceso Test");
        proceso.setPool(pool);
        proceso.setEstado(EstadoProceso.BORRADOR);

        org.mockito.Mockito.lenient().when(usuarioActualService.obtenerUsuarioActual()).thenReturn(usuario);
    }

    @Test
    void crearProceso_exitoso() {
        ProcesoDTO request = new ProcesoDTO();
        request.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(2L, null));
        request.setNombre("Proceso Test");

        ProcesoDTO resultadoDTO = new ProcesoDTO();
        resultadoDTO.setNombre("Proceso Test");

        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.save(any(Proceso.class))).thenReturn(proceso);
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(resultadoDTO);

        ProcesoDTO result = procesoService.crearProceso(request);

        assertThat(result.getNombre()).isEqualTo("Proceso Test");
        assertThat(result.getPool().getId()).isEqualTo(2L);
        assertThat(result.getEmpresa().getId()).isEqualTo(1L);
    }

    @Test
    void crearProceso_poolSinEmpresa() {
        pool.setEmpresa(null);
        
        ProcesoDTO request = new ProcesoDTO();
        request.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(2L, null));

        when(poolService.obtenerEntityById(2L)).thenReturn(pool);

        assertThatThrownBy(() -> procesoService.crearProceso(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Empresa no encontrada");
    }

    @Test
    void crearProceso_usuarioOtraEmpresa() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        usuario.setEmpresa(otraEmpresa);

        ProcesoDTO request = new ProcesoDTO();
        request.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(2L, null));
        request.setNombre("Proceso Test");

        when(poolService.obtenerEntityById(2L)).thenReturn(pool);

        assertThatThrownBy(() -> procesoService.crearProceso(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void obtenerProcesoById_exitoso() {
        ProcesoDTO dto = new ProcesoDTO();
        when(procesoRepository.findById(5L)).thenReturn(Optional.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        ProcesoDTO result = procesoService.obtenerProcesoById(5L);

        assertThat(result).isNotNull();
    }

    @Test
    void editarProceso_conCambios_registraHistorial() {
        ProcesoDTO request = new ProcesoDTO();
        request.setNombre("Nuevo Nombre");
        request.setEstado(EstadoProceso.PUBLICADO);

        Proceso procesoActualizado = new Proceso();
        procesoActualizado.setId(5L);
        procesoActualizado.setNombre("Nuevo Nombre");
        procesoActualizado.setEstado(EstadoProceso.PUBLICADO);
        procesoActualizado.setPool(pool);

        ProcesoDTO resultadoDTO = new ProcesoDTO();
        resultadoDTO.setNombre("Nuevo Nombre");

        when(procesoRepository.findById(5L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.save(proceso)).thenReturn(procesoActualizado);
        when(modelMapper.map(procesoActualizado, ProcesoDTO.class)).thenReturn(resultadoDTO);

        ProcesoDTO result = procesoService.editarProceso(5L, request);

        assertThat(result.getNombre()).isEqualTo("Nuevo Nombre");
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void eliminarProceso_cambiaEstadoAInactivo_registraHistorial() {
        when(procesoRepository.findById(5L)).thenReturn(Optional.of(proceso));

        procesoService.eliminarProceso(5L);

        assertThat(proceso.getEstado()).isEqualTo(EstadoProceso.INACTIVO);
        verify(procesoRepository).save(proceso);
        verify(historialCambiosRepository).save(any(HistorialCambios.class));
    }

    @Test
    void listarProcesosPorPoolYEstado_exitoso() {
        ProcesoDTO dto = new ProcesoDTO();
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPoolConFiltros(2L, EstadoProceso.BORRADOR, null, false))
            .thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPoolYEstado(2L, EstadoProceso.BORRADOR);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarProcesosPorEmpresa_exitoso() {
        ProcesoDTO dto = new ProcesoDTO();
        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);
        when(procesoRepository.buscarVisiblesPorEmpresa(1L, false)).thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorEmpresa(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarProcesosPorPool_exitoso() {
        ProcesoDTO dto = new ProcesoDTO();
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPool(2L, false)).thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPool(2L);

        assertThat(result).hasSize(1);
    }

    @Test
    void listarProcesosPorPoolYCategoria_exitoso() {
        ProcesoDTO dto = new ProcesoDTO();
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPoolConFiltros(2L, null, "RRHH", false))
            .thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPoolYCategoria(2L, "RRHH");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarProcesosPorPoolConFiltros_conAmbosFiltros() {
        ProcesoDTO dto = new ProcesoDTO();
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPoolConFiltros(2L, EstadoProceso.BORRADOR, "RRHH", false))
            .thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPoolConFiltros(2L, EstadoProceso.BORRADOR, "RRHH");

        assertThat(result).hasSize(1);
    }

    @Test
    void listarProcesosPorPoolConFiltros_sinFiltros() {
        ProcesoDTO dto = new ProcesoDTO();
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPoolConFiltros(2L, null, null, false)).thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPoolConFiltros(2L, null, null);

        assertThat(result).hasSize(1);
        verify(procesoRepository).buscarVisiblesPorPoolConFiltros(2L, null, null, false);
    }

    @Test
    void buscarProcesosPorNombre_exitoso() {
        ProcesoDTO dto = new ProcesoDTO();
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPoolYNombre(2L, "Test", false)).thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.buscarProcesosPorNombre(2L, "Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void obtenerHistorialProceso_exitoso() {
        com.grupo1.editorprocesos.dto.HistorialCambiosDTO hDto = new com.grupo1.editorprocesos.dto.HistorialCambiosDTO();
        HistorialCambios historial = new HistorialCambios();
        
        when(procesoRepository.findById(5L)).thenReturn(Optional.of(proceso));
        when(historialCambiosRepository.findByProcesoId(5L)).thenReturn(List.of(historial));
        when(modelMapper.map(historial, com.grupo1.editorprocesos.dto.HistorialCambiosDTO.class)).thenReturn(hDto);

        List<com.grupo1.editorprocesos.dto.HistorialCambiosDTO> result = procesoService.obtenerHistorialProceso(5L);

        assertThat(result).hasSize(1);
    }

    @Test
    void editarProceso_sinCambios_noRegistraHistorial() {
        ProcesoDTO request = new ProcesoDTO();
        // Request vacio, sin cambios efectivos
        
        when(procesoRepository.findById(5L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.save(proceso)).thenReturn(proceso);
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(new ProcesoDTO());

        procesoService.editarProceso(5L, request);

        // Verifica que no se llamó a guardar historial
        verify(historialCambiosRepository, org.mockito.Mockito.never()).save(any(HistorialCambios.class));
    }

    @Test
    void crearProceso_poolIdNull() {
        ProcesoDTO request = new ProcesoDTO();
        request.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(null, null));

        assertThatThrownBy(() -> procesoService.crearProceso(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerProcesoById_noEncontrado() {
        when(procesoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> procesoService.obtenerProcesoById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void validarUsuarioPertenecAEmpresa_usuarioSinEmpresa() {
        usuario.setEmpresa(null);
        
        ProcesoDTO dto = new ProcesoDTO();
        dto.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(2L, null));
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);

        assertThatThrownBy(() -> procesoService.crearProceso(dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("El usuario no tiene acceso a la empresa");
    }

    @Test
    void obtenerUsuarioActual_headerVacio() {
        when(usuarioActualService.obtenerUsuarioActual()).thenThrow(new UnauthorizedException("No se proporcionó el header X-User-Email para identificar al usuario"));
        
        ProcesoDTO dto = new ProcesoDTO();
        dto.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(2L, null));
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);

        assertThatThrownBy(() -> procesoService.crearProceso(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void obtenerUsuarioActual_usuarioNoEncontrado() {
        when(usuarioActualService.obtenerUsuarioActual()).thenThrow(new UnauthorizedException("Usuario no encontrado con el email: noexiste@empresa.com"));

        ProcesoDTO dto = new ProcesoDTO();
        dto.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(2L, null));
        when(poolService.obtenerEntityById(2L)).thenReturn(pool);

        assertThatThrownBy(() -> procesoService.crearProceso(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void listarProcesosPorPool_conPermisoCompartidosEnHeader() {
        ProcesoDTO dto = new ProcesoDTO();
        org.mockito.Mockito.when(httpServletRequest.getHeader("X-User-Permissions"))
                .thenReturn("PROCESO_COMPARTIDO_VER");

        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPool(2L, true)).thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPool(2L);

        assertThat(result).hasSize(1);
        verify(procesoRepository).buscarVisiblesPorPool(2L, true);
    }

    @Test
    void listarProcesosPorPool_adminPlataformaIncluyeCompartidos() {
        ProcesoDTO dto = new ProcesoDTO();
        usuario.setRolSistema(RolSistema.ADMIN_PLATAFORMA);

        when(poolService.obtenerEntityById(2L)).thenReturn(pool);
        when(procesoRepository.buscarVisiblesPorPool(2L, true)).thenReturn(List.of(proceso));
        when(modelMapper.map(proceso, ProcesoDTO.class)).thenReturn(dto);

        List<ProcesoDTO> result = procesoService.listarProcesosPorPool(2L);

        assertThat(result).hasSize(1);
        verify(procesoRepository).buscarVisiblesPorPool(2L, true);
    }
}
