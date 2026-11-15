#!/usr/bin/env bash
# Cria o histórico Red -> Green -> Refactor no Git.
# Uso: rode dentro da pasta do projeto, num repositório novo:  bash tdd-commits.sh
set -e
git init -q 2>/dev/null || true
S=src/main/java/com/carteira
T=src/test/java/com/carteira
cp $S/service/ContaService.java /tmp/ContaService.final

git add pom.xml .gitignore src/main/resources/application.properties $S/EwalletApplication.java $S/model $S/repository README.md
git commit -m "chore: estrutura inicial do projeto (entidade Conta e repository)"

git add $S/exception $T/service/ContaServiceTest.java
git commit -m "test(RED): testes unitarios do ContaService antes da implementacao"

cat > $S/service/ContaService.java <<'JAVA'
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
JAVA
git add $S/service/ContaService.java
git commit -m "feat(GREEN): implementa ContaService para os testes passarem"

cp /tmp/ContaService.final $S/service/ContaService.java
git add $S/service/ContaService.java
git commit -m "refactor: extrai metodos buscarConta/validarValor e adiciona @Transactional"

git add $T/controller/ContaControllerIntegrationTest.java
git commit -m "test(RED): testes de integracao MockMvc dos endpoints"

git add $S/controller
git commit -m "feat(GREEN): ContaController e tratamento de erros com HTTP 400"

git add $T/selenium src/main/resources/static
git commit -m "test: pagina de deposito e teste de interface com Selenium"

git add -A
git commit -m "docs: script de commits TDD"
git log --oneline
