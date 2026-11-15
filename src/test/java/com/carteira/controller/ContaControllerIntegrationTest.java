package com.carteira.controller;

import com.carteira.model.Conta;
import com.carteira.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContaRepository repository;

    private Long origemId;
    private Long destinoId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        origemId = repository.save(new Conta(null, "Ana", 100.0)).getId();
        destinoId = repository.save(new Conta(null, "Bruno", 0.0)).getId();
    }

    @Test
    void deveCriarConta() throws Exception {
        mockMvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titular\":\"Carla\",\"saldo\":10.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titular").value("Carla"));
    }

    @Test
    void deveRetornar400AoCriarContaInvalida() throws Exception {
        mockMvc.perform(post("/contas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titular\":\"\",\"saldo\":10.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveConsultarSaldoComStatus200() throws Exception {
        mockMvc.perform(get("/contas/{id}/saldo", origemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(100.0));
    }

    @Test
    void deveRetornar400ParaContaInexistente() throws Exception {
        mockMvc.perform(get("/contas/{id}/saldo", 9999))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveDepositarComStatus200() throws Exception {
        mockMvc.perform(post("/contas/{id}/deposito", origemId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":50.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.0));
    }

    @Test
    void deveRetornar400ParaDepositoInvalido() throws Exception {
        mockMvc.perform(post("/contas/{id}/deposito", origemId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"valor\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void deveTransferirComStatus200() throws Exception {
        mockMvc.perform(post("/contas/transferencia").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origemId\":" + origemId + ",\"destinoId\":" + destinoId + ",\"valor\":40.0}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/contas/{id}/saldo", destinoId))
                .andExpect(jsonPath("$.saldo").value(40.0));
    }

    @Test
    void deveRetornar400ParaSaldoInsuficiente() throws Exception {
        mockMvc.perform(post("/contas/transferencia").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"origemId\":" + origemId + ",\"destinoId\":" + destinoId + ",\"valor\":500.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Saldo insuficiente na conta " + origemId));
    }
}
