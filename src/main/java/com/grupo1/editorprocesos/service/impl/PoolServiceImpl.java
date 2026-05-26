package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.EmpresaService;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.service.PoolService;
import com.grupo1.editorprocesos.service.UsuarioActualService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private static final String POOL_NO_ENCONTRADO_CON_ID = "Pool no encontrado con id: ";

    private final PoolRepository poolRepository;
    private final ProcesoRepository procesoRepository;
    private final EmpresaService empresaService;
    private final ModelMapper modelMapper;
    private final UsuarioActualService usuarioActualService;

    @Override
    @Transactional
    public PoolDTO crearPool(PoolDTO poolDTO) {
        if (poolDTO.getEmpresa() == null || poolDTO.getEmpresa().getId() == null) {
            throw new IllegalArgumentException("La referencia a la empresa es requerida");
        }
        Empresa empresa = empresaService.obtenerEntityById(poolDTO.getEmpresa().getId());

        // Validar que el usuario actual tenga rol ADMIN_EMPRESA
        Usuario usuario = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuario, empresa);
        if (usuario.getRolSistema() != RolSistema.ADMIN_EMPRESA
                && usuario.getRolSistema() != RolSistema.ADMIN_PLATAFORMA) {
            throw new UnauthorizedException("Solo administradores pueden crear pools");
        }

        Pool pool = new Pool();
        pool.setNombre(poolDTO.getNombre());
        pool.setEmpresa(empresa);

        Pool guardado = poolRepository.save(pool);

        PoolDTO resultado = modelMapper.map(guardado, PoolDTO.class);
        resultado.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(guardado.getEmpresa().getId(), guardado.getEmpresa().getNombre()));
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoolDTO> listarPoolsPorEmpresa(Long empresaId) {
        Empresa empresa = empresaService.obtenerEntityById(empresaId);

        Usuario usuario = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuario, empresa);

        return poolRepository.findByEmpresaId(empresaId).stream()
                .map(pool -> {
                    PoolDTO dto = modelMapper.map(pool, PoolDTO.class);
                    dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(empresa.getId(), empresa.getNombre()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public PoolDTO editarPool(Long id, PoolDTO poolDTO) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                POOL_NO_ENCONTRADO_CON_ID + id));

        Usuario usuario = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuario, pool.getEmpresa());
        if (usuario.getRolSistema() != RolSistema.ADMIN_EMPRESA
                && usuario.getRolSistema() != RolSistema.ADMIN_PLATAFORMA) {
            throw new UnauthorizedException("Solo administradores pueden editar pools");
        }

        if (poolDTO.getNombre() != null) {
            pool.setNombre(poolDTO.getNombre());
        }

        Pool actualizado = poolRepository.save(pool);

        PoolDTO resultado = modelMapper.map(actualizado, PoolDTO.class);
        resultado.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(actualizado.getEmpresa().getId(), actualizado.getEmpresa().getNombre()));
        return resultado;
    }

    @Override
    @Transactional
    public void eliminarPool(Long id) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                POOL_NO_ENCONTRADO_CON_ID + id));

        Usuario usuario = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuario, pool.getEmpresa());
        if (usuario.getRolSistema() != RolSistema.ADMIN_EMPRESA
                && usuario.getRolSistema() != RolSistema.ADMIN_PLATAFORMA) {
            throw new UnauthorizedException("Solo administradores pueden eliminar pools");
        }

        // Verificar que no haya procesos activos en el pool
        if (!procesoRepository.findByPoolId(id).isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el pool porque tiene procesos asignados");
        }

        poolRepository.delete(pool);
    }

    // ===== Métodos privados de utilidad =====



    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PoolDTO obtenerPoolPorId(Long id) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO_CON_ID + id));
        PoolDTO resultado = modelMapper.map(pool, PoolDTO.class);
        resultado.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(pool.getEmpresa().getId(), pool.getEmpresa().getNombre()));
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Pool obtenerEntityById(Long id) {
        return poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO_CON_ID + id));
    }
}
