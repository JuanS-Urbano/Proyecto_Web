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
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.EmpresaService;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.PoolServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class PoolServiceImplTest {

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private ProcesoRepository procesoRepository;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PoolServiceImpl poolService;

    private Empresa empresa;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("admin@empresa.com");
        usuario.setEmpresa(empresa);
        usuario.setRolSistema(RolSistema.ADMIN_EMPRESA);

        org.mockito.Mockito.lenient().when(httpServletRequest.getHeader("X-User-Email")).thenReturn("admin@empresa.com");
        org.mockito.Mockito.lenient().when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(usuario));
    }

    @Test
    void crearPool_exitoso() {
        PoolDTO dto = new PoolDTO();
        dto.setNombre("Pool Test");
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(1L, null));

        Pool pool = new Pool();
        pool.setId(5L);
        pool.setNombre("Pool Test");
        pool.setEmpresa(empresa);

        PoolDTO resultado = new PoolDTO();
        resultado.setNombre("Pool Test");

        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);
        when(poolRepository.save(any(Pool.class))).thenReturn(pool);
        when(modelMapper.map(pool, PoolDTO.class)).thenReturn(resultado);

        PoolDTO result = poolService.crearPool(dto);

        assertThat(result.getNombre()).isEqualTo("Pool Test");
        assertThat(result.getEmpresa().getId()).isEqualTo(1L);
    }

    @Test
    void crearPool_empresaNoEncontrada() {
        PoolDTO dto = new PoolDTO();
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(99L, null));

        when(empresaService.obtenerEntityById(99L)).thenThrow(new com.grupo1.editorprocesos.exception.ResourceNotFoundException("Empresa no encontrada"));

        assertThatThrownBy(() -> poolService.crearPool(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearPool_sinPermisos() {
        usuario.setRolSistema(RolSistema.LECTOR);

        PoolDTO dto = new PoolDTO();
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(1L, null));

        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);

        assertThatThrownBy(() -> poolService.crearPool(dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("administradores");
    }

    @Test
    void listarPoolsPorEmpresa_exitoso() {
        Pool pool1 = new Pool();
        pool1.setId(1L);
        pool1.setEmpresa(empresa);

        PoolDTO dto1 = new PoolDTO();

        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);
        when(poolRepository.findByEmpresaId(1L)).thenReturn(List.of(pool1));
        when(modelMapper.map(pool1, PoolDTO.class)).thenReturn(dto1);

        List<PoolDTO> result = poolService.listarPoolsPorEmpresa(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void editarPool_exitoso() {
        Pool pool = new Pool();
        pool.setId(5L);
        pool.setNombre("Viejo");
        pool.setEmpresa(empresa);

        PoolDTO dto = new PoolDTO();
        dto.setNombre("Nuevo");

        Pool actualizado = new Pool();
        actualizado.setId(5L);
        actualizado.setNombre("Nuevo");
        actualizado.setEmpresa(empresa);

        PoolDTO resultado = new PoolDTO();
        resultado.setNombre("Nuevo");

        when(poolRepository.findById(5L)).thenReturn(Optional.of(pool));
        when(poolRepository.save(any(Pool.class))).thenReturn(actualizado);
        when(modelMapper.map(actualizado, PoolDTO.class)).thenReturn(resultado);

        PoolDTO result = poolService.editarPool(5L, dto);

        assertThat(result.getNombre()).isEqualTo("Nuevo");
    }

    @Test
    void editarPool_sinPermisos() {
        usuario.setRolSistema(RolSistema.LECTOR);

        Pool pool = new Pool();
        pool.setId(5L);
        pool.setEmpresa(empresa);

        when(poolRepository.findById(5L)).thenReturn(Optional.of(pool));

        PoolDTO dto = new PoolDTO();
        dto.setNombre("Nuevo");

        assertThatThrownBy(() -> poolService.editarPool(5L, dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void editarPool_noEncontrado() {
        when(poolRepository.findById(99L)).thenReturn(Optional.empty());

        PoolDTO dto = new PoolDTO();
        dto.setNombre("Test");

        assertThatThrownBy(() -> poolService.editarPool(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listarPoolsPorEmpresa_empresaNoEncontrada() {
        when(empresaService.obtenerEntityById(99L)).thenThrow(new com.grupo1.editorprocesos.exception.ResourceNotFoundException("Empresa no encontrada"));

        assertThatThrownBy(() -> poolService.listarPoolsPorEmpresa(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearPool_conAdminPlataforma_exitoso() {
        usuario.setRolSistema(RolSistema.ADMIN_PLATAFORMA);

        PoolDTO dto = new PoolDTO();
        dto.setNombre("Pool Admin");
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(1L, null));

        Pool pool = new Pool();
        pool.setId(10L);
        pool.setNombre("Pool Admin");
        pool.setEmpresa(empresa);

        PoolDTO resultado = new PoolDTO();
        resultado.setNombre("Pool Admin");

        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);
        when(poolRepository.save(any(Pool.class))).thenReturn(pool);
        when(modelMapper.map(pool, PoolDTO.class)).thenReturn(resultado);

        PoolDTO result = poolService.crearPool(dto);

        assertThat(result.getNombre()).isEqualTo("Pool Admin");
    }

    @Test
    void crearPool_usuarioOtraEmpresa() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        usuario.setEmpresa(otraEmpresa);

        PoolDTO dto = new PoolDTO();
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(1L, null));

        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);

        assertThatThrownBy(() -> poolService.crearPool(dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("no tiene acceso");
    }
}

