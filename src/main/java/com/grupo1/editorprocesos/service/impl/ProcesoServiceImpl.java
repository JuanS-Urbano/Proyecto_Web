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
import com.grupo1.editorprocesos.model.enums.RolSistema;
import com.grupo1.editorprocesos.service.EmpresaService;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.service.PoolService;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.service.ProcesoService;
import com.grupo1.editorprocesos.service.PermisosPoolService;
import com.grupo1.editorprocesos.service.UsuarioActualService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcesoServiceImpl implements ProcesoService {

    private static final String POOL_NO_ENCONTRADO = "Pool no encontrado con ID: ";
    private static final String PROCESO_NO_ENCONTRADO = "Proceso no encontrado con ID: ";

    private final ProcesoRepository procesoRepository;
    private final PoolService poolService;
    private final EmpresaService empresaService;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final ModelMapper modelMapper;
    private final UsuarioActualService usuarioActualService;
    private final HttpServletRequest httpServletRequest;
    private final PermisosPoolService permisosPoolService;

    @Override
    @Transactional
    public ProcesoDTO crearProceso(ProcesoDTO procesoDTO) {
        if (procesoDTO.getPool() == null || procesoDTO.getPool().getId() == null) {
            throw new IllegalArgumentException("El pool referenciado es requerido para crear un proceso");
        }

        Pool pool = poolService.obtenerEntityById(procesoDTO.getPool().getId());

        Empresa empresa = pool.getEmpresa();
        if (empresa == null) {
            throw new ResourceNotFoundException(
                    "Empresa no encontrada para el pool con ID: " + procesoDTO.getPool().getId());
        }

        // 2. Validar que el usuario actual pertenezca a la empresa del proceso
        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);
        permisosPoolService.validarPermisoEscritura(usuarioActual);

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
        resultadoDTO.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(empresa.getId(), empresa.getNombre()));
        resultadoDTO.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(pool.getId(), pool.getNombre()));

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
        Empresa empresa = empresaService.obtenerEntityById(empresaId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);
        boolean incluirCompartidos = puedeVerProcesosCompartidos(usuarioActual);

        return procesoRepository.buscarVisiblesPorEmpresa(empresaId, incluirCompartidos).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPool(Long poolId) {
        Pool pool;
        try {
            pool = poolService.obtenerEntityById(poolId);
        } catch (ResourceNotFoundException e) {
            return List.of();
        }

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());
        boolean incluirCompartidos = puedeVerProcesosCompartidos(usuarioActual);

        return procesoRepository.buscarVisiblesPorPool(poolId, incluirCompartidos).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPoolYEstado(Long poolId, EstadoProceso estado) {
        Pool pool = poolService.obtenerEntityById(poolId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());
        boolean incluirCompartidos = puedeVerProcesosCompartidos(usuarioActual);

        return procesoRepository.buscarVisiblesPorPoolConFiltros(poolId, estado, null, incluirCompartidos).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPoolYCategoria(Long poolId, String categoria) {
        Pool pool = poolService.obtenerEntityById(poolId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());
        boolean incluirCompartidos = puedeVerProcesosCompartidos(usuarioActual);

        return procesoRepository.buscarVisiblesPorPoolConFiltros(poolId, null, categoria, incluirCompartidos).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> listarProcesosPorPoolConFiltros(Long poolId, EstadoProceso estado, String categoria) {
        Pool pool = poolService.obtenerEntityById(poolId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());
        boolean incluirCompartidos = puedeVerProcesosCompartidos(usuarioActual);

        List<Proceso> procesos = procesoRepository.buscarVisiblesPorPoolConFiltros(
                poolId,
                estado,
                categoria,
                incluirCompartidos
        );

        return procesos.stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoDTO> buscarProcesosPorNombre(Long poolId, String nombre) {
        Pool pool = poolService.obtenerEntityById(poolId);

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, pool.getEmpresa());
        boolean incluirCompartidos = puedeVerProcesosCompartidos(usuarioActual);

        return procesoRepository.buscarVisiblesPorPoolYNombre(poolId, nombre, incluirCompartidos).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional
    public ProcesoDTO editarProceso(Long id, ProcesoDTO procesoDTO) {
        Proceso proceso = procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PROCESO_NO_ENCONTRADO + id));

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
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

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
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

        Usuario usuarioActual = usuarioActualService.obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return historialCambiosRepository.findByProcesoId(procesoId).stream()
                .map(h -> modelMapper.map(h, HistorialCambiosDTO.class))
                .toList();
    }

    // ===== Métodos privados de utilidad =====

    private ProcesoDTO convertirADTO(Proceso proceso) {
        ProcesoDTO dto = modelMapper.map(proceso, ProcesoDTO.class);
        if (proceso.getPool() != null) {
            dto.setPool(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getPool().getId(), proceso.getPool().getNombre()));
            if (proceso.getPool().getEmpresa() != null) {
                dto.setEmpresa(new com.grupo1.editorprocesos.dto.ReferenciaDTO(
                    proceso.getPool().getEmpresa().getId(), 
                    proceso.getPool().getEmpresa().getNombre()));
            }
        }
        return dto;
    }

    /**
     * Obtiene el usuario actual desde el header X-User-Email del request HTTP.
     * En la Fase 3 (JWT), se reemplazará por SecurityContextHolder.getContext().getAuthentication().
     */


    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }

    /**
     * Soporta integración con Dev 6/Auth:
     * - ADMIN_PLATAFORMA tiene acceso global a compartidos.
     * - Header opcional X-User-Permissions habilita lectura de compartidos.
     */
    private boolean puedeVerProcesosCompartidos(Usuario usuarioActual) {
        if (usuarioActual.getRolSistema() == RolSistema.ADMIN_PLATAFORMA) {
            return true;
        }

        String permisosHeader = httpServletRequest.getHeader("X-User-Permissions");
        if (permisosHeader == null || permisosHeader.isBlank()) {
            return false;
        }

        Set<String> permisos = Arrays.stream(permisosHeader.split(","))
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .map(p -> p.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return permisos.contains("PROCESO_COMPARTIDO_VER")
                || permisos.contains("PROCESOS_COMPARTIDOS_VER")
                || permisos.contains("SHARED_PROCESS_READ")
                || permisos.contains("POOL_SHARED_READ");
    }

    @Override
    @Transactional(readOnly = true)
    public Proceso obtenerEntityById(Long id) {
        return procesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PROCESO_NO_ENCONTRADO + id));
    }
}
