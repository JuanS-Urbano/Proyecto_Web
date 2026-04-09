package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.PoolDTO;

import java.util.List;

public interface PoolService {
    PoolDTO crearPool(PoolDTO poolDTO);

    List<PoolDTO> listarPoolsPorEmpresa(Long empresaId);

    PoolDTO editarPool(Long id, PoolDTO poolDTO);

    void eliminarPool(Long id);

    /**
     * Devuelve la entidad Pool. Método interno para ser usado por otros servicios.
     */
    com.grupo1.editorprocesos.model.entity.core.Pool obtenerEntityById(Long id);
}
