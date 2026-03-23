package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.PoolDTO;

import java.util.List;

public interface PoolService {
    PoolDTO crearPool(PoolDTO poolDTO);

    List<PoolDTO> listarPoolsPorEmpresa(Long empresaId);

    PoolDTO editarPool(Long id, PoolDTO poolDTO);

    void eliminarPool(Long id);
}
