package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Lista todos los mensajes de un proceso.
     */
    List<Mensaje> findByProcesoId(Long procesoId);

    /**
     * Busca mensajes por nombre y estado.
     * Dev 2 (HU-27 Catch) usará esto para consumir mensajes PENDIENTE.
     */
    List<Mensaje> findByNombreAndEstado(String nombre, EstadoMensaje estado);

    /**
     * Busca mensajes por correlationKey.
     * Dev 3 (HU-28 Correlación) usará esto para emparejar Throw↔Catch.
     */
    List<Mensaje> findByCorrelationKey(String correlationKey);

    /**
     * Busca mensajes por tipo y proceso.
     */
    List<Mensaje> findByProcesoIdAndTipo(Long procesoId, TipoMensaje tipo);
}
