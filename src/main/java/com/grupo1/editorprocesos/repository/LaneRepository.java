package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.process.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {
}
