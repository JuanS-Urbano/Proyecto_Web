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

import com.grupo1.editorprocesos.dto.RolProcesoDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.service.EmpresaService;
import com.grupo1.editorprocesos.repository.RolProcesoRepository;
import com.grupo1.editorprocesos.service.impl.RolProcesoServiceImpl;

@ExtendWith(MockitoExtension.class)
class RolProcesoServiceImplTest {

    @Mock
    private RolProcesoRepository rolProcesoRepository;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RolProcesoServiceImpl rolProcesoService;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
    }

    @Test
    void crearRol_exitoso() {
        RolProcesoDTO dto = new RolProcesoDTO();
        dto.setNombre("Analista");
        dto.setDescripcion("Analista de procesos");
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(1L, null));

        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Analista");
        rol.setEmpresa(empresa);

        RolProcesoDTO resultado = new RolProcesoDTO();
        resultado.setNombre("Analista");

        when(empresaService.obtenerEntityById(1L)).thenReturn(empresa);
        when(rolProcesoRepository.save(any(RolProceso.class))).thenReturn(rol);
        when(modelMapper.map(rol, RolProcesoDTO.class)).thenReturn(resultado);

        RolProcesoDTO result = rolProcesoService.crearRol(dto);

        assertThat(result.getNombre()).isEqualTo("Analista");
        assertThat(result.getEmpresa().getId()).isEqualTo(1L);
    }

    @Test
    void crearRol_empresaNoEncontrada() {
        RolProcesoDTO dto = new RolProcesoDTO();
        dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(99L, null));

        when(empresaService.obtenerEntityById(99L)).thenThrow(new com.grupo1.editorprocesos.exception.ResourceNotFoundException("Empresa no encontrada"));

        assertThatThrownBy(() -> rolProcesoService.crearRol(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void editarRol_exitoso() {
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Viejo");
        rol.setEmpresa(empresa);

        RolProcesoDTO dto = new RolProcesoDTO();
        dto.setNombre("Nuevo");
        dto.setDescripcion("Nueva descripción");

        RolProceso actualizado = new RolProceso();
        actualizado.setId(5L);
        actualizado.setNombre("Nuevo");
        actualizado.setEmpresa(empresa);

        RolProcesoDTO resultado = new RolProcesoDTO();
        resultado.setNombre("Nuevo");

        when(rolProcesoRepository.findById(5L)).thenReturn(Optional.of(rol));
        when(rolProcesoRepository.save(any(RolProceso.class))).thenReturn(actualizado);
        when(modelMapper.map(actualizado, RolProcesoDTO.class)).thenReturn(resultado);

        RolProcesoDTO result = rolProcesoService.editarRol(5L, dto);

        assertThat(result.getNombre()).isEqualTo("Nuevo");
    }

    @Test
    void eliminarRol_exitoso() {
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Analista");
        rol.setEmpresa(empresa);

        when(rolProcesoRepository.findById(5L)).thenReturn(Optional.of(rol));
        when(rolProcesoRepository.estaEnUso(5L)).thenReturn(false);
        when(actividadRepository.existeActividadConRol(5L)).thenReturn(false);

        rolProcesoService.eliminarRol(5L);

        verify(rolProcesoRepository).delete(rol);
    }

    @Test
    void eliminarRol_enUso_falla() {
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Analista");
        rol.setEmpresa(empresa);

        when(rolProcesoRepository.findById(5L)).thenReturn(Optional.of(rol));
        when(rolProcesoRepository.estaEnUso(5L)).thenReturn(true);

        assertThatThrownBy(() -> rolProcesoService.eliminarRol(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lanes");
    }

    @Test
    void eliminarRol_usadoPorActividades_falla() {
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setNombre("Analista");
        rol.setEmpresa(empresa);

        when(rolProcesoRepository.findById(5L)).thenReturn(Optional.of(rol));
        when(rolProcesoRepository.estaEnUso(5L)).thenReturn(false);
        when(actividadRepository.existeActividadConRol(5L)).thenReturn(true);

        assertThatThrownBy(() -> rolProcesoService.eliminarRol(5L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("actividades");
    }

    @Test
    void listarPorEmpresa_exitoso() {
        RolProceso rol = new RolProceso();
        rol.setId(1L);
        rol.setEmpresa(empresa);

        RolProcesoDTO dto = new RolProcesoDTO();

        when(rolProcesoRepository.findByEmpresaId(1L)).thenReturn(List.of(rol));
        when(modelMapper.map(rol, RolProcesoDTO.class)).thenReturn(dto);

        List<RolProcesoDTO> result = rolProcesoService.listarPorEmpresa(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void obtenerPorId_exitoso() {
        RolProceso rol = new RolProceso();
        rol.setId(5L);
        rol.setEmpresa(empresa);

        RolProcesoDTO dto = new RolProcesoDTO();

        when(rolProcesoRepository.findById(5L)).thenReturn(Optional.of(rol));
        when(modelMapper.map(rol, RolProcesoDTO.class)).thenReturn(dto);

        RolProcesoDTO result = rolProcesoService.obtenerPorId(5L);

        assertThat(result).isNotNull();
    }

    @Test
    void obtenerPorId_noEncontrado() {
        when(rolProcesoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolProcesoService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
