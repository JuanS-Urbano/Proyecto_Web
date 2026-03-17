package com.grupo1.editorprocesos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo1.editorprocesos.model.entity.process.Lane;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {

    java.util.List<Lane> findByProcesoId(Long procesoId);
}
