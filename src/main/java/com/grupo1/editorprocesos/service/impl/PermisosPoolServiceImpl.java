package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.PermisosPoolService;
import org.springframework.stereotype.Service;

/**
 * HU-24: Implementación de validación de permisos por Pool.
 *
 * Escritura permitida: ADMIN_PLATAFORMA, ADMIN_EMPRESA, EDITOR
 * Solo lectura: LECTOR
 */
@Service
public class PermisosPoolServiceImpl implements PermisosPoolService {

    @Override
    public void validarPermisoEscritura(Usuario usuario) {
        if (!tienePermisoEscritura(usuario)) {
            throw new UnauthorizedException(
                    "El usuario con rol " + usuario.getRolSistema().name()
                            + " no tiene permisos de escritura. Solo EDITOR o ADMIN pueden realizar esta acción.");
        }
    }

    @Override
    public void validarPermisoLectura(Usuario usuario) {
        // Todos los roles tienen permiso de lectura
        if (usuario == null || usuario.getRolSistema() == null) {
            throw new UnauthorizedException("Usuario sin rol asignado");
        }
    }

    @Override
    public boolean tienePermisoEscritura(Usuario usuario) {
        if (usuario == null || usuario.getRolSistema() == null) {
            return false;
        }
        RolSistema rol = usuario.getRolSistema();
        return rol == RolSistema.ADMIN_PLATAFORMA
                || rol == RolSistema.ADMIN_EMPRESA
                || rol == RolSistema.EDITOR;
    }
}
