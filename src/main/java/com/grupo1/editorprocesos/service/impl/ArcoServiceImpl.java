package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.ArcoDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.bpmn.ElementoBpmn;
import com.grupo1.editorprocesos.model.entity.bpmn.Gateway;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.repository.GatewayRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.ArcoService;
import com.grupo1.editorprocesos.service.PermisosPoolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArcoServiceImpl implements ArcoService {

    private static final String ARCO_NO_ENCONTRADO = "Arco no encontrado con ID: ";

    private final ArcoRepository arcoRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final ActividadRepository actividadRepository;
    private final GatewayRepository gatewayRepository;
    private final HttpServletRequest httpServletRequest;
    private final PermisosPoolService permisosPoolService;

    // =====================================================================================
    // HU-11: Crear Arco
    // =====================================================================================

    @Override
    @Transactional
    public ArcoDTO crearArco(ArcoDTO arcoDTO) {
        // 1. Validar que el proceso exista
        Proceso proceso = procesoRepository.findById(arcoDTO.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + arcoDTO.getProcesoId()));

        // 2. Validar que el usuario actual pertenezca a la empresa del proceso
        Usuario usuarioActual = obtenerUsuarioActual();
        Empresa empresa = proceso.getPool().getEmpresa();
        validarUsuarioPertenecAEmpresa(usuarioActual, empresa);
        permisosPoolService.validarPermisoEscritura(usuarioActual);

        // 3. Validar que los elementos origen y destino existan dentro del mismo proceso
        validarElementoExisteEnProceso(arcoDTO.getOrigenId(), proceso.getId());
        validarElementoExisteEnProceso(arcoDTO.getDestinoId(), proceso.getId());

        // 4. Validar que origen y destino no sean el mismo elemento
        if (arcoDTO.getOrigenId().equals(arcoDTO.getDestinoId())) {
            throw new IllegalArgumentException("El origen y destino del arco no pueden ser el mismo elemento");
        }

        // 5. Validar que no exista ya un arco entre estos elementos
        if (arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId(arcoDTO.getOrigenId(), arcoDTO.getDestinoId(), proceso.getId()).isPresent()) {
            throw new DuplicateResourceException("Ya existe un arco entre '" + arcoDTO.getOrigenId() + "' y '" + arcoDTO.getDestinoId() + "' en este proceso");
        }

        // 4. Crear la entidad Arco
        Arco arco = new Arco();
        arco.setOrigenId(arcoDTO.getOrigenId());
        arco.setDestinoId(arcoDTO.getDestinoId());
        arco.setProceso(proceso);

        // 5. Persistir
        Arco guardado = arcoRepository.save(arco);

        // 6. Registrar en historial
        registrarHistorial(proceso, usuarioActual,
                "Arco creado: Origen \"" + guardado.getOrigenId()
                        + "\" → Destino \"" + guardado.getDestinoId() + "\"");

        return convertirADTO(guardado);
    }

    // =====================================================================================
    // HU-12: Editar Arco
    // =====================================================================================

    @Override
    @Transactional
    public ArcoDTO editarArco(Long id, ArcoDTO arcoDTO) {
        // 1. Buscar arco existente
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ARCO_NO_ENCONTRADO + id));

        // 2. Validar pertenencia a empresa
        Proceso proceso = arco.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());
        permisosPoolService.validarPermisoEscritura(usuarioActual);

        // 3. Rastrear cambios
        StringBuilder cambios = new StringBuilder("Arco editado (ID: " + id + "): ");
        boolean huboCambios = false;

        if (arcoDTO.getOrigenId() != null && !arcoDTO.getOrigenId().equals(arco.getOrigenId())) {
            // Validar que el nuevo origen exista en el proceso
            validarElementoExisteEnProceso(arcoDTO.getOrigenId(), proceso.getId());
            cambios.append("Origen: \"").append(arco.getOrigenId())
                    .append("\" → \"").append(arcoDTO.getOrigenId()).append("\". ");
            arco.setOrigenId(arcoDTO.getOrigenId());
            huboCambios = true;
        }

        if (arcoDTO.getDestinoId() != null && !arcoDTO.getDestinoId().equals(arco.getDestinoId())) {
            // Validar que el nuevo destino exista en el proceso
            validarElementoExisteEnProceso(arcoDTO.getDestinoId(), proceso.getId());
            cambios.append("Destino: \"").append(arco.getDestinoId())
                    .append("\" → \"").append(arcoDTO.getDestinoId()).append("\". ");
            arco.setDestinoId(arcoDTO.getDestinoId());
            huboCambios = true;
        }

        // Validar que origen y destino no sean el mismo elemento
        if (arco.getOrigenId().equals(arco.getDestinoId())) {
            throw new IllegalArgumentException("El origen y destino del arco no pueden ser el mismo elemento");
        }

        // Validar unicidad si cambió origen o destino
        if (huboCambios && arcoRepository.findByOrigenIdAndDestinoIdAndProcesoId(arco.getOrigenId(), arco.getDestinoId(), proceso.getId()).isPresent()) {
            throw new DuplicateResourceException("Ya existe un arco entre '" + arco.getOrigenId() + "' y '" + arco.getDestinoId() + "' en este proceso");
        }

        // 4. Guardar cambios
        Arco actualizado = arcoRepository.save(arco);

        // 5. Registrar historial si hubo cambios
        if (huboCambios) {
            registrarHistorial(proceso, usuarioActual, cambios.toString());
        }

        return convertirADTO(actualizado);
    }

    // =====================================================================================
    // Consultas de lectura
    // =====================================================================================

    @Override
    @Transactional(readOnly = true)
    public ArcoDTO obtenerArcoPorId(Long id) {
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ARCO_NO_ENCONTRADO + id));
        return convertirADTO(arco);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArcoDTO> listarArcosPorProceso(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return arcoRepository.findByProcesoId(procesoId).stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarArco(Long id) {
        Arco arco = arcoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ARCO_NO_ENCONTRADO + id));

        Proceso proceso = arco.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        arcoRepository.delete(arco);

        registrarHistorial(proceso, usuarioActual,
                "Arco eliminado: Origen \"" + arco.getOrigenId()
                        + "\" → Destino \"" + arco.getDestinoId() + "\"");
    }

    // ===== Métodos privados de utilidad =====

    private ArcoDTO convertirADTO(Arco arco) {
        ArcoDTO dto = new ArcoDTO();
        dto.setId(arco.getId());
        dto.setOrigenId(arco.getOrigenId());
        dto.setDestinoId(arco.getDestinoId());
        dto.setProcesoId(arco.getProceso().getId());
        return dto;
    }

    private void registrarHistorial(Proceso proceso, Usuario usuario, String cambio) {
        HistorialCambios historial = new HistorialCambios();
        historial.setProceso(proceso);
        historial.setUsuarioId(usuario.getId());
        historial.setFecha(LocalDateTime.now());
        historial.setCambio(cambio);
        historialCambiosRepository.save(historial);
    }

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

    /**
     * Valida que un elemento con el nombre dado exista dentro del proceso especificado.
     * Los elementos pueden ser Actividades o Gateways.
     */
    private void validarElementoExisteEnProceso(String elementoNombre, Long procesoId) {
        // Verificar si es una Actividad
        boolean existeActividad = actividadRepository.findByNombreAndProcesoId(elementoNombre, procesoId).isPresent();

        if (existeActividad) {
            return;
        }

        // Verificar si es un Gateway
        boolean existeGateway = gatewayRepository.findByNombreAndProcesoId(elementoNombre, procesoId).isPresent();

        if (!existeGateway) {
            throw new IllegalArgumentException(
                    "Elemento con nombre '" + elementoNombre + "' no encontrado en el proceso con ID: " + procesoId);
        }
    }
}