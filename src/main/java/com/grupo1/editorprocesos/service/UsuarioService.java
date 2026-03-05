package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.UsuarioCreateDTO;
import com.grupo1.editorprocesos.dto.UsuarioDTO;

public interface UsuarioService {

    /**
     * Crea (o invita) un usuario asociado a una empresa.
     *
     * @param usuarioCreate datos básicos del usuario a crear
     * @return información del usuario creado, incluyendo id y rol asignado
     */
    UsuarioDTO crearUsuario(UsuarioCreateDTO usuarioCreate);

    /**
     * Crea un usuario administrador inicial para una empresa recién creada.
     *
     * @param empresa       La empresa a la que pertenece el administrador
     * @param emailContacto El correo que se usará como inicio de sesión
     */
    void crearAdminInicial(com.grupo1.editorprocesos.model.entity.core.Empresa empresa, String emailContacto);
}
