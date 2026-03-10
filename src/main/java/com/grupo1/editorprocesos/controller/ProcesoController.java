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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProceso(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso marcado como INACTIVO", null));
    /**
     * Crea un nuevo proceso asociado a la empresa/pool del usuario actual.
     * 
     * Validaciones:
     * - El usuario debe estar autenticado
     * - El usuario debe pertenecer a la empresa del pool
     * - El pool debe existir
     * - El proceso se crea en estado BORRADOR por defecto
     * 
     * @param procesoDTO DTO con los datos del proceso a crear (nombre, descripción, categoria, poolId)
     * @return ResponseEntity con el proceso creado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProcesoDTO>> crearProceso(@RequestBody ProcesoDTO procesoDTO) {
        ProcesoDTO procesoCreado = procesoService.crearProceso(procesoDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Proceso creado exitosamente", procesoCreado));
    }

    /**
     * Obtiene un proceso específico por su ID.
     * Valida que el usuario pertenezca a la empresa del proceso.
     * 
     * @param id ID del proceso a obtener
     * @return ResponseEntity con los datos del proceso
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcesoDTO>> obtenerProceso(@PathVariable Long id) {
        ProcesoDTO proceso = procesoService.obtenerProcesoById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso obtenido exitosamente", proceso));
    }

    /**
     * Lista todos los procesos de una empresa específica.
     * Valida que el usuario pertenezca a la empresa.
     * 
     * @param empresaId ID de la empresa
     * @return ResponseEntity con la lista de procesos de la empresa
     */
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorEmpresa(@PathVariable Long empresaId) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorEmpresa(empresaId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Procesos de la empresa obtenidos", procesos));
    }

    /**
     * Lista todos los procesos de un pool específico.
     * Valida que el usuario pertenezca a la empresa del pool.
     * 
     * @param poolId ID del pool
     * @return ResponseEntity con la lista de procesos del pool
     */
    @GetMapping("/pool/{poolId}")
    public ResponseEntity<ApiResponse<List<ProcesoDTO>>> listarProcesosPorPool(@PathVariable Long poolId) {
        List<ProcesoDTO> procesos = procesoService.listarProcesosPorPool(poolId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Procesos del pool obtenidos", procesos));
    }

    /**
     * Edita un proceso existente.
     * Valida que el usuario pertenezca a la empresa del proceso.
     * 
     * @param id ID del proceso a editar
     * @param procesoDTO DTO con los nuevos datos del proceso
     * @return ResponseEntity con el proceso actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProcesoDTO>> editarProceso(@PathVariable Long id, @RequestBody ProcesoDTO procesoDTO) {
        ProcesoDTO procesoActualizado = procesoService.editarProceso(id, procesoDTO);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso actualizado exitosamente", procesoActualizado));
    }

    /**
     * Elimina (lógicamente) un proceso.
     * Valida que el usuario pertenezca a la empresa del proceso.
     * El proceso cambia a estado INACTIVO en lugar de ser eliminado físicamente.
     * 
     * @param id ID del proceso a eliminar
     * @return ResponseEntity con mensaje de éxito
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarProceso(@PathVariable Long id) {
        procesoService.eliminarProceso(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proceso eliminado exitosamente", null));
    }
}
