package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.service.PoolService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PoolDTO crearPool(PoolDTO poolDTO) {
        Empresa empresa = empresaRepository.findById(poolDTO.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id: " + poolDTO.getEmpresaId()));

        // =====================================================================================
        // TODO (Dev 3): Validar que el usuario actual tenga rol ADMIN_EMPRESA
        // Usuario usuario = obtenerUsuarioActual();
        // if (usuario.getRolSistema() != RolSistema.ADMIN_EMPRESA) throw ...
        // =====================================================================================

        Pool pool = new Pool();
        pool.setNombre(poolDTO.getNombre());
        pool.setEmpresa(empresa);

        Pool guardado = poolRepository.save(pool);

        PoolDTO resultado = modelMapper.map(guardado, PoolDTO.class);
        resultado.setEmpresaId(guardado.getEmpresa().getId());
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoolDTO> listarPoolsPorEmpresa(Long empresaId) {
        empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id: " + empresaId));

        return poolRepository.findByEmpresaId(empresaId).stream()
                .map(pool -> {
                    PoolDTO dto = modelMapper.map(pool, PoolDTO.class);
                    dto.setEmpresaId(empresaId);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PoolDTO editarPool(Long id, PoolDTO poolDTO) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con id: " + id));

        if (poolDTO.getNombre() != null) {
            pool.setNombre(poolDTO.getNombre());
        }

        Pool actualizado = poolRepository.save(pool);

        PoolDTO resultado = modelMapper.map(actualizado, PoolDTO.class);
        resultado.setEmpresaId(actualizado.getEmpresa().getId());
        return resultado;
    }
}
