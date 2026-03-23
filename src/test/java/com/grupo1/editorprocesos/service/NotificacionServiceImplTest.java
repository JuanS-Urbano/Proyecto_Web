package com.grupo1.editorprocesos.service;

import com.grupo1.editorprocesos.dto.NotificacionRequestDTO;
import com.grupo1.editorprocesos.dto.NotificacionResponseDTO;
import com.grupo1.editorprocesos.service.impl.NotificacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    private NotificacionRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new NotificacionRequestDTO();
    }

    @Test
    void enviarEmail_destinoValido_debeRetornarExito() {
        NotificacionResponseDTO resultado =
                notificacionService.enviarEmail("usuario@empresa.com", "Alerta", "Actividad completada");

        assertThat(resultado.isEnviado()).isTrue();
        assertThat(resultado.getTipo()).isEqualTo("EMAIL");
        assertThat(resultado.getDestino()).isEqualTo("usuario@empresa.com");
    }

    @Test
    void enviarEmail_destinoVacio_debeRetornarError() {
        NotificacionResponseDTO resultado =
                notificacionService.enviarEmail("", "Asunto", "Cuerpo");

        assertThat(resultado.isEnviado()).isFalse();
        assertThat(resultado.getMensaje()).contains("destino no puede estar vacío");
    }

    @Test
    void enviarWebhook_urlValida_debeRetornarExito() {
        NotificacionResponseDTO resultado =
                notificacionService.enviarWebhook("https://api.empresa.com/hook", "{\"evento\":\"test\"}");

        assertThat(resultado.isEnviado()).isTrue();
        assertThat(resultado.getTipo()).isEqualTo("WEBHOOK");
    }

    @Test
    void enviarWebhook_urlSinHttp_debeRetornarError() {
        NotificacionResponseDTO resultado =
                notificacionService.enviarWebhook("api.empresa.com/hook", "{}");

        assertThat(resultado.isEnviado()).isFalse();
        assertThat(resultado.getMensaje()).contains("http://");
    }

    @Test
    void enviar_tipoEmail_debeDespacharCorrectamente() {
        request.setTipo("EMAIL");
        request.setDestino("test@correo.com");
        request.setAsunto("Prueba");
        request.setCuerpo("Mensaje de prueba");

        NotificacionResponseDTO resultado = notificacionService.enviar(request);

        assertThat(resultado.isEnviado()).isTrue();
        assertThat(resultado.getTipo()).isEqualTo("EMAIL");
    }

    @Test
    void enviar_tipoWebhook_debeDespacharCorrectamente() {
        request.setTipo("WEBHOOK");
        request.setDestino("https://hook.empresa.com/evento");
        request.setCuerpo("{\"proceso\":\"activo\"}");

        NotificacionResponseDTO resultado = notificacionService.enviar(request);

        assertThat(resultado.isEnviado()).isTrue();
        assertThat(resultado.getTipo()).isEqualTo("WEBHOOK");
    }

    @Test
    void enviar_tipoNoSoportado_debeRetornarError() {
        request.setTipo("SMS");
        request.setDestino("3001234567");

        NotificacionResponseDTO resultado = notificacionService.enviar(request);

        assertThat(resultado.isEnviado()).isFalse();
        assertThat(resultado.getMensaje()).contains("no soportado");
    }

    @Test
    void enviar_tipoNulo_debeRetornarError() {
        request.setTipo(null);
        request.setDestino("destino");

        NotificacionResponseDTO resultado = notificacionService.enviar(request);

        assertThat(resultado.isEnviado()).isFalse();
        assertThat(resultado.getMensaje()).contains("obligatorio");
    }
}