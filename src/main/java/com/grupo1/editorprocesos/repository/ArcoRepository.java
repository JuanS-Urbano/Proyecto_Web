package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArcoRepository extends JpaRepository<Arco, Long> {

    /**
     * Lista todos los arcos que pertenecen a un proceso.
     */
    List<Arco> findByProcesoId(Long procesoId);

    /**
     * Busca arcos donde el elemento origen tenga el nombre especificado.
     */
    List<Arco> findByOrigenId(String origenId);

    /**
     * Busca arcos donde el elemento destino tenga el nombre especificado.
     */
    List<Arco> findByDestinoId(String destinoId);

    /**
     * Busca un arco específico entre origen y destino en un proceso.
     */
    Optional<Arco> findByOrigenIdAndDestinoIdAndProcesoId(String origenId, String destinoId, Long procesoId);
}
