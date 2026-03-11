package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {
    List<RolProceso> findByEmpresaId(Long empresaId);

    @Query("SELECT COUNT(l) > 0 FROM Lane l WHERE l.rolProceso.id = :rolId")
    boolean estaEnUso(@Param("rolId") Long rolId);
}

