package com.denkitronik.clienteservice.delivery.rest;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.services.IClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteRestController.class)
class ClienteRestControllerTest {

    private static final String BASE_URL = "/api/v1/cliente-service/clientes";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IClienteService clienteService;

    @Test
    void getByIdReturnsClient() throws Exception {
        when(clienteService.findById(1L)).thenReturn(cliente(1L));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ada"))
                .andExpect(jsonPath("$.apellido").value("Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void getByMissingIdReturns404() throws Exception {
        when(clienteService.findById(99999L)).thenThrow(new ClienteNotFoundException(99999L));

        mockMvc.perform(get(BASE_URL + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postCreatesClientAndReturns201() throws Exception {
        when(clienteService.save(any(Cliente.class))).thenReturn(cliente(7L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombre").value("Ada"));
    }

    @Test
    void postWithInvalidEmailReturns400AndErrorsArray() throws Exception {
        String invalidEmail = """
                {"nombre":"Ada","apellido":"Lovelace","email":"invalid",
                 "region":{"id":1}}
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmail))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @Test
    void postWithEmptyNameReturns400AndErrorsArray() throws Exception {
        String emptyName = """
                {"nombre":"","apellido":"Lovelace","email":"ada@example.com",
                 "region":{"id":1}}
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyName))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("nombre")));
    }

    @Test
    void putUpdatesClientAndReturns201() throws Exception {
        when(clienteService.findById(1L)).thenReturn(cliente(1L));
        when(clienteService.update(eq(1L), any(Cliente.class))).thenReturn(cliente(1L));

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void putWithMissingIdReturns404() throws Exception {
        when(clienteService.findById(99999L)).thenThrow(new ClienteNotFoundException(99999L));

        mockMvc.perform(put(BASE_URL + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validClientJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        doNothing().when(clienteService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(clienteService).delete(1L);
    }

    private static Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre("Ada");
        cliente.setApellido("Lovelace");
        cliente.setEmail("ada@example.com");
        Region region = new Region();
        region.setId(1L);
        cliente.setRegion(region);
        return cliente;
    }

    private static String validClientJson() {
        return """
                {"nombre":"Ada","apellido":"Lovelace","email":"ada@example.com",
                 "region":{"id":1}}
                """;
    }
}
