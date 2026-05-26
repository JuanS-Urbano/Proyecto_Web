package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface HistorialCambiosRepository extends JpaRepository<HistorialCambios, Long> {

    @Query("SELECT h FROM HistorialCambios h JOIN FETCH h.proceso WHERE h.proceso.id = :procesoId")
    List<HistorialCambios> findByProcesoId(@Param("procesoId") Long procesoId);

    /**
     * Obtiene los emails de los usuarios dados sus IDs en una sola consulta,
     * eliminando el N+1 del controlador de historial.
     */
    @Query("SELECT u.id, u.email FROM Usuario u WHERE u.id IN :ids")
    List<Object[]> findEmailsByUsuarioIds(@Param("ids") Set<Long> ids);

    @Query("SELECT h FROM HistorialCambios h JOIN FETCH h.proceso WHERE h.proceso.pool.empresa.id = :empresaId ORDER BY h.fecha DESC")
    List<HistorialCambios> findRecentByEmpresaId(@Param("empresaId") Long empresaId, org.springframework.data.domain.Pageable pageable);
}
