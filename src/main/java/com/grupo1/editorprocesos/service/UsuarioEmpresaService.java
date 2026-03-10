package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.UsuarioEmpresaDTO;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;

import java.util.List;

public interface UsuarioEmpresaService {

    List<Usuario> listarUsuariosPorEmpresa(Long empresaId);

    void guardarUsuario(UsuarioEmpresaDTO dto);

    Empresa obtenerEmpresaPorId(Long empresaId);
}