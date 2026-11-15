package com.carteira.service;

import com.carteira.exception.SaldoInsuficienteException;
import com.carteira.model.Conta;
import com.carteira.repository.ContaRepository;
import org.springframework.stereotype.Service;

@Service
public class ContaService {
    private final ContaRepository repository;
    public ContaService(ContaRepository repository) { this.repository = repository; }

    public Conta criarConta(String titular, Double saldoInicial) {
        double saldo = saldoInicial == null ? 0.0 : saldoInicial;
        if (titular == null || titular.isBlank() || saldo < 0) throw new IllegalArgumentException("Dados da conta inválidos");
        return repository.save(new Conta(null, titular, saldo));
    }

    public Double consultarSaldo(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + id)).getSaldo();
    }

    public Conta depositar(Long id, Double valor) {
        if (valor == null || valor <= 0) throw new IllegalArgumentException("O valor deve ser maior que zero");
        Conta conta = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + id));
        conta.setSaldo(conta.getSaldo() + valor);
        return repository.save(conta);
    }

    public void transferir(Long origemId, Long destinoId, Double valor) {
        if (valor == null || valor <= 0) throw new IllegalArgumentException("O valor deve ser maior que zero");
        if (origemId.equals(destinoId)) throw new IllegalArgumentException("Origem e destino devem ser diferentes");
        Conta origem = repository.findById(origemId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + origemId));
        Conta destino = repository.findById(destinoId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + destinoId));
        if (origem.getSaldo() < valor) throw new SaldoInsuficienteException("Saldo insuficiente na conta " + origemId);
        origem.setSaldo(origem.getSaldo() - valor);
        destino.setSaldo(destino.getSaldo() + valor);
        repository.save(origem);
        repository.save(destino);
    }
}
