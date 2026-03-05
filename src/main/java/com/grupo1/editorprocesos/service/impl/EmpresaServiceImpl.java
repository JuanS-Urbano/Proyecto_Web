package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.EmpresaDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public EmpresaDTO crearEmpresa(EmpresaDTO empresaDTO) {
        // 1. Validar que el NIT sea único
        if (empresaRepository.existsByNit(empresaDTO.getNit())) {
            throw new DuplicateResourceException("Ya existe una empresa registrada con el NIT: " + empresaDTO.getNit());
        }

        // 2. Mapear DTO a Entidad
        Empresa empresa = modelMapper.map(empresaDTO, Empresa.class);

        // 3. Guardar la empresa en la base de datos
        empresa = empresaRepository.save(empresa);

        // =====================================================================================
        // TODO (Dev 2 - HU-02): Generar el usuario administrador al crear la empresa.
        // Aquí debes inyectar y llamar al UsuarioService (Ej:
        // usuarioService.crearAdmin(empresa))
        // Validar que se reciba la contraseña temporal o se envíe un correo.
        // =====================================================================================

        // 4. Retornar DTO guardado con su ID generado
        return modelMapper.map(empresa, EmpresaDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaDTO> listarEmpresas() {
        return empresaRepository.findAll()
                .stream()
                .map(empresa -> modelMapper.map(empresa, EmpresaDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaDTO obtenerEmpresaPorId(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + id));
        return modelMapper.map(empresa, EmpresaDTO.class);
    }
}
