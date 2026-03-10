package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.RolProcesoDTO;
import com.grupo1.editorprocesos.service.RolProcesoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles-proceso")
@RequiredArgsConstructor
public class RolProcesoController {

    private final RolProcesoService rolProcesoService;

    @PostMapping
    public ResponseEntity<ApiResponse<RolProcesoDTO>> crearRol(@RequestBody RolProcesoDTO rolDTO) {
        RolProcesoDTO creado = rolProcesoService.crearRol(rolDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Rol de proceso creado exitosamente", creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RolProcesoDTO>> editarRol(@PathVariable Long id,
                                                                  @RequestBody RolProcesoDTO rolDTO) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Rol actualizado", rolProcesoService.editarRol(id, rolDTO)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarRol(@PathVariable Long id) {
        rolProcesoService.eliminarRol(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rol eliminado exitosamente", null));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<RolProcesoDTO>>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Roles obtenidos", rolProcesoService.listarPorEmpresa(empresaId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RolProcesoDTO>> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Rol obtenido", rolProcesoService.obtenerPorId(id)));
    }
}
