package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.EmpresaDTO;
import java.util.List;

public interface EmpresaService {
    EmpresaDTO crearEmpresa(EmpresaDTO empresaDTO);

    List<EmpresaDTO> listarEmpresas();

    EmpresaDTO obtenerEmpresaPorId(Long id);
}
