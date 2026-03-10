package com.grupo1.editorprocesos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.editorprocesos.model.entity.core.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByEmpresa_Id(Long empresaId);

    boolean existsByCorreo(String correo);
}