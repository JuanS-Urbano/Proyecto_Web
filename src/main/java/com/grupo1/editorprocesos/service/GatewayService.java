package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.GatewayDTO;

import java.util.List;

public interface GatewayService {

    /**
     * HU-15 (CRUD Gateway): Crea un nuevo gateway dentro de un proceso.
     * Valida que el proceso exista y que el usuario pertenezca a la empresa.
     */
    GatewayDTO crearGateway(GatewayDTO gatewayDTO);

    /**
     * Edita un gateway existente. Registra los cambios en el historial.
     */
    GatewayDTO editarGateway(Long id, GatewayDTO gatewayDTO);

    /**
     * Obtiene un gateway por su ID.
     */
    GatewayDTO obtenerGatewayPorId(Long id);

    /**
     * Lista todos los gateways de un proceso.
     */
    List<GatewayDTO> listarGatewaysPorProceso(Long procesoId);

    /**
     * HU-16: Elimina un gateway y sanea el grafo del proceso.
     * - Si el gateway tiene exactamente 1 arco entrante y 1 saliente, los reconecta.
     * - Si tiene múltiples entrantes o salientes, elimina todos los arcos conectados.
     * Requiere confirmación explícita del caller (validado en el controller).
     */
    void eliminarGateway(Long id);
}
