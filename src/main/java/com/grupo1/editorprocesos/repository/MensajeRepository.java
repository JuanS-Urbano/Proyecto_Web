package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Lista todos los mensajes de un proceso (como origen — throwMessage).
     */
    List<Mensaje> findByProcesoId(Long procesoId);

    /**
     * Dev 2 (HU-27): Busca mensajes por nombre y estado.
     */
    List<Mensaje> findByNombreAndEstado(String nombre, EstadoMensaje estado);

    /**
     * Dev 3 (HU-28): Busca mensajes por correlationKey.
     */
    List<Mensaje> findByCorrelationKey(String correlationKey);

    /**
     * Busca mensajes por tipo y proceso.
     */
    List<Mensaje> findByProcesoIdAndTipo(Long procesoId, TipoMensaje tipo);

    /**
     * Dev 2 (HU-27): Verifica si ya existe un CATCH duplicado.
     */
    boolean existsByNombreAndTipoAndProcesoDestinoId(String nombre, TipoMensaje tipo, Long procesoDestinoId);

    /**
     * Dev 2 (HU-27): Lista mensajes CATCH por proceso destino.
     */
    List<Mensaje> findByProcesoDestinoId(Long procesoDestinoId);

    /**
     * Dev 3 (HU-28): Busca el primer mensaje por correlationKey, tipo y estado.
     * Usado para emparejar THROW↔CATCH pendientes.
     */
    Optional<Mensaje> findFirstByCorrelationKeyAndTipoAndEstado(
            String correlationKey, TipoMensaje tipo, EstadoMensaje estado);
}
