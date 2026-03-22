package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.MensajeDTO;

import java.util.List;

public interface MensajeService {

    /**
     * HU-25 (Dev 1): Envía un Message Throw. Valida JSON del payload si se proporciona.
     */
    MensajeDTO throwMessage(MensajeDTO dto);

    /**
     * Lista todos los mensajes de un proceso.
     */
    List<MensajeDTO> listarMensajesPorProceso(Long procesoId);

    /**
     * Obtiene un mensaje por su ID.
     */
    MensajeDTO obtenerMensajePorId(Long id);
}
