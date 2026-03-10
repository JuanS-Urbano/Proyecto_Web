package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.ProcesoDTO;
import com.grupo1.editorprocesos.service.ProcesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos")
@RequiredArgsConstructor
public class ProcesoController {

    private final ProcesoService procesoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProcesoDTO>> crearProceso(@RequestBody ProcesoDTO procesoDTO) {
        ProcesoDTO procesoCreado = procesoService.crearProceso(procesoDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Proceso creado exitosamente", procesoCreado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcesoDTO>> obtenerProceso(@PathVariable Long id) {
        ProcesoDTO proceso = procesoService.obtenerProcesoById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso obtenido exitosamente", proceso));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorEmpresa(@PathVariable Long empresaId) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorEmpresa(empresaId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Procesos de la empresa obtenidos", procesos));
    }

    @GetMapping("/pool/{poolId}")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorPool(@PathVariable Long poolId) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorPool(poolId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Procesos del pool obtenidos", procesos));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcesoDTO>> editarProceso(@PathVariable Long id,
            @RequestBody ProcesoDTO procesoDTO) {
        ProcesoDTO procesoActualizado = procesoService.editarProceso(id, procesoDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso actualizado exitosamente", procesoActualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProceso(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso marcado como INACTIVO", null));
    }
}
