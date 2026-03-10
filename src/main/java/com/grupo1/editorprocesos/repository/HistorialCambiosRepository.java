package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCambiosRepository extends JpaRepository<HistorialCambios, Long> {
    List<HistorialCambios> findByProcesoId(Long procesoId);
}
