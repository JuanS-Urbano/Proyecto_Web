package com.grupo1.editorprocesos.repository;

import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
}
