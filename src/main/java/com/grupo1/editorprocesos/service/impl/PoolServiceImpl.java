package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.PoolDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.PoolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public PoolDTO crearPool(PoolDTO poolDTO) {
        Empresa empresa = empresaRepository.findById(poolDTO.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id: " + poolDTO.getEmpresaId()));

        // Validar que el usuario actual tenga rol ADMIN_EMPRESA
        Usuario usuario = obtenerUsuarioActual();
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
        resultado.setEmpresaId(guardado.getEmpresa().getId());
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoolDTO> listarPoolsPorEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id: " + empresaId));

        Usuario usuario = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuario, empresa);

        return poolRepository.findByEmpresaId(empresaId).stream()
                .map(pool -> {
                    PoolDTO dto = modelMapper.map(pool, PoolDTO.class);
                    dto.setEmpresaId(empresaId);
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public PoolDTO editarPool(Long id, PoolDTO poolDTO) {
        Pool pool = poolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con id: " + id));

        Usuario usuario = obtenerUsuarioActual();
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
        resultado.setEmpresaId(actualizado.getEmpresa().getId());
        return resultado;
    }

    // ===== Métodos privados de utilidad =====

    private Usuario obtenerUsuarioActual() {
        String email = httpServletRequest.getHeader("X-User-Email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("No se proporcionó el header X-User-Email para identificar al usuario");
        }
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Usuario no encontrado con el email: " + email));
    }

    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }
}
