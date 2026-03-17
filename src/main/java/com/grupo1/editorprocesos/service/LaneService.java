package com.grupo1.editorprocesos.service;

import java.util.List;

import com.grupo1.editorprocesos.dto.LaneDTO;

public interface LaneService {

    /**
     * Crea un lane asociado a un proceso. Valida existencia del proceso y permisos.
     */
    LaneDTO crearLane(Long procesoId, LaneDTO laneDTO);

    /**
     * Obtiene la lista de lanes de un proceso.
     */
    List<LaneDTO> listarLanesPorProceso(Long procesoId);

    /**
     * Obtiene un lane por su id.
     */
    LaneDTO obtenerLanePorId(Long laneId);

    /**
     * Devuelve la entidad Lane. Útil para validaciones internas.
     */
    com.grupo1.editorprocesos.model.entity.process.Lane obtenerLaneEntityById(Long laneId);

    /**
     * Valida que un lane exista y pertenezca al proceso dado.
     */
    void validarLanePerteneceAlProceso(Long laneId, Long procesoId);

    /**
     * Edita un lane existente. Registra los cambios aplicados.
     */
    LaneDTO editarLane(Long laneId, LaneDTO laneDTO);

    /**
     * Elimina un lane. Valida que no tenga actividades asignadas antes de eliminar.
     */
    void eliminarLane(Long laneId);
}
