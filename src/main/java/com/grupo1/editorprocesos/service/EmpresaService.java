package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.EmpresaDTO;
import java.util.List;

public interface EmpresaService {
    EmpresaDTO crearEmpresa(EmpresaDTO empresaDTO);

    List<EmpresaDTO> listarEmpresas();

    EmpresaDTO obtenerEmpresaPorId(Long id);

    /**
     * Devuelve la entidad Empresa. Método interno para ser usado por otros servicios.
     */
    com.grupo1.editorprocesos.model.entity.core.Empresa obtenerEntityById(Long id);
}
