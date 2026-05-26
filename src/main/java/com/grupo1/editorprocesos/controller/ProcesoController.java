package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.HistorialCambiosDTO;
import com.grupo1.editorprocesos.dto.ProcesoDTO;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import com.grupo1.editorprocesos.service.ProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos")
@RequiredArgsConstructor
public class ProcesoController {

    private final ProcesoService procesoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProcesoDTO>> crearProceso(@Valid @RequestBody ProcesoDTO procesoDTO) {
        ProcesoDTO resultado = procesoService.crearProceso(procesoDTO);
        ApiResponse<ProcesoDTO> response = new ApiResponse<>(true, "Proceso creado exitosamente", resultado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcesoDTO>> obtenerProceso(@PathVariable Long id) {
        ProcesoDTO proceso = procesoService.obtenerProcesoById(id);
        ApiResponse<ProcesoDTO> response = new ApiResponse<>(true, "Proceso obtenido exitosamente", proceso);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorEmpresa(@PathVariable Long empresaId) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorEmpresa(empresaId);
        ApiResponse<List<ProcesoDTO>> response = new ApiResponse<>(true, "Procesos obtenidos exitosamente", procesos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pool/{poolId}/lista")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorPool(@PathVariable Long poolId) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorPool(poolId);
        ApiResponse<List<ProcesoDTO>> response = new ApiResponse<>(true, "Procesos del pool obtenidos exitosamente", procesos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pool/{poolId}/filtro/estado")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorPoolYEstado(
            @PathVariable Long poolId,
            @RequestParam EstadoProceso estado) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorPoolYEstado(poolId, estado);
        ApiResponse<List<ProcesoDTO>> response = new ApiResponse<>(true, "Procesos filtrados por estado", procesos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pool/{poolId}/filtro/categoria")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorPoolYCategoria(
            @PathVariable Long poolId,
            @RequestParam String categoria) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorPoolYCategoria(poolId, categoria);
        ApiResponse<List<ProcesoDTO>> response = new ApiResponse<>(true, "Procesos filtrados por categoría", procesos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pool/{poolId}/filtro")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorPoolConFiltros(
            @PathVariable Long poolId,
            @RequestParam(required = false) EstadoProceso estado,
            @RequestParam(required = false) String categoria) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorPoolConFiltros(poolId, estado, categoria);
        ApiResponse<List<ProcesoDTO>> response = new ApiResponse<>(true, "Procesos filtrados", procesos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pool/{poolId}/buscar")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> buscarProcesosPorNombre(
            @PathVariable Long poolId,
            @RequestParam String nombre) {
        List<ProcesoDTO> procesos = procesoService.buscarProcesosPorNombre(poolId, nombre);
        ApiResponse<List<ProcesoDTO>> response = new ApiResponse<>(true, "Búsqueda de procesos completada", procesos);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcesoDTO>> editarProceso(
            @PathVariable Long id,
            @Valid @RequestBody ProcesoDTO procesoDTO) {
        ProcesoDTO resultado = procesoService.editarProceso(id, procesoDTO);
        ApiResponse<ProcesoDTO> response = new ApiResponse<>(true, "Proceso actualizado exitosamente", resultado);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<ApiResponse<Void>> eliminarProceso(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        ApiResponse<Void> response = new ApiResponse<>(true, "Proceso eliminado exitosamente", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{procesoId}/historial")
    public ResponseEntity<ApiResponse<List<HistorialCambiosDTO>>> obtenerHistorialProceso(@PathVariable Long procesoId) {
        List<HistorialCambiosDTO> historial = procesoService.obtenerHistorialProceso(procesoId);
        ApiResponse<List<HistorialCambiosDTO>> response = new ApiResponse<>(true, "Historial de cambios obtenido exitosamente", historial);
        return ResponseEntity.ok(response);
    }
}
