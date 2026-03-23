package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.model.entity.core.Usuario;

/**
 * HU-24: Servicio centralizado de validación de permisos por Pool.
 * Refuerza que solo EDITORES y ADMIN puedan modificar recursos,
 * mientras que LECTORs solo pueden consultar.
 */
public interface PermisosPoolService {

    /**
     * Valida que el usuario tenga permiso de escritura (crear/editar/eliminar).
     * Lanza UnauthorizedException si el usuario es LECTOR.
     */
    void validarPermisoEscritura(Usuario usuario);

    /**
     * Valida que el usuario tenga permiso de lectura.
     * Actualmente todos los roles tienen lectura.
     */
    void validarPermisoLectura(Usuario usuario);

    /**
     * Retorna true si el usuario puede realizar operaciones de escritura.
     */
    boolean tienePermisoEscritura(Usuario usuario);
}
