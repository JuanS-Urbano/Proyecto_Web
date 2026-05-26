package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.exception.MensajeCatchException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.exception.UnauthorizedException;
import com.grupo1.editorprocesos.model.entity.bpmn.Actividad;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.entity.core.Usuario;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.MensajeRepository;
import com.grupo1.editorprocesos.service.impl.MensajeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensajeServiceImplTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private ProcesoService procesoService;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private com.grupo1.editorprocesos.service.UsuarioActualService usuarioActualService;


    @InjectMocks
    private MensajeServiceImpl mensajeService;

    private Empresa empresa;
    private Pool pool;
    private Proceso proceso;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);

        pool = new Pool();
        pool.setId(1L);
        pool.setEmpresa(empresa);

        proceso = new Proceso();
        proceso.setId(10L);
        proceso.setPool(pool);

        usuario = new Usuario();
        usuario.setId(99L);
        usuario.setEmail("user@empresa.com");
        usuario.setEmpresa(empresa);

        Mockito.lenient().when(usuarioActualService.obtenerUsuarioActual()).thenReturn(usuario);
    }

    // =====================================================================================
    // throwMessage tests
    // =====================================================================================

    @Test
    void throwMessage_exitoso_sinPayload() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        Mensaje guardado = new Mensaje();
        guardado.setId(1L);
        guardado.setNombre("Orden Aprobada");
        guardado.setTipo(TipoMensaje.THROW);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(proceso);
        when(mensajeRepository.save(any())).thenReturn(guardado);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Orden Aprobada");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));

        MensajeDTO result = mensajeService.throwMessage(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Orden Aprobada");
        assertThat(result.getTipo()).isEqualTo(TipoMensaje.THROW);
        assertThat(result.getEstado()).isEqualTo(EstadoMensaje.PENDIENTE);
        assertThat(result.getProceso().getId()).isEqualTo(proceso.getId());
    }

    @Test
    void throwMessage_conPayloadJSON_valido() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        String jsonPayload = "{\"orderId\": 123, \"total\": 500.00}";

        Mensaje guardado = new Mensaje();
        guardado.setId(2L);
        guardado.setNombre("Pago Procesado");
        guardado.setPayloadJson(jsonPayload);
        guardado.setTipo(TipoMensaje.THROW);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(proceso);
        when(mensajeRepository.save(any())).thenReturn(guardado);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Pago Procesado");
        dto.setPayloadJson(jsonPayload);
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));

        MensajeDTO result = mensajeService.throwMessage(dto);

        assertThat(result.getPayloadJson()).isEqualTo(jsonPayload);
    }

    @Test
    void throwMessage_conPayloadJSON_invalido_falla() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Mensaje Malo");
        dto.setPayloadJson("esto no es json");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON válido");
    }

    @Test
    void throwMessage_procesoNoExiste_falla() {
        when(procesoService.obtenerEntityById(999L)).thenThrow(new ResourceNotFoundException("Proceso no encontrado"));

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Mensaje");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(999L, null));

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proceso no encontrado");
    }

    @Test
    void throwMessage_sinHeaderUsuario_falla() {
        when(usuarioActualService.obtenerUsuarioActual()).thenThrow(new UnauthorizedException("No se proporcionó el header X-User-Email para identificar al usuario"));
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Mensaje");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void throwMessage_nombreVacio_falla() {
        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del mensaje");
    }

    @Test
    void throwMessage_conActividadOrigen_exitoso() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(proceso);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        Mensaje guardado = new Mensaje();
        guardado.setId(3L);
        guardado.setNombre("Notificación");
        guardado.setTipo(TipoMensaje.THROW);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(proceso);
        guardado.setActividadOrigen(actividad);
        when(mensajeRepository.save(any())).thenReturn(guardado);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Notificación");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));
        dto.setActividadOrigen(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));

        MensajeDTO result = mensajeService.throwMessage(dto);

        assertThat(result.getActividadOrigen().getId()).isEqualTo(5L);
    }

    @Test
    void throwMessage_conActividadDeOtroProceso_falla() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        Proceso otroProceso = new Proceso();
        otroProceso.setId(999L);

        Actividad actividad = new Actividad();
        actividad.setId(5L);
        actividad.setProceso(otroProceso);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actividad));

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Notificación");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));
        dto.setActividadOrigen(new com.grupo1.editorprocesos.dto.ReferenciaDTO(5L, null));

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece al proceso");
    }

    // =====================================================================================
    // listar y obtener tests
    // =====================================================================================

    @Test
    void listarMensajesPorProceso_exitoso() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);

        Mensaje m1 = new Mensaje();
        m1.setId(1L);
        m1.setNombre("Msg1");
        m1.setTipo(TipoMensaje.THROW);
        m1.setEstado(EstadoMensaje.PENDIENTE);
        m1.setProceso(proceso);

        Mensaje m2 = new Mensaje();
        m2.setId(2L);
        m2.setNombre("Msg2");
        m2.setTipo(TipoMensaje.THROW);
        m2.setEstado(EstadoMensaje.ENTREGADO);
        m2.setProceso(proceso);

        when(mensajeRepository.findByProcesoId(proceso.getId())).thenReturn(List.of(m1, m2));

        List<MensajeDTO> result = mensajeService.listarMensajesPorProceso(proceso.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNombre()).isEqualTo("Msg1");
        assertThat(result.get(1).getNombre()).isEqualTo("Msg2");
    }

    @Test
    void obtenerMensajePorId_exitoso() {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(1L);
        mensaje.setNombre("Test Msg");
        mensaje.setTipo(TipoMensaje.THROW);
        mensaje.setEstado(EstadoMensaje.PENDIENTE);
        mensaje.setProceso(proceso);
        when(mensajeRepository.findById(1L)).thenReturn(Optional.of(mensaje));

        MensajeDTO result = mensajeService.obtenerMensajePorId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Test Msg");
    }

    @Test
    void obtenerMensajePorId_noExiste_falla() {
        when(mensajeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensajeService.obtenerMensajePorId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mensaje no encontrado");
    }

        @Test
        void throwMessage_actividadOrigenNoExiste_falla() {
        when(procesoService.obtenerEntityById(proceso.getId())).thenReturn(proceso);
        when(actividadRepository.findById(123L)).thenReturn(Optional.empty());

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("Notificación");
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(proceso.getId(), null));
        dto.setActividadOrigen(new com.grupo1.editorprocesos.dto.ReferenciaDTO(123L, null));

        assertThatThrownBy(() -> mensajeService.throwMessage(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Actividad origen no encontrada");
        }

        @Test
        void catchMessage_exitoso_conProcesoOrigen() {
        Proceso destino = new Proceso();
        destino.setId(20L);
        destino.setPool(pool);

        Proceso origen = new Proceso();
        origen.setId(21L);
        origen.setPool(pool);

        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("MsgCatch");
        dto.setCorrelationKey("ORD-1");
        dto.setProcesoDestino(new com.grupo1.editorprocesos.dto.ReferenciaDTO(20L, null));
        dto.setProceso(new com.grupo1.editorprocesos.dto.ReferenciaDTO(21L, null));

        Mensaje guardado = new Mensaje();
        guardado.setId(10L);
        guardado.setNombre("MsgCatch");
        guardado.setTipo(TipoMensaje.CATCH);
        guardado.setEstado(EstadoMensaje.PENDIENTE);
        guardado.setProceso(origen);
        guardado.setProcesoDestinoId(20L);

        when(procesoService.obtenerEntityById(20L)).thenReturn(destino);
        when(procesoService.obtenerEntityById(21L)).thenReturn(origen);
        when(mensajeRepository.existsByNombreAndTipoAndProcesoDestinoId("MsgCatch", TipoMensaje.CATCH, 20L)).thenReturn(false);
        when(mensajeRepository.save(any(Mensaje.class))).thenReturn(guardado);

        MensajeDTO result = mensajeService.catchMessage(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getProcesoDestino().getId()).isEqualTo(20L);
        assertThat(result.getTipo()).isEqualTo(TipoMensaje.CATCH);
        }

        @Test
        void catchMessage_duplicado_falla() {
        MensajeDTO dto = new MensajeDTO();
        dto.setNombre("MsgCatch");
        dto.setProcesoDestino(new com.grupo1.editorprocesos.dto.ReferenciaDTO(20L, null));

        when(procesoService.obtenerEntityById(20L)).thenReturn(proceso);
        when(mensajeRepository.existsByNombreAndTipoAndProcesoDestinoId("MsgCatch", TipoMensaje.CATCH, 20L)).thenReturn(true);

        assertThatThrownBy(() -> mensajeService.catchMessage(dto))
            .isInstanceOf(MensajeCatchException.class)
            .hasMessageContaining("Ya existe un mensaje CATCH");
        }

        @Test
        void correlateMessages_exitoso() {
        Mensaje throwMsg = new Mensaje();
        throwMsg.setId(1L);
        throwMsg.setNombre("T");
        throwMsg.setTipo(TipoMensaje.THROW);
        throwMsg.setEstado(EstadoMensaje.PENDIENTE);
        throwMsg.setPayloadJson("{\"x\":1}");
        throwMsg.setCorrelationKey("K1");
        throwMsg.setProceso(proceso);

        Mensaje catchMsg = new Mensaje();
        catchMsg.setId(2L);
        catchMsg.setNombre("C");
        catchMsg.setTipo(TipoMensaje.CATCH);
        catchMsg.setEstado(EstadoMensaje.PENDIENTE);
        catchMsg.setCorrelationKey("K1");
        catchMsg.setProceso(proceso);
        catchMsg.setProcesoDestinoId(proceso.getId());

        when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado("K1", TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
            .thenReturn(Optional.of(throwMsg));
        when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado("K1", TipoMensaje.CATCH, EstadoMensaje.PENDIENTE))
            .thenReturn(Optional.of(catchMsg));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> inv.getArgument(0));

        com.grupo1.editorprocesos.dto.CorrelacionResultDTO result = mensajeService.correlateMessages("K1");

        assertThat(result.isCorrelacionExitosa()).isTrue();
        assertThat(result.getThrowMensaje().getEstado()).isEqualTo(EstadoMensaje.ENTREGADO);
        assertThat(result.getCatchMensaje().getEstado()).isEqualTo(EstadoMensaje.ENTREGADO);
        assertThat(result.getCatchMensaje().getPayloadJson()).isEqualTo("{\"x\":1}");
        }

        @Test
        void correlateMessages_sinThrow_falla() {
        when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado("K2", TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensajeService.correlateMessages("K2"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("THROW pendiente");
        }

        @Test
        void correlateMessages_sinCatch_falla() {
        Mensaje throwMsg = new Mensaje();
        throwMsg.setId(1L);
        throwMsg.setTipo(TipoMensaje.THROW);
        throwMsg.setEstado(EstadoMensaje.PENDIENTE);

        when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado("K3", TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
            .thenReturn(Optional.of(throwMsg));
        when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado("K3", TipoMensaje.CATCH, EstadoMensaje.PENDIENTE))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensajeService.correlateMessages("K3"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("CATCH pendiente");
        }

        @Test
        void buscarPorCorrelationKey_exitoso() {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(7L);
        mensaje.setNombre("M");
        mensaje.setCorrelationKey("ABC");
        mensaje.setProceso(proceso);

        when(mensajeRepository.findByCorrelationKey("ABC")).thenReturn(List.of(mensaje));

        List<MensajeDTO> result = mensajeService.buscarPorCorrelationKey("ABC");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCorrelationKey()).isEqualTo("ABC");
        }

        @Test
        void buscarPorCorrelationKey_vacio_falla() {
        assertThatThrownBy(() -> mensajeService.buscarPorCorrelationKey(" "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("correlationKey es requerido");
        }

        @Test
        void listarMensajesPorProceso_usuarioSinAcceso_falla() {
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);

        Usuario usuarioOtro = new Usuario();
        usuarioOtro.setId(100L);
        usuarioOtro.setEmail("otro@empresa.com");
        usuarioOtro.setEmpresa(otraEmpresa);

        when(usuarioActualService.obtenerUsuarioActual()).thenReturn(usuarioOtro);
        Long procesoId = proceso.getId();
        when(procesoService.obtenerEntityById(procesoId)).thenReturn(proceso);

        assertThatThrownBy(() -> mensajeService.listarMensajesPorProceso(procesoId))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("no tiene acceso");
        }
}
