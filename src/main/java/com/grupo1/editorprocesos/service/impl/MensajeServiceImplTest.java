package com.grupo1.editorprocesos.service.impl;

import com.grupo1.editorprocesos.dto.MensajeDTO;
import com.grupo1.editorprocesos.exception.MensajeCatchException;
import com.grupo1.editorprocesos.exception.ResourceNotFoundException;
import com.grupo1.editorprocesos.model.entity.message.Mensaje;
import com.grupo1.editorprocesos.model.entity.process.Proceso;
import com.grupo1.editorprocesos.repository.MensajeRepository;
import com.grupo1.editorprocesos.repository.ProcesoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de MensajeServiceImpl.catchMessage() — HU-27.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MensajeServiceImpl — HU-27 catchMessage()")
class MensajeServiceImplTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @Mock
    private ProcesoRepository procesoRepository;

    @InjectMocks
    private MensajeServiceImpl mensajeService;

    private MensajeDTO dtoCorrecto;
    private Proceso procesoDestino;

    @BeforeEach
    void setUp() {
        dtoCorrecto = new MensajeDTO();
        dtoCorrecto.setNombre("msgPagoAprobado");
        dtoCorrecto.setTipo("CATCH");
        dtoCorrecto.setProcesoOrigenId(1L);
        dtoCorrecto.setProcesoDestinoId(2L);
        dtoCorrecto.setPayloadJson(null);

        procesoDestino = new Proceso();
        procesoDestino.setId(2L);
        procesoDestino.setNombre("Proceso Receptor");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Escenarios exitosos
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Correlación exitosa")
    class CatchExitoso {

        @BeforeEach
        void stubsOk() {
            when(procesoRepository.findById(2L)).thenReturn(Optional.of(procesoDestino));
            when(mensajeRepository.existsByNombreAndTipoAndProcesoDestinoId(
                    "msgPagoAprobado", "CATCH", 2L)).thenReturn(false);

            Mensaje guardado = new Mensaje();
            guardado.setId(10L);
            guardado.setNombre("msgPagoAprobado");
            guardado.setTipo("CATCH");
            guardado.setPayloadJson(null);
            when(mensajeRepository.save(any(Mensaje.class))).thenReturn(guardado);
        }

        @Test
        @DisplayName("Debe retornar DTO con id generado cuando la captura es exitosa")
        void catchMessage_exitoso_retornaDTOConId() {
            MensajeDTO resultado = mensajeService.catchMessage(dtoCorrecto);

            assertThat(resultado.getId()).isEqualTo(10L);
            assertThat(resultado.getNombre()).isEqualTo("msgPagoAprobado");
            assertThat(resultado.getTipo()).isEqualTo("CATCH");
            assertThat(resultado.getProcesoDestinoId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Debe persistir con tipo CATCH sin importar el tipo del DTO")
        void catchMessage_siempreSetTipoCatch() {
            dtoCorrecto.setTipo("THROW"); // aunque venga mal, debe forzar CATCH

            mensajeService.catchMessage(dtoCorrecto);

            verify(mensajeRepository).save(argThat(m -> "CATCH".equals(m.getTipo())));
        }

        @Test
        @DisplayName("Debe persistir el payloadJson cuando viene informado")
        void catchMessage_conPayload_persistePayload() {
            dtoCorrecto.setPayloadJson("{\"monto\":500}");
            Mensaje guardadoConPayload = new Mensaje();
            guardadoConPayload.setId(11L);
            guardadoConPayload.setNombre("msgPagoAprobado");
            guardadoConPayload.setTipo("CATCH");
            guardadoConPayload.setPayloadJson("{\"monto\":500}");
            when(mensajeRepository.save(any())).thenReturn(guardadoConPayload);

            MensajeDTO resultado = mensajeService.catchMessage(dtoCorrecto);

            assertThat(resultado.getPayloadJson()).isEqualTo("{\"monto\":500}");
        }

        @Test
        @DisplayName("Debe verificar proceso destino y chequeo de duplicado exactamente una vez")
        void catchMessage_invocaRepositoriosCorrectos() {
            mensajeService.catchMessage(dtoCorrecto);

            verify(procesoRepository, times(1)).findById(2L);
            verify(mensajeRepository, times(1))
                    .existsByNombreAndTipoAndProcesoDestinoId("msgPagoAprobado", "CATCH", 2L);
            verify(mensajeRepository, times(1)).save(any(Mensaje.class));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Validaciones de entrada
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Validación del DTO")
    class ValidacionDto {

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el DTO es null")
        void catchMessage_dtoNull_lanzaExcepcion() {
            assertThatThrownBy(() -> mensajeService.catchMessage(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando nombre es null")
        void catchMessage_nombreNull_lanzaExcepcion() {
            dtoCorrecto.setNombre(null);

            assertThatThrownBy(() -> mensajeService.catchMessage(dtoCorrecto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando nombre está en blanco")
        void catchMessage_nombreBlanco_lanzaExcepcion() {
            dtoCorrecto.setNombre("   ");

            assertThatThrownBy(() -> mensajeService.catchMessage(dtoCorrecto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("nombre");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando procesoDestinoId es null")
        void catchMessage_procesoDestinoNull_lanzaExcepcion() {
            dtoCorrecto.setProcesoDestinoId(null);

            assertThatThrownBy(() -> mensajeService.catchMessage(dtoCorrecto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("proceso destino");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Errores de negocio
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Errores de negocio")
    class ErroresNegocio {

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el proceso destino no existe")
        void catchMessage_procesoDestinoInexistente_lanzaExcepcion() {
            when(procesoRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> mensajeService.catchMessage(dtoCorrecto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("2");
        }

        @Test
        @DisplayName("Debe lanzar MensajeCatchException cuando ya existe un CATCH duplicado")
        void catchMessage_duplicado_lanzaExcepcion() {
            when(procesoRepository.findById(2L)).thenReturn(Optional.of(procesoDestino));
            when(mensajeRepository.existsByNombreAndTipoAndProcesoDestinoId(
                    "msgPagoAprobado", "CATCH", 2L)).thenReturn(true);

            assertThatThrownBy(() -> mensajeService.catchMessage(dtoCorrecto))
                    .isInstanceOf(MensajeCatchException.class)
                    .hasMessageContaining("msgPagoAprobado");

            // No debe intentar persistir
            verify(mensajeRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Límite de responsabilidad
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("throwMessage() debe lanzar UnsupportedOperationException — pertenece a Dev 1")
    void throwMessage_noImplementado_lanzaExcepcion() {
        assertThatThrownBy(() -> mensajeService.throwMessage(dtoCorrecto))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Dev 1");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Prueba de integración básica — flujo Throw/Catch para Dev 1
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Este test documenta el flujo esperado del cruce Throw/Catch.
     * Dev 1 puede usarlo como contrato:
     *   1. Registrar CATCH para el proceso destino (este método).
     *   2. Verificar que el CATCH queda persistido y es recuperable.
     *   3. Dev 1 luego llamará throwMessage() con el mismo nombre y procesoOrigenId.
     */
    @Test
    @DisplayName("[Cruce Dev1-Dev2] CATCH registrado es recuperable por nombre y proceso destino")
    void catchRegistrado_esRecuperablePorNombreYProceso() {
        when(procesoRepository.findById(2L)).thenReturn(Optional.of(procesoDestino));
        when(mensajeRepository.existsByNombreAndTipoAndProcesoDestinoId(any(), any(), any()))
                .thenReturn(false);
        Mensaje guardado = new Mensaje();
        guardado.setId(99L);
        guardado.setNombre("msgPagoAprobado");
        guardado.setTipo("CATCH");
        when(mensajeRepository.save(any())).thenReturn(guardado);
        when(mensajeRepository.findById(99L)).thenReturn(Optional.of(guardado));

        // Dev 2 registra el CATCH
        MensajeDTO catchResult = mensajeService.catchMessage(dtoCorrecto);
        assertThat(catchResult.getId()).isEqualTo(99L);

        // Dev 1 puede verificar que el CATCH existe antes de lanzar el THROW
        MensajeDTO encontrado = mensajeService.obtenerMensajePorId(99L);
        assertThat(encontrado.getNombre()).isEqualTo("msgPagoAprobado");
        assertThat(encontrado.getTipo()).isEqualTo("CATCH");
    }
}
