package com.grupo1.editorprocesos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActividadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void obtenerActividad_noExiste_retorna404() throws Exception {

        mockMvc.perform(get("/api/v1/actividades/1"))
                .andExpect(status().isNotFound());

    }

}