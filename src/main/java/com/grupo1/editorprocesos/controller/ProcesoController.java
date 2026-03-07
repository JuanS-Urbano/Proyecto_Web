package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.service.ProcesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procesos")
@RequiredArgsConstructor
public class ProcesoController {

    private final ProcesoService procesoService;

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProceso(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso marcado como INACTIVO", null));
    }
}
