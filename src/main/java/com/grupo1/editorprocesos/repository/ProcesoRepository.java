package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {
}
