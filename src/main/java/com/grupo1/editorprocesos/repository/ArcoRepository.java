package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * Busca arcos salientes de un elemento dentro de un proceso específico.
     * Usado para el saneamiento del grafo al eliminar un nodo (Actividad o Gateway).
     */
    List<Arco> findByOrigenIdAndProcesoId(String origenId, Long procesoId);

    /**
     * Busca arcos entrantes a un elemento dentro de un proceso específico.
     * Usado para el saneamiento del grafo al eliminar un nodo (Actividad o Gateway).
     */
    List<Arco> findByDestinoIdAndProcesoId(String destinoId, Long procesoId);
}
