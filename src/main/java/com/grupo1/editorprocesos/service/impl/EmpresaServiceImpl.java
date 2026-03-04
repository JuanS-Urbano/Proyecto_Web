package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.EmpresaDTO;
import com.grupo1.editorprocesos.service.EmpresaService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Override
    public EmpresaDTO crearEmpresa(EmpresaDTO empresaDTO) {
        // Mock implementation
        empresaDTO.setId(1L);
        return empresaDTO;
    }

    @Override
    public List<EmpresaDTO> listarEmpresas() {
        return Collections.emptyList();
    }

    @Override
    public EmpresaDTO obtenerEmpresaPorId(Long id) {
        return new EmpresaDTO();
    }
}
