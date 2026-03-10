package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.UsuarioEmpresaDTO;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.UsuarioEmpresaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioEmpresaServiceImpl implements UsuarioEmpresaService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    public UsuarioEmpresaServiceImpl(UsuarioRepository usuarioRepository,
                                    EmpresaRepository empresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
    }

    @Override
    public List<Usuario> listarUsuariosPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresa_Id(empresaId);
    }

    @Override
    public void guardarUsuario(UsuarioEmpresaDTO dto) {
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo.");
        }

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada."));

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setRol(dto.getRol());
        usuario.setEmpresa(empresa);

        usuarioRepository.save(usuario);
    }

    @Override
    public Empresa obtenerEmpresaPorId(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada."));
    }
}