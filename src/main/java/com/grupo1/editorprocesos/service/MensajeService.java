package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.CorrelacionResultDTO;
import com.grupo1.editorprocesos.dto.MensajeDTO;

import java.util.List;

public interface MensajeService {

    /**
     * HU-25 (Dev 1): Envía un Message Throw. Valida JSON del payload si se proporciona.
     */
    MensajeDTO throwMessage(MensajeDTO dto);

    /**
     * HU-27 (Dev 2): Captura un mensaje BPMN entrante al proceso destino.
     */
    MensajeDTO catchMessage(MensajeDTO mensajeDTO);

    /**
     * Lista todos los mensajes de un proceso.
     */
    List<MensajeDTO> listarMensajesPorProceso(Long procesoId);

    /**
     * Obtiene un mensaje por su ID.
     */
    MensajeDTO obtenerMensajePorId(Long id);

    // =====================================================================================
    // HU-28 (Dev 3): Correlación de Mensajes
    // =====================================================================================

    /**
     * HU-28: Ejecuta la correlación THROW↔CATCH por correlationKey.
     * Busca un THROW PENDIENTE y un CATCH PENDIENTE con la misma key,
     * los marca como ENTREGADO y copia el payload del THROW al CATCH.
     */
    CorrelacionResultDTO correlateMessages(String correlationKey);

    /**
     * HU-28: Lista todos los mensajes que comparten un correlationKey.
     */
    List<MensajeDTO> buscarPorCorrelationKey(String correlationKey);
}
