package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolProcesoRepository extends JpaRepository<RolProceso, Long> {
}
