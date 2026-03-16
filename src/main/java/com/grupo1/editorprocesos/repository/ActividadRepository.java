package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    /**
     * Lista todas las actividades que pertenecen a un proceso.
     */
    List<Actividad> findByProcesoId(Long procesoId);

    /**
     * Lista todas las actividades asignadas a un lane específico.
     * TODO (Dev 2 — HU-22): Usado por LaneService para verificar qué actividades
     * están dentro de un lane antes de eliminarlo o modificarlo.
     */
    List<Actividad> findByLaneId(Long laneId);
}
