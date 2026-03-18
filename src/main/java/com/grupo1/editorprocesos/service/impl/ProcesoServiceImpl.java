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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesoServiceImpl implements ProcesoService {

    private static final String POOL_NO_ENCONTRADO = "Pool no encontrado con ID: ";
    private static final String PROCESO_NO_ENCONTRADO = "Proceso no encontrado con ID: ";

    private final ProcesoRepository procesoRepository;
    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final ModelMapper modelMapper;
    private final HttpServletRequest httpServletRequest;

    @Override
    @Transactional
    public ProcesoDTO crearProceso(ProcesoDTO procesoDTO) {
        if (procesoDTO.getPoolId() == null) {
            throw new IllegalArgumentException("El poolId es requerido para crear un proceso");
        }

        Pool pool = poolRepository.findById(procesoDTO.getPoolId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO + procesoDTO.getPoolId()));

        Empresa empresa = pool.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException(
                    "Empresa no encontrada para el pool con ID: " + procesoDTO.getPoolId());
        }

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);

        Proceso proceso = new Proceso();
        proceso.setNombre(procesoDTO.getNombre());
        proceso.setDescripcion(procesoDTO.getDescripcion());
        proceso.setCategoria(procesoDTO.getCategoria());
        proceso.setEstado(EstadoProceso.BORRADOR);
        Boolean compartido = procesoDTO.getConfiguracionCompartido();
        proceso.setConfiguracionCompartido(Boolean.TRUE.equals(compartido));
        proceso.setPool(pool);

        Proceso procesoGuardado = procesoRepository.save(proceso);

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
                        PROCESO_NO_ENCONTRADO + id));
        return convertirADTO(proceso);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa no encontrada con ID: " + empresaId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);

        return procesoRepository.findByPoolEmpresaId(empresaId).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPool(Long poolId) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolId(poolId).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPoolYEstado(Long poolId, EstadoProceso estado) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolIdAndEstado(poolId, estado).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPoolYCategoria(Long poolId, String categoria) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolIdAndCategoria(poolId, categoria).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPoolConFiltros(Long poolId, EstadoProceso estado, String categoria) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO + poolId));

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
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> buscarProcesosPorNombre(Long poolId, String nombre) {
        Pool pool = poolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        POOL_NO_ENCONTRADO + poolId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());

        return procesoRepository.findByPoolIdAndNombreContainingIgnoreCase(poolId, nombre).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional
    public ProcesoDTO editarProceso(Long id, ProcesoDTO procesoDTO) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PROCESO_NO_ENCONTRADO + id));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        StringBuilder cambios = new StringBuilder("Cambios realizados: ");
        boolean huboCambios = false;

        if (procesoDTO.getNombre() != null && !procesoDTO.getNombre().equals(proceso.getNombre())) {
            cambios.append("Nombre: \"").append(proceso.getNombre()).append("\" → \"").append(procesoDTO.getNombre()).append("\". ");
            proceso.setNombre(procesoDTO.getNombre());
            huboCambios = true;
        }

        if (procesoDTO.getDescripcion() != null && !procesoDTO.getDescripcion().equals(proceso.getDescripcion())) {
            cambios.append("Descripción actualizada. ");
            proceso.setDescripcion(procesoDTO.getDescripcion());
            huboCambios = true;
        }

        if (procesoDTO.getCategoria() != null && !procesoDTO.getCategoria().equals(proceso.getCategoria())) {
            cambios.append("Categoría: \"").append(proceso.getCategoria()).append("\" → \"").append(procesoDTO.getCategoria()).append("\". ");
            proceso.setCategoria(procesoDTO.getCategoria());
            huboCambios = true;
        }

        if (procesoDTO.getEstado() != null && !procesoDTO.getEstado().equals(proceso.getEstado())) {
            cambios.append("Estado: ").append(proceso.getEstado()).append(" → ").append(procesoDTO.getEstado()).append(". ");
            proceso.setEstado(procesoDTO.getEstado());
            huboCambios = true;
        }

        if (procesoDTO.getConfiguracionCompartido() != null &&
            !procesoDTO.getConfiguracionCompartido().equals(proceso.getConfiguracionCompartido())) {
            cambios.append("Configuración compartido: ").append(proceso.getConfiguracionCompartido()).append(" → ").append(procesoDTO.getConfiguracionCompartido()).append(". ");
            proceso.setConfiguracionCompartido(procesoDTO.getConfiguracionCompartido());
            huboCambios = true;
        }

        Proceso procesoActualizado = procesoRepository.save(proceso);

        if (huboCambios) {
            HistorialCambios historial = new HistorialCambios();
            historial.setProceso(procesoActualizado);
            historial.setUsuarioId(usuarioActual.getId());
            historial.setFecha(LocalDateTime.now());
            historial.setCambio(cambios.toString());
            historialCambiosRepository.save(historial);
        }

        return convertirADTO(procesoActualizado);
    }

    @Override
    @Transactional
    public void eliminarProceso(Long id) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PROCESO_NO_ENCONTRADO + id));

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
    @Transactional(readOnly = true)
    public List<HistorialCambiosDTO> obtenerHistorialProceso(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PROCESO_NO_ENCONTRADO + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return historialCambiosRepository.findByProcesoId(procesoId).stream()
                .map(h -> modelMapper.map(h, HistorialCambiosDTO.class))
                .toList();
    }

    // ===== Métodos privados de utilidad =====

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

    /**
     * Obtiene el usuario actual desde el header X-User-Email del request HTTP.
     * En la Fase 3 (JWT), se reemplazará por SecurityContextHolder.getContext().getAuthentication().
     */
    private Usuario obtenerUsuarioActual() {
        String email = httpServletRequest.getHeader("X-User-Email");
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("No se proporcionó el header X-User-Email para identificar al usuario");
        }
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Usuario no encontrado con el email: " + email));
    }

    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }
}
