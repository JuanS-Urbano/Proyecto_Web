package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para la entidad Mensaje.
 * Provee las queries mínimas para HU-27 (catchMessage).
 *
 * NOTA: La entidad Mensaje no tiene FK directa a Proceso en el modelo actual.
 * Se usa procesoDestinoId como campo lógico en las queries nombradas.
 * Dev 3 (HU-28) puede extender este repositorio para correlación avanzada.
 */
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Verifica si ya existe un mensaje con el mismo nombre, tipo y proceso destino.
     * Usado por catchMessage() para evitar duplicados.
     *
     * ─── Cruce con Dev 1 ────────────────────────────────────────────────
     * Si Dev 1 persiste el THROW con el mismo nombre, no choca aquí
     * porque el tipo es diferente ("THROW" vs "CATCH").
     * ─────────────────────────────────────────────────────────────────────
     */
    @Query("""
        SELECT COUNT(m) > 0
        FROM Mensaje m
        WHERE m.nombre = :nombre
          AND m.tipo = :tipo
          AND m.procesoDestinoId = :procesoDestinoId
        """)
    boolean existsByNombreAndTipoAndProcesoDestinoId(
            @Param("nombre") String nombre,
            @Param("tipo") String tipo,
            @Param("procesoDestinoId") Long procesoDestinoId);

    /**
     * Lista todos los mensajes CATCH asociados a un proceso destino.
     */
    @Query("SELECT m FROM Mensaje m WHERE m.procesoDestinoId = :procesoDestinoId AND m.tipo = 'CATCH'")
    List<Mensaje> findByProcesoDestinoId(@Param("procesoDestinoId") Long procesoDestinoId);

    /**
     * Busca un mensaje CATCH por nombre y proceso destino.
     * Útil para que Dev 1 verifique que su THROW tiene un CATCH esperando.
     */
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.nombre = :nombre
          AND m.tipo = 'CATCH'
          AND m.procesoDestinoId = :procesoDestinoId
        """)
    java.util.Optional<Mensaje> findCatchByNombreAndProcesoDestino(
            @Param("nombre") String nombre,
            @Param("procesoDestinoId") Long procesoDestinoId);
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
