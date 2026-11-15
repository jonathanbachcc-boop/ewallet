package com.carteira.service;

import com.carteira.exception.SaldoInsuficienteException;
import com.carteira.model.Conta;
import com.carteira.repository.ContaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository repository;

    @InjectMocks
    private ContaService service;

    // ---------- Criar conta ----------
    @Test
    void deveCriarContaComSaldoInicial() {
        when(repository.save(any(Conta.class))).thenAnswer(inv -> {
            Conta c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        Conta conta = service.criarConta("Ana", 50.0);
        assertEquals(1L, conta.getId());
        assertEquals(50.0, conta.getSaldo());
    }

    @Test
    void deveCriarContaComSaldoZeroQuandoNulo() {
        when(repository.save(any(Conta.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(0.0, service.criarConta("Ana", null).getSaldo());
    }

    @Test
    void naoDeveCriarContaSemTitular() {
        assertThrows(IllegalArgumentException.class, () -> service.criarConta(" ", 10.0));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCriarContaComSaldoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> service.criarConta("Ana", -1.0));
    }

    // ---------- Consultar saldo ----------
    @Test
    void deveConsultarSaldo() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Conta(1L, "Ana", 100.0)));
        assertEquals(100.0, service.consultarSaldo(1L));
    }

    @Test
    void deveLancarExcecaoAoConsultarContaInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.consultarSaldo(99L));
    }

    // ---------- Depósito ----------
    @Test
    void deveDepositarValorPositivo() {
        Conta conta = new Conta(1L, "Ana", 100.0);
        when(repository.findById(1L)).thenReturn(Optional.of(conta));
        when(repository.save(conta)).thenReturn(conta);

        Conta resultado = service.depositar(1L, 50.0);

        assertEquals(150.0, resultado.getSaldo());
        verify(repository).save(conta);
    }

    @Test
    void naoDeveDepositarValorZero() {
        assertThrows(IllegalArgumentException.class, () -> service.depositar(1L, 0.0));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveDepositarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () -> service.depositar(1L, -10.0));
    }

    @Test
    void naoDeveDepositarValorNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.depositar(1L, null));
    }

    // ---------- Transferência ----------
    @Test
    void deveTransferirEntreContas() {
        Conta origem = new Conta(1L, "Ana", 100.0);
        Conta destino = new Conta(2L, "Bruno", 20.0);
        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));

        service.transferir(1L, 2L, 30.0);

        assertEquals(70.0, origem.getSaldo());
        assertEquals(50.0, destino.getSaldo());
        verify(repository, times(2)).save(any(Conta.class));
    }

    @Test
    void devePermitirTransferirSaldoTotal() {
        Conta origem = new Conta(1L, "Ana", 100.0);
        Conta destino = new Conta(2L, "Bruno", 0.0);
        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));

        service.transferir(1L, 2L, 100.0);

        assertEquals(0.0, origem.getSaldo());
        assertEquals(100.0, destino.getSaldo());
    }

    @Test
    void deveLancarSaldoInsuficienteException() {
        Conta origem = new Conta(1L, "Ana", 10.0);
        Conta destino = new Conta(2L, "Bruno", 0.0);
        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));

        assertThrows(SaldoInsuficienteException.class, () -> service.transferir(1L, 2L, 50.0));

        assertEquals(10.0, origem.getSaldo());
        assertEquals(0.0, destino.getSaldo());
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveTransferirValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.transferir(1L, 2L, 0.0));
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveTransferirParaMesmaConta() {
        assertThrows(IllegalArgumentException.class, () -> service.transferir(1L, 1L, 10.0));
    }
}
