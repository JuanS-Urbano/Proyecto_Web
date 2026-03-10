package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.HistorialCambiosDTO;
import com.grupo1.editorprocesos.dto.ProcesoDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.ProcesoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcesoServiceImpl implements ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ProcesoDTO crearProceso(ProcesoDTO procesoDTO) {
        if (procesoDTO.getPoolId() == null) {
            throw new IllegalArgumentException("El poolId es requerido para crear un proceso");
        }

        // Obtener el pool especificado
        Pool pool = poolRepository.findById(procesoDTO.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con ID: " + procesoDTO.getPoolId()));

        // Obtener la empresa del pool
        Empresa empresa = pool.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException(
                    "Empresa no encontrada para el pool con ID: " + procesoDTO.getPoolId());
        }

        // Obtener el usuario actual desde el contexto o sesión
        // Por ahora, usamos el usuarioId del DTO si está disponible,
        // En fase 3 (Seguridad) esto vendrá del JWT
        Usuario usuarioActual = obtenerUsuarioActual();

        // Validar que el usuario pertenezca a la empresa
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);

        // Crear la nueva entidad Proceso
        Proceso proceso = new Proceso();
        proceso.setNombre(procesoDTO.getNombre());
        proceso.setDescripcion(procesoDTO.getDescripcion());
        proceso.setCategoria(procesoDTO.getCategoria());
        proceso.setEstado(EstadoProceso.BORRADOR); // Por defecto, todo proceso se crea en BORRADOR
        Boolean compartido = procesoDTO.getConfiguracionCompartido();
        proceso.setConfiguracionCompartido(compartido != null ? compartido : false);
        proceso.setPool(pool);

        // Persistir el proceso
        Proceso procesoGuardado = procesoRepository.save(proceso);

        // Convertir a DTO y retornar
        ProcesoDTO resultadoDTO = modelMapper.map(procesoGuardado, ProcesoDTO.class);
        resultadoDTO.setEmpresaId(empresa.getId());
        resultadoDTO.setPoolId(pool.getId());

        return resultadoDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProcesoDTO obtenerProcesoById(Long id) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + id));

        ProcesoDTO dto = modelMapper.map(proceso, ProcesoDTO.class);
        if (proceso.getPool() != null) {
            dto.setPoolId(proceso.getPool().getId());
            if (proceso.getPool().getEmpresa() != null) {
                dto.setEmpresaId(proceso.getPool().getEmpresa().getId());
            }
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con ID: " + empresaId));

        // Obtener usuario actual y validar que pertenezca a la empresa
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);

        // Consulta eficiente por empresa via JPA
        return procesoRepository.findByPoolEmpresaId(empresaId).stream()
                .map(proceso -> {
                    ProcesoDTO dto = modelMapper.map(proceso, ProcesoDTO.class);
                    dto.setPoolId(proceso.getPool().getId());
                    dto.setEmpresaId(empresaId);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPool(Long poolId) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con ID: " + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolId(poolId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcesoDTO> listarProcesosPorPoolYEstado(Long poolId, EstadoProceso estado) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con ID: " + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolIdAndEstado(poolId, estado).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcesoDTO> listarProcesosPorPoolYCategoria(Long poolId, String categoria) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con ID: " + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolIdAndCategoria(poolId, categoria).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcesoDTO> listarProcesosPorPoolConFiltros(Long poolId, EstadoProceso estado, String categoria) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con ID: " + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        List<Proceso> procesos;
        
        if (estado != null && categoria != null) {
            procesos = procesoRepository.findByPoolIdAndEstadoAndCategoria(poolId, estado, categoria);
        } else if (estado != null) {
            procesos = procesoRepository.findByPoolIdAndEstado(poolId, estado);
        } else if (categoria != null) {
            procesos = procesoRepository.findByPoolIdAndCategoria(poolId, categoria);
        } else {
            procesos = procesoRepository.findByPoolId(poolId);
        }

        return procesos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcesoDTO> buscarProcesosPorNombre(Long poolId, String nombre) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pool no encontrado con ID: " + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByNombreContains(nombre).stream()
                .filter(p -> p.getPool().getId().equals(poolId))
                .map(this::convertirADTO)
        // Validar que el usuario pertenezca a la empresa del pool
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        // Consulta eficiente por pool via JPA
        return procesoRepository.findByPoolId(poolId).stream()
                .map(proceso -> {
                    ProcesoDTO dto = modelMapper.map(proceso, ProcesoDTO.class);
                    dto.setPoolId(pool.getId());
                    dto.setEmpresaId(pool.getEmpresa().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProcesoDTO editarProceso(Long id, ProcesoDTO procesoDTO) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + id));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        StringBuilder cambios = new StringBuilder("Cambios realizados: ");
        boolean hubosCambios = false;

        if (procesoDTO.getNombre() != null && !procesoDTO.getNombre().equals(proceso.getNombre())) {
            cambios.append("Nombre: \"").append(proceso.getNombre()).append("\" → \"").append(procesoDTO.getNombre()).append("\". ");
            proceso.setNombre(procesoDTO.getNombre());
            hubosCambios = true;
        }
        
        if (procesoDTO.getDescripcion() != null && !procesoDTO.getDescripcion().equals(proceso.getDescripcion())) {
            cambios.append("Descripción actualizada. ");
            proceso.setDescripcion(procesoDTO.getDescripcion());
            hubosCambios = true;
        }
        
        if (procesoDTO.getCategoria() != null && !procesoDTO.getCategoria().equals(proceso.getCategoria())) {
            cambios.append("Categoría: \"").append(proceso.getCategoria()).append("\" → \"").append(procesoDTO.getCategoria()).append("\". ");
            proceso.setCategoria(procesoDTO.getCategoria());
            hubosCambios = true;
        }
        
        if (procesoDTO.getEstado() != null && !procesoDTO.getEstado().equals(proceso.getEstado())) {
            cambios.append("Estado: ").append(proceso.getEstado()).append(" → ").append(procesoDTO.getEstado()).append(". ");
            proceso.setEstado(procesoDTO.getEstado());
            hubosCambios = true;
        }
        
        if (procesoDTO.getConfiguracionCompartido() != null && 
            !procesoDTO.getConfiguracionCompartido().equals(proceso.getConfiguracionCompartido())) {
            cambios.append("Configuración compartido: ").append(proceso.getConfiguracionCompartido()).append(" → ").append(procesoDTO.getConfiguracionCompartido()).append(". ");
            proceso.setConfiguracionCompartido(procesoDTO.getConfiguracionCompartido());
            hubosCambios = true;
        }

        Proceso procesoActualizado = procesoRepository.save(proceso);

        if (hubosCambios) {
            HistorialCambios historial = new HistorialCambios();
            historial.setProceso(proceso);
            historial.setUsuarioId(usuarioActual.getId());
            historial.setFecha(LocalDateTime.now());
            historial.setCambio(cambios.toString());
            historialCambiosRepository.save(historial);
        }

        return convertirADTO(procesoActualizado);
        // =====================================================================================
        // TODO (Dev 5 - HU-05): Registrar cambios en HistorialCambios
        // historialCambiosService.registrar(procesoActualizado, usuarioActual,
        // "EDICION");
        // =====================================================================================

        ProcesoDTO resultadoDTO = modelMapper.map(procesoActualizado, ProcesoDTO.class);
        resultadoDTO.setPoolId(proceso.getPool().getId());
        resultadoDTO.setEmpresaId(proceso.getPool().getEmpresa().getId());

        return resultadoDTO;
    }

    @Override
    @Transactional
    public void eliminarProceso(Long id) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + id));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        proceso.setEstado(EstadoProceso.INACTIVO);
        procesoRepository.save(proceso);

        HistorialCambios historial = new HistorialCambios();
        historial.setProceso(proceso);
        historial.setUsuarioId(usuarioActual.getId());
        historial.setFecha(LocalDateTime.now());
        historial.setCambio("Proceso eliminado (cambió a estado INACTIVO)");
        historialCambiosRepository.save(historial);
    }

    @Override
    public List<HistorialCambiosDTO> obtenerHistorialProceso(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return historialCambiosRepository.findByProcesoId(procesoId).stream()
                .map(h -> modelMapper.map(h, HistorialCambiosDTO.class))
                .collect(Collectors.toList());
    }

    private ProcesoDTO convertirADTO(Proceso proceso) {
        ProcesoDTO dto = modelMapper.map(proceso, ProcesoDTO.class);
        if (proceso.getPool() != null) {
            dto.setPoolId(proceso.getPool().getId());
            if (proceso.getPool().getEmpresa() != null) {
                dto.setEmpresaId(proceso.getPool().getEmpresa().getId());
            }
        }
        return dto;
    }

    private Usuario obtenerUsuarioActual() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException(
                        "No hay usuario autenticado en la sesión"));
    }

    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null 
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }
}
