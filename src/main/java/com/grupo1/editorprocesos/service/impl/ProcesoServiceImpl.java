package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.ProcesoDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoProceso;
import com.grupo1.editorprocesos.repository.EmpresaRepository;
import com.grupo1.editorprocesos.repository.PoolRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.ProcesoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcesoServiceImpl implements ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final PoolRepository poolRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    /**
     * Crea un nuevo proceso validando que esté asociado correctamente a la
     * empresa/pool del usuario.
     * 
     * Validaciones realizadas:
     * 1. El pool especificado existe
     * 2. La empresa del pool existe
     * 3. El usuario actual pertenece a la empresa del pool
     * 
     * El proceso se crea en estado BORRADOR por defecto y se asocia al pool
     * indicado.
     */
    @Override
    @Transactional
    public ProcesoDTO crearProceso(ProcesoDTO procesoDTO) {
        // Validar que el poolId sea proporcionado
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

        // Validar que el usuario pertenezca a la empresa
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        // Actualizar campos
        if (procesoDTO.getNombre() != null) {
            proceso.setNombre(procesoDTO.getNombre());
        }
        if (procesoDTO.getDescripcion() != null) {
            proceso.setDescripcion(procesoDTO.getDescripcion());
        }
        if (procesoDTO.getCategoria() != null) {
            proceso.setCategoria(procesoDTO.getCategoria());
        }
        if (procesoDTO.getEstado() != null) {
            proceso.setEstado(procesoDTO.getEstado());
        }
        if (procesoDTO.getConfiguracionCompartido() != null) {
            proceso.setConfiguracionCompartido(procesoDTO.getConfiguracionCompartido());
        }

        Proceso procesoActualizado = procesoRepository.save(proceso);

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

        // Validar que el usuario pertenezca a la empresa
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        // Eliminación lógica: cambiar estado a INACTIVO
        proceso.setEstado(EstadoProceso.INACTIVO);
        procesoRepository.save(proceso);
    }

    /**
     * Obtiene el usuario actual desde el contexto de la aplicación.
     * 
     * Nota: Esta es una implementación temporal. En la Fase 3 (Seguridad),
     * se reemplazará por la obtención del usuario desde el JWT en el
     * SecurityContext.
     */
    private Usuario obtenerUsuarioActual() {
        // Implementación temporal: retornar el primer usuario activo
        // En fase 3, se obtendrá del JWT/SecurityContext
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException(
                        "No hay usuario autenticado en la sesión"));
    }

    /**
     * Valida que el usuario pertenezca a la empresa especificada.
     * 
     * @param usuario Usuario a validar
     * @param empresa Empresa a la que debe pertenecer
     * @throws UnauthorizedException si el usuario no pertenece a la empresa
     */
    private void validarUsuarioPertenecAEmpresa(Usuario usuario, Empresa empresa) {
        if (usuario.getEmpresa() == null
                || !usuario.getEmpresa().getId().equals(empresa.getId())) {
            throw new UnauthorizedException(
                    "El usuario no tiene acceso a la empresa con ID: " + empresa.getId());
        }
    }
}
