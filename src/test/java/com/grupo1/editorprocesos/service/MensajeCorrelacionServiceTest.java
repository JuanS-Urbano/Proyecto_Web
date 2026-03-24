package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.CorrelacionResultDTO;
import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.core.Empresa;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.model.entity.core.Pool;
import com.grupo1.editorprocesos.model.enums.EstadoMensaje;
import com.grupo1.editorprocesos.model.enums.TipoMensaje;
import com.grupo1.editorprocesos.repository.ActividadRepository;
import com.grupo1.editorprocesos.repository.MensajeRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import com.grupo1.editorprocesos.repository.UsuarioRepository;
import com.grupo1.editorprocesos.service.impl.MensajeServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HU-28 (Dev 3): Correlación de Mensajes")
class MensajeCorrelacionServiceTest {

    @Mock private MensajeRepository mensajeRepository;
    @Mock private ProcesoRepository procesoRepository;
    @Mock private ActividadRepository actividadRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HttpServletRequest httpServletRequest;

    @InjectMocks
    private MensajeServiceImpl mensajeService;

    private Mensaje throwMsg;
    private Mensaje catchMsg;
    private static final String CORRELATION_KEY = "order-12345";

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Pool pool = new Pool();
        pool.setId(1L);
        pool.setEmpresa(empresa);

        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setPool(pool);

        throwMsg = new Mensaje();
        throwMsg.setId(10L);
        throwMsg.setNombre("msgPago");
        throwMsg.setTipo(TipoMensaje.THROW);
        throwMsg.setEstado(EstadoMensaje.PENDIENTE);
        throwMsg.setCorrelationKey(CORRELATION_KEY);
        throwMsg.setPayloadJson("{\"monto\":100}");
        throwMsg.setProceso(proceso);

        catchMsg = new Mensaje();
        catchMsg.setId(20L);
        catchMsg.setNombre("msgPago");
        catchMsg.setTipo(TipoMensaje.CATCH);
        catchMsg.setEstado(EstadoMensaje.PENDIENTE);
        catchMsg.setCorrelationKey(CORRELATION_KEY);
        catchMsg.setProcesoDestinoId(2L);
        catchMsg.setProceso(proceso);
    }

    @Nested
    @DisplayName("correlateMessages()")
    class CorrelateMessages {

        @Test
        @DisplayName("Correlación exitosa: THROW + CATCH pendientes → ambos ENTREGADO")
        void correlateMessages_exitoso() {
            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.of(throwMsg));
            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.CATCH, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.of(catchMsg));
            when(mensajeRepository.save(any(Mensaje.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CorrelacionResultDTO result = mensajeService.correlateMessages(CORRELATION_KEY);

            assertThat(result.isCorrelacionExitosa()).isTrue();
            assertThat(result.getThrowMensaje()).isNotNull();
            assertThat(result.getCatchMensaje()).isNotNull();
            assertThat(throwMsg.getEstado()).isEqualTo(EstadoMensaje.ENTREGADO);
            assertThat(catchMsg.getEstado()).isEqualTo(EstadoMensaje.ENTREGADO);
            verify(mensajeRepository, times(2)).save(any(Mensaje.class));
        }

        @Test
        @DisplayName("Sin THROW pendiente → ResourceNotFoundException")
        void correlateMessages_sinThrow_falla() {
            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> mensajeService.correlateMessages(CORRELATION_KEY))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("THROW pendiente");
        }

        @Test
        @DisplayName("Sin CATCH pendiente → ResourceNotFoundException")
        void correlateMessages_sinCatch_falla() {
            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.of(throwMsg));
            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.CATCH, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> mensajeService.correlateMessages(CORRELATION_KEY))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("CATCH pendiente");
        }

        @Test
        @DisplayName("Key vacía → IllegalArgumentException")
        void correlateMessages_keyVacia_falla() {
            assertThatThrownBy(() -> mensajeService.correlateMessages(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("correlationKey es requerido");
        }

        @Test
        @DisplayName("Key null → IllegalArgumentException")
        void correlateMessages_keyNull_falla() {
            assertThatThrownBy(() -> mensajeService.correlateMessages(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("correlationKey es requerido");
        }

        @Test
        @DisplayName("Correlación copia payload del THROW al CATCH")
        void correlateMessages_copiaPayload_alCatch() {
            catchMsg.setPayloadJson(null); // CATCH sin payload

            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.THROW, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.of(throwMsg));
            when(mensajeRepository.findFirstByCorrelationKeyAndTipoAndEstado(
                    CORRELATION_KEY, TipoMensaje.CATCH, EstadoMensaje.PENDIENTE))
                    .thenReturn(Optional.of(catchMsg));
            when(mensajeRepository.save(any(Mensaje.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            mensajeService.correlateMessages(CORRELATION_KEY);

            // El CATCH debe tener ahora el payload del THROW
            assertThat(catchMsg.getPayloadJson()).isEqualTo("{\"monto\":100}");
        }
    }

    @Nested
    @DisplayName("buscarPorCorrelationKey()")
    class BuscarPorCorrelationKey {

        @Test
        @DisplayName("Retorna mensajes con el correlationKey dado")
        void buscarPorCorrelationKey_exitoso() {
            when(mensajeRepository.findByCorrelationKey(CORRELATION_KEY))
                    .thenReturn(List.of(throwMsg, catchMsg));

            List<MensajeDTO> result = mensajeService.buscarPorCorrelationKey(CORRELATION_KEY);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Sin resultados retorna lista vacía")
        void buscarPorCorrelationKey_sinResultados() {
            when(mensajeRepository.findByCorrelationKey("inexistente"))
                    .thenReturn(Collections.emptyList());

            List<MensajeDTO> result = mensajeService.buscarPorCorrelationKey("inexistente");

            assertThat(result).isEmpty();
        }
    }
}
