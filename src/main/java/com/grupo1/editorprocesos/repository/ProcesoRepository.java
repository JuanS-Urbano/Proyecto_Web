package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesoRepository extends JpaRepository<Proceso, Long> {
    List<Proceso> findByPoolId(Long poolId);

    List<Proceso> findByPoolEmpresaId(Long empresaId);
}
