package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.service.PoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public PoolDTO crearPool(PoolDTO poolDTO) {
        Empresa empresa = empresaRepository.findById(poolDTO.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id: " + poolDTO.getEmpresaId()));

        Pool pool = new Pool();
        pool.setNombre(poolDTO.getNombre());
        pool.setEmpresa(empresa);

        Pool guardado = poolRepository.save(pool);

        PoolDTO resultado = new PoolDTO();
        resultado.setId(guardado.getId());
        resultado.setNombre(guardado.getNombre());
        resultado.setEmpresaId(guardado.getEmpresa().getId());
        return resultado;
    }
}
