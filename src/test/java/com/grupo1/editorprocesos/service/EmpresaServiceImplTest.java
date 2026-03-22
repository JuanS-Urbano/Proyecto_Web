package com.grupo1.editorprocesos.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.grupo1.editorprocesos.dto.EmpresaDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.service.impl.EmpresaServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    @Test
    void crearEmpresa_exitoso() {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setNit("123456");
        dto.setCorreoContacto("admin@empresa.com");

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        EmpresaDTO resultado = new EmpresaDTO();
        resultado.setNit("123456");

        when(empresaRepository.existsByNit("123456")).thenReturn(false);
        when(modelMapper.map(dto, Empresa.class)).thenReturn(empresa);
        when(empresaRepository.save(empresa)).thenReturn(empresa);
        when(modelMapper.map(empresa, EmpresaDTO.class)).thenReturn(resultado);

        EmpresaDTO result = empresaService.crearEmpresa(dto);

        assertThat(result.getNit()).isEqualTo("123456");
        verify(usuarioService).crearAdminInicial(empresa, "admin@empresa.com");
    }

    @Test
    void crearEmpresa_nitDuplicado() {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setNit("123456");

        when(empresaRepository.existsByNit("123456")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.crearEmpresa(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("123456");
    }

    @Test
    void listarEmpresas_retornaLista() {
        Empresa empresa1 = new Empresa();
        empresa1.setId(1L);
        Empresa empresa2 = new Empresa();
        empresa2.setId(2L);

        EmpresaDTO dto1 = new EmpresaDTO();
        EmpresaDTO dto2 = new EmpresaDTO();

        when(empresaRepository.findAll()).thenReturn(List.of(empresa1, empresa2));
        when(modelMapper.map(empresa1, EmpresaDTO.class)).thenReturn(dto1);
        when(modelMapper.map(empresa2, EmpresaDTO.class)).thenReturn(dto2);

        List<EmpresaDTO> result = empresaService.listarEmpresas();

        assertThat(result).hasSize(2);
    }

    @Test
    void obtenerEmpresaPorId_exitoso() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        EmpresaDTO dto = new EmpresaDTO();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(modelMapper.map(empresa, EmpresaDTO.class)).thenReturn(dto);

        EmpresaDTO result = empresaService.obtenerEmpresaPorId(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void obtenerEmpresaPorId_noEncontrada() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.obtenerEmpresaPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
