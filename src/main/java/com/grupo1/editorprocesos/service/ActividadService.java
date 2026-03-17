package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.ActividadDTO;

import java.util.List;

public interface ActividadService {

    /**
     * Crea una nueva actividad dentro de un proceso.
     * Valida que el proceso exista y que el usuario actual pertenezca a la empresa.
     */
    ActividadDTO crearActividad(ActividadDTO actividadDTO);

    /**
     * Edita una actividad existente. Registra los cambios en el historial.
     */
    ActividadDTO editarActividad(Long id, ActividadDTO actividadDTO);

    /**
     * Obtiene una actividad por su ID.
     */
    ActividadDTO obtenerActividadPorId(Long id);

    /**
     * Lista todas las actividades de un proceso.
     */
    List<ActividadDTO> listarActividadesPorProceso(Long procesoId);

    /**
     * Elimina una actividad y maneja los arcos conectados.
     * Elimina todos los arcos donde la actividad es origen o destino.
     */
    void eliminarActividad(Long id);
}
