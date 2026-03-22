package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.GatewayDTO;
import com.grupo1.editorprocesos.exception.DuplicateResourceException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Arco;
import com.grupo1.editorprocesos.model.entity.bpmn.Gateway;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.process.HistorialCambios;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.ArcoRepository;
import com.grupo1.editorprocesos.repository.GatewayRepository;
import com.grupo1.editorprocesos.repository.HistorialCambiosRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.GatewayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GatewayServiceImpl implements GatewayService {

    private static final String GATEWAY_NO_ENCONTRADO = "Gateway no encontrado con ID: ";

    private final GatewayRepository gatewayRepository;
    private final ProcesoRepository procesoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialCambiosRepository historialCambiosRepository;
    private final ArcoRepository arcoRepository;
    private final HttpServletRequest httpServletRequest;

    // =====================================================================================
    // HU-15: Crear Gateway
    // =====================================================================================

    @Override
    @Transactional
    public GatewayDTO crearGateway(GatewayDTO gatewayDTO) {
        // 1. Validar que el proceso exista
        Proceso proceso = procesoRepository.findById(gatewayDTO.getProcesoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + gatewayDTO.getProcesoId()));

        // 2. Validar usuario y empresa
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        // 3. Validar tipo de gateway
        if (gatewayDTO.getTipoGateway() == null) {
            throw new IllegalArgumentException("El tipo de gateway es requerido");
        }

        // 4. Validar nombre único dentro del proceso (compartido con actividades)
        if (gatewayRepository.findByNombreAndProcesoId(gatewayDTO.getNombre(), proceso.getId()).isPresent()) {
            throw new DuplicateResourceException(
                    "Ya existe un gateway con el nombre '" + gatewayDTO.getNombre() + "' en este proceso");
        }

        // 5. Crear la entidad Gateway
        Gateway gateway = new Gateway();
        gateway.setNombre(gatewayDTO.getNombre());
        gateway.setTipoGateway(gatewayDTO.getTipoGateway());
        gateway.setPosicionX(gatewayDTO.getPosicionX());
        gateway.setPosicionY(gatewayDTO.getPosicionY());
        gateway.setProceso(proceso);

        // 6. Persistir
        Gateway guardado = gatewayRepository.save(gateway);

        // 7. Registrar en historial
        registrarHistorial(proceso, usuarioActual,
                "Gateway creado: \"" + guardado.getNombre()
                        + "\" (Tipo: " + guardado.getTipoGateway() + ")");

        return convertirADTO(guardado);
    }

    // =====================================================================================
    // HU-15: Editar Gateway
    // =====================================================================================

    @Override
    @Transactional
    public GatewayDTO editarGateway(Long id, GatewayDTO gatewayDTO) {
        // 1. Buscar gateway existente
        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        GATEWAY_NO_ENCONTRADO + id));

        // 2. Validar usuario y empresa
        Proceso proceso = gateway.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        // 3. Rastrear cambios
        StringBuilder cambios = new StringBuilder("Gateway editado (ID: " + id + "): ");
        boolean huboCambios = false;

        if (gatewayDTO.getNombre() != null && !gatewayDTO.getNombre().equals(gateway.getNombre())) {
            // Validar unicidad del nuevo nombre en el proceso
            if (gatewayRepository.findByNombreAndProcesoId(gatewayDTO.getNombre(), proceso.getId()).isPresent()) {
                throw new DuplicateResourceException(
                        "Ya existe un gateway con el nombre '" + gatewayDTO.getNombre() + "' en este proceso");
            }
            cambios.append("Nombre: \"").append(gateway.getNombre())
                    .append("\" → \"").append(gatewayDTO.getNombre()).append("\". ");
            gateway.setNombre(gatewayDTO.getNombre());
            huboCambios = true;
        }

        if (gatewayDTO.getTipoGateway() != null && !gatewayDTO.getTipoGateway().equals(gateway.getTipoGateway())) {
            cambios.append("Tipo: ").append(gateway.getTipoGateway())
                    .append(" → ").append(gatewayDTO.getTipoGateway()).append(". ");
            gateway.setTipoGateway(gatewayDTO.getTipoGateway());
            huboCambios = true;
        }

        if (gatewayDTO.getPosicionX() != null && !gatewayDTO.getPosicionX().equals(gateway.getPosicionX())) {
            cambios.append("PosicionX: ").append(gateway.getPosicionX())
                    .append(" → ").append(gatewayDTO.getPosicionX()).append(". ");
            gateway.setPosicionX(gatewayDTO.getPosicionX());
            huboCambios = true;
        }

        if (gatewayDTO.getPosicionY() != null && !gatewayDTO.getPosicionY().equals(gateway.getPosicionY())) {
            cambios.append("PosicionY: ").append(gateway.getPosicionY())
                    .append(" → ").append(gatewayDTO.getPosicionY()).append(". ");
            gateway.setPosicionY(gatewayDTO.getPosicionY());
            huboCambios = true;
        }

        // 4. Guardar cambios
        Gateway actualizado = gatewayRepository.save(gateway);

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
    public GatewayDTO obtenerGatewayPorId(Long id) {
        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        GATEWAY_NO_ENCONTRADO + id));
        return convertirADTO(gateway);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayDTO> listarGatewaysPorProceso(Long procesoId) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proceso no encontrado con ID: " + procesoId));

        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        return gatewayRepository.findByProcesoId(procesoId).stream()
                .map(this::convertirADTO)
                .toList();
    }

    // =====================================================================================
    // HU-16: Eliminar Gateway — con saneamiento del grafo
    // =====================================================================================

    @Override
    @Transactional
    public void eliminarGateway(Long id) {
        // 1. Validar existencia del gateway
        Gateway gateway = gatewayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        GATEWAY_NO_ENCONTRADO + id));

        // 2. Validar usuario y empresa
        Proceso proceso = gateway.getProceso();
        Usuario usuarioActual = obtenerUsuarioActual();
        validarUsuarioPertenecAEmpresa(usuarioActual, proceso.getPool().getEmpresa());

        String nombreGateway = gateway.getNombre();
        Long procesoId = proceso.getId();

        // 3. Buscar arcos conectados al gateway dentro del mismo proceso
        List<Arco> arcosEntrantes = arcoRepository.findByDestinoIdAndProcesoId(nombreGateway, procesoId);
        List<Arco> arcosSalientes = arcoRepository.findByOrigenIdAndProcesoId(nombreGateway, procesoId);

        StringBuilder detalleHistorial = new StringBuilder(
                "Gateway eliminado: \"" + nombreGateway + "\" (Tipo: " + gateway.getTipoGateway() + "). ");

        // 4. Aplicar regla de saneamiento del grafo
        // Regla: si exactamente 1 entrante y 1 saliente → reconectar predecesor con sucesor
        // Si hay múltiples o ninguno → eliminar todos los arcos sin reconectar (dejar huérfanos)
        if (arcosEntrantes.size() == 1 && arcosSalientes.size() == 1) {
            String origenPredeces = arcosEntrantes.get(0).getOrigenId();
            String destinoSucesor = arcosSalientes.get(0).getDestinoId();

            // Eliminar los dos arcos conectados al gateway
            arcoRepository.delete(arcosEntrantes.get(0));
            arcoRepository.delete(arcosSalientes.get(0));

            // Verificar que no exista ya ese arco para evitar duplicado
            boolean yaExiste = arcoRepository
                    .findByOrigenIdAndDestinoIdAndProcesoId(origenPredeces, destinoSucesor, procesoId)
                    .isPresent();

            if (!yaExiste) {
                Arco nuevoArco = new Arco();
                nuevoArco.setOrigenId(origenPredeces);
                nuevoArco.setDestinoId(destinoSucesor);
                nuevoArco.setProceso(proceso);
                arcoRepository.save(nuevoArco);
                detalleHistorial.append("Arcos saneados: 2 eliminados, reconexión creada \"")
                        .append(origenPredeces).append("\" → \"").append(destinoSucesor).append("\".");
            } else {
                detalleHistorial.append("Arcos saneados: 2 eliminados. Reconexión omitida (arco ya existe).");
            }
        } else {
            // Múltiples entrantes/salientes: eliminar todos sin reconectar
            int totalArcos = arcosEntrantes.size() + arcosSalientes.size();
            arcoRepository.deleteAll(arcosEntrantes);
            arcoRepository.deleteAll(arcosSalientes);
            detalleHistorial.append("Arcos huérfanos: ").append(totalArcos)
                    .append(" arcos conectados eliminados sin reconexión (múltiples ramas).");
        }

        // 5. Eliminar el gateway
        gatewayRepository.delete(gateway);

        // 6. Registrar en historial
        registrarHistorial(proceso, usuarioActual, detalleHistorial.toString());
    }

    // ===== Métodos privados de utilidad =====

    private GatewayDTO convertirADTO(Gateway gateway) {
        GatewayDTO dto = new GatewayDTO();
        dto.setId(gateway.getId());
        dto.setNombre(gateway.getNombre());
        dto.setTipoGateway(gateway.getTipoGateway());
        dto.setPosicionX(gateway.getPosicionX());
        dto.setPosicionY(gateway.getPosicionY());
        dto.setProcesoId(gateway.getProceso().getId());
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
}
