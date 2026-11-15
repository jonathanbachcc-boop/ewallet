package com.carteira.controller;

import com.carteira.model.Conta;
import com.carteira.service.ContaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    public record CriarContaRequest(String titular, Double saldo) {}
    public record ValorRequest(Double valor) {}
    public record TransferenciaRequest(Long origemId, Long destinoId, Double valor) {}

    @PostMapping
    public ResponseEntity<Conta> criar(@RequestBody CriarContaRequest req) {
        return ResponseEntity.ok(service.criarConta(req.titular(), req.saldo()));
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<Map<String, Object>> saldo(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("id", id, "saldo", service.consultarSaldo(id)));
    }

    @PostMapping("/{id}/deposito")
    public ResponseEntity<Conta> depositar(@PathVariable Long id, @RequestBody ValorRequest req) {
        return ResponseEntity.ok(service.depositar(id, req.valor()));
    }

    @PostMapping("/transferencia")
    public ResponseEntity<Map<String, String>> transferir(@RequestBody TransferenciaRequest req) {
        service.transferir(req.origemId(), req.destinoId(), req.valor());
        return ResponseEntity.ok(Map.of("mensagem", "Transferência realizada com sucesso"));
    }
}
