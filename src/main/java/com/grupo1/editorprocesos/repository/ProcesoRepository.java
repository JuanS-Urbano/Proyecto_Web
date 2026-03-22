package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {
    List<Proceso> findByPoolId(Long poolId);
    List<Proceso> findByPoolEmpresaId(Long empresaId);
    List<Proceso> findByPoolIdAndEstado(Long poolId, EstadoProceso estado);
    List<Proceso> findByPoolIdAndCategoria(Long poolId, String categoria);
    List<Proceso> findByPoolIdAndEstadoAndCategoria(Long poolId, EstadoProceso estado, String categoria);
    List<Proceso> findByPoolIdAndNombreContainingIgnoreCase(Long poolId, String nombre);

    @Query("""
        SELECT p
        FROM Proceso p
        WHERE p.pool.empresa.id = :empresaId
           OR (:incluirCompartidos = true AND p.configuracionCompartido = true)
        """)
    List<Proceso> buscarVisiblesPorEmpresa(@Param("empresaId") Long empresaId,
                       @Param("incluirCompartidos") boolean incluirCompartidos);

    @Query("""
        SELECT p
        FROM Proceso p
        WHERE p.pool.id = :poolId
           OR (:incluirCompartidos = true AND p.configuracionCompartido = true)
        """)
    List<Proceso> buscarVisiblesPorPool(@Param("poolId") Long poolId,
                    @Param("incluirCompartidos") boolean incluirCompartidos);

    @Query("""
        SELECT p
        FROM Proceso p
        WHERE (p.pool.id = :poolId
           OR (:incluirCompartidos = true AND p.configuracionCompartido = true))
          AND (:estado IS NULL OR p.estado = :estado)
          AND (:categoria IS NULL OR p.categoria = :categoria)
        """)
    List<Proceso> buscarVisiblesPorPoolConFiltros(@Param("poolId") Long poolId,
                          @Param("estado") EstadoProceso estado,
                          @Param("categoria") String categoria,
                          @Param("incluirCompartidos") boolean incluirCompartidos);

    @Query("""
        SELECT p
        FROM Proceso p
        WHERE (p.pool.id = :poolId
           OR (:incluirCompartidos = true AND p.configuracionCompartido = true))
          AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
        """)
    List<Proceso> buscarVisiblesPorPoolYNombre(@Param("poolId") Long poolId,
                           @Param("nombre") String nombre,
                           @Param("incluirCompartidos") boolean incluirCompartidos);
}
