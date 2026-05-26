package com.grupo1.editorprocesos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessProtectedResourceWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/procesos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "editor@empresa.com", roles = {"EDITOR"})
    void whenAccessProtectedResourceWithToken_thenNotUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/procesos/1"))
                .andExpect(status().isNotFound()); // It is found to be 404 instead of 401
    }

    @Test
    @WithMockUser(username = "lector@empresa.com", roles = {"LECTOR"})
    void whenDeleteProcessAsLector_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@empresa.com", roles = {"ADMIN_EMPRESA"})
    void whenDeleteProcessAsAdmin_thenNotForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1"))
                .andExpect(status().isNotFound()); // Mock is 404 because process 1 does not exist, but not 403 Forbidden!
    }

    @Test
    void whenRegisterEnterprise_thenPublic() throws Exception {
        long timestamp = System.currentTimeMillis();
        int randomVal = (int)(Math.random() * 1000);
        String uniqueNit = "nit-" + timestamp + "-" + randomVal;
        String uniqueEmail = "sec-" + timestamp + "-" + randomVal + "@empresa.com";
        mockMvc.perform(post("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nit\":\"" + uniqueNit + "\",\"nombre\":\"Empresa Test Security\",\"correoContacto\":\"" + uniqueEmail + "\",\"passwordInicialAdmin\":\"Pass123!\"}"))
                .andExpect(status().isCreated());
    }
}
