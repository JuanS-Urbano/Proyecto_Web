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
class ProcesoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void obtenerProceso_noExiste_retorna404() throws Exception {

        mockMvc.perform(get("/api/v1/procesos/1"))
                .andExpect(status().isNotFound());

    }

}