package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.core.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Long> {
    List<Pool> findByEmpresaId(Long empresaId);
}
