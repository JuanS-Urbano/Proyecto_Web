package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.RolProcesoDTO;

import java.util.List;

public interface RolProcesoService {
    RolProcesoDTO crearRol(RolProcesoDTO rolDTO);
    RolProcesoDTO editarRol(Long id, RolProcesoDTO rolDTO);
    void eliminarRol(Long id);
    List<RolProcesoDTO> listarPorEmpresa(Long empresaId);
    RolProcesoDTO obtenerPorId(Long id);
}
