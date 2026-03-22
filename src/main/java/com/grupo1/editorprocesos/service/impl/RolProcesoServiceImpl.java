package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.RolProcesoDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.process.RolProceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.RolProcesoRepository;
import com.grupo1.editorprocesos.service.RolProcesoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolProcesoServiceImpl implements RolProcesoService {

    private final RolProcesoRepository rolProcesoRepository;
    private final EmpresaRepository empresaRepository;
    private final ActividadRepository actividadRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public RolProcesoDTO crearRol(RolProcesoDTO rolDTO) {
        Empresa empresa = empresaRepository.findById(rolDTO.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con id: " + rolDTO.getEmpresaId()));
        RolProceso rol = new RolProceso();
        rol.setNombre(rolDTO.getNombre());
        rol.setDescripcion(rolDTO.getDescripcion());
        rol.setEmpresa(empresa);
        return toDTO(rolProcesoRepository.save(rol));
    }

    @Override
    @Transactional
    public RolProcesoDTO editarRol(Long id, RolProcesoDTO rolDTO) {
        RolProceso rol = findById(id);
        if (rolDTO.getNombre() != null) {
            rol.setNombre(rolDTO.getNombre());
        }
        if (rolDTO.getDescripcion() != null) {
            rol.setDescripcion(rolDTO.getDescripcion());
        }
        return toDTO(rolProcesoRepository.save(rol));
    }

    // =====================================================================================
    // HU-19: Eliminar RolProceso — con validación de dependencias
    // =====================================================================================

    @Override
    @Transactional
    public void eliminarRol(Long id) {
        RolProceso rol = findById(id);

        // HU-19: Validar que no esté asignado a ningún Lane
        if (rolProcesoRepository.estaEnUso(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el rol '" + rol.getNombre()
                            + "' porque está asignado a uno o más lanes.");
        }

        // HU-19: Validar que no esté referenciado indirectamente por actividades
        if (actividadRepository.existeActividadConRol(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el rol '" + rol.getNombre()
                            + "' porque está siendo usado por una o más actividades a través de sus lanes.");
        }

        rolProcesoRepository.delete(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolProcesoDTO> listarPorEmpresa(Long empresaId) {
        return rolProcesoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolProcesoDTO obtenerPorId(Long id) {
        return toDTO(findById(id));
    }

    private RolProceso findById(Long id) {
        return rolProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rol de proceso no encontrado con id: " + id));
    }

    private RolProcesoDTO toDTO(RolProceso r) {
        RolProcesoDTO dto = modelMapper.map(r, RolProcesoDTO.class);
        dto.setEmpresaId(r.getEmpresa().getId());
        return dto;
    }
}
