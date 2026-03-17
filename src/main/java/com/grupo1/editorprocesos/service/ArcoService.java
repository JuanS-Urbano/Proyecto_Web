package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.ArcoDTO;

import java.util.List;

public interface ArcoService {

    /**
     * Crea un nuevo arco dentro de un proceso.
     * Valida que los elementos origen y destino existan dentro del mismo proceso.
     */
    ArcoDTO crearArco(ArcoDTO arcoDTO);

    /**
     * Edita un arco existente.
     */
    ArcoDTO editarArco(Long id, ArcoDTO arcoDTO);

    /**
     * Obtiene un arco por su ID.
     */
    ArcoDTO obtenerArcoPorId(Long id);

    /**
     * Lista todos los arcos de un proceso.
     */
    List<ArcoDTO> listarArcosPorProceso(Long procesoId);

    /**
     * Elimina un arco por su ID.
     */
    void eliminarArco(Long id);
}