package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByProcesoId(Long procesoId);

    @Query("SELECT COUNT(a) > 0 FROM Actividad a WHERE a.lane.rolProceso.id = :rolId")
    boolean existeActividadConRol(@Param("rolId") Long rolId);
}
