package com.grupo1.editorprocesos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo1.editorprocesos.model.entity.core.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}