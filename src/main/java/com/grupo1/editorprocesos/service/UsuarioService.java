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
}
