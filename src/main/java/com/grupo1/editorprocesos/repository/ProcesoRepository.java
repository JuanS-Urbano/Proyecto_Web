package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
