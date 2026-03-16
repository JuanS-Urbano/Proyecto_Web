package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.bpmn.Gateway;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayRepository extends JpaRepository<Gateway, Long> {

    /**
     * Lista todos los gateways que pertenecen a un proceso.
     */
    List<Gateway> findByProcesoId(Long procesoId);

    /**
     * Busca un gateway por nombre dentro de un proceso específico.
     */
    Optional<Gateway> findByNombreAndProcesoId(String nombre, Long procesoId);
}
