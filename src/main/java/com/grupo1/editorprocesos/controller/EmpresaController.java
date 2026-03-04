package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.EmpresaDTO;
import com.grupo1.editorprocesos.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaDTO>> crearEmpresa(@RequestBody EmpresaDTO empresaDTO) {
        EmpresaDTO creada = empresaService.crearEmpresa(empresaDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Empresa creada exitosamente", creada));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmpresaDTO>>> listarEmpresas() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Empresas obtenidas", empresaService.listarEmpresas()));
    }
}
