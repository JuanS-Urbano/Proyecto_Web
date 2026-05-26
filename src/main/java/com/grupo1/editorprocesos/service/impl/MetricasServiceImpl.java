package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.HistorialCambiosDTO;
import com.grupo1.editorprocesos.dto.MetricasDTO;
import com.grupo1.editorprocesos.dto.ReferenciaDTO;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import com.grupo1.editorprocesos.repository.*;
import com.grupo1.editorprocesos.service.MetricasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricasServiceImpl implements MetricasService {

    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActividadRepository actividadRepository;
    private final RolProcesoRepository rolProcesoRepository;
    private final HistorialCambiosRepository historialCambiosRepository;

    @Override
    @Transactional(readOnly = true)
    public MetricasDTO obtenerMetricasPorEmpresa(Long empresaId) {
        // 1. Procesos de la empresa
        List<Proceso> procesos = procesoRepository.findByPoolEmpresaId(empresaId);
        long totalProcesos = procesos.stream()
                .filter(p -> p.getEstado() != EstadoProceso.INACTIVO)
                .count();

        Map<String, Long> procesosPorEstado = procesos.stream()
                .collect(Collectors.groupingBy(p -> p.getEstado().name(), Collectors.counting()));

        // Asegurar que las llaves básicas existan en el mapa para el frontend
        for (EstadoProceso est : EstadoProceso.values()) {
            procesosPorEstado.putIfAbsent(est.name(), 0L);
        }

        // 2. Usuarios de la empresa
        List<Usuario> usuarios = usuarioRepository.findByEmpresaId(empresaId);
        long totalUsuarios = usuarios.size();
        Map<String, Long> usuariosPorRol = usuarios.stream()
                .collect(Collectors.groupingBy(u -> u.getRolSistema().name(), Collectors.counting()));

        // 3. Actividades totales
        long totalActividades = actividadRepository.countByEmpresaId(empresaId);

        // 4. Roles de proceso
        long totalRolesProceso = rolProcesoRepository.findByEmpresaId(empresaId).size();

        // 5. Últimos 10 cambios
        List<HistorialCambios> historialList = historialCambiosRepository.findRecentByEmpresaId(empresaId, PageRequest.of(0, 10));

        Set<Long> usuarioIds = historialList.stream()
                .map(HistorialCambios::getUsuarioId)
                .collect(Collectors.toSet());

        Map<Long, String> emailPorUsuarioId = new HashMap<>();
        if (!usuarioIds.isEmpty()) {
            emailPorUsuarioId = historialCambiosRepository.findEmailsByUsuarioIds(usuarioIds).stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (String) row[1],
                            (existing, replacement) -> existing
                    ));
        }

        Map<Long, String> finalEmailPorUsuarioId = emailPorUsuarioId;
        List<HistorialCambiosDTO> ultimosCambios = historialList.stream()
                .map(h -> {
                    String emailUsuario = finalEmailPorUsuarioId.getOrDefault(
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

        return MetricasDTO.builder()
                .totalProcesos(totalProcesos)
                .procesosPorEstado(procesosPorEstado)
                .totalUsuarios(totalUsuarios)
                .usuariosPorRol(usuariosPorRol)
                .totalActividades(totalActividades)
                .totalRolesProceso(totalRolesProceso)
                .ultimosCambios(ultimosCambios)
                .build();
    }
}
