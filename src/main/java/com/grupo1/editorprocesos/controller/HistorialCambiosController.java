package com.grupo1.editorprocesos.controller;

import com.grupo1.editorprocesos.dto.ApiResponse;
import com.grupo1.editorprocesos.dto.HistorialCambiosDTO;
import com.grupo1.editorprocesos.dto.ReferenciaDTO;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/historial-cambios")
@RequiredArgsConstructor
public class HistorialCambiosController {

    private final HistorialCambiosRepository historialCambiosRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HistorialCambiosDTO>>> getByProceso(
            @RequestParam Long procesoId) {

        List<HistorialCambios> historialList = historialCambiosRepository.findByProcesoId(procesoId);

        // Recolectamos todos los usuarioIds únicos y resolvemos sus emails en UNA sola consulta (fix N+1)
        Set<Long> usuarioIds = historialList.stream()
                .map(HistorialCambios::getUsuarioId)
                .collect(Collectors.toSet());

        Map<Long, String> emailPorUsuarioId = historialCambiosRepository
                .findEmailsByUsuarioIds(usuarioIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));

        List<HistorialCambiosDTO> lista = historialList.stream()
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .map(h -> {
                    String emailUsuario = emailPorUsuarioId.getOrDefault(
                            h.getUsuarioId(), "Usuario #" + h.getUsuarioId());
                    return new HistorialCambiosDTO(
                            h.getId(),
                            new ReferenciaDTO(h.getProceso().getId(), h.getProceso().getNombre()),
                            new ReferenciaDTO(h.getUsuarioId(), emailUsuario),
                            h.getFecha(),
                            h.getCambio()
                    );
                })
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "OK", lista));
    }
}
