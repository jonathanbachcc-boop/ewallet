# Carteira Digital (e-Wallet) — Testes Automatizados I

API REST em Spring Boot (Java 17) com camadas Repository, Service e Controller, testada com JUnit 5, Mockito, MockMvc, Selenium e JaCoCo.

## Endpoints
| Método | Rota | Corpo | Sucesso | Erro |
|---|---|---|---|---|
| POST | `/contas` | `{"titular":"Ana","saldo":100}` | 200 | 400 |
| GET | `/contas/{id}/saldo` | — | 200 | 400 |
| POST | `/contas/{id}/deposito` | `{"valor":50}` | 200 | 400 (valor <= 0) |
| POST | `/contas/transferencia` | `{"origemId":1,"destinoId":2,"valor":30}` | 200 | 400 (`SaldoInsuficienteException`) |

Página de depósito: `http://localhost:8080/index.html`

## Comandos
```bash
mvn spring-boot:run            # sobe a API
mvn test                       # roda todos os testes + gera relatório JaCoCo
mvn verify                     # roda testes e falha se cobertura < 80%
mvn test -Dheadless=false      # mostra o Chrome abrindo no teste Selenium
mvn test -Dtest=ContaServiceTest   # só os testes unitários
```
Relatório de cobertura: `target/site/jacoco/index.html` (filtrado para `service` e `controller`).

Requisitos: JDK 17+, Maven, Google Chrome instalado (o driver é baixado automaticamente).

## Testes
- **Unitários** — `ContaServiceTest`: `@Mock ContaRepository` + `@InjectMocks ContaService`, caminho feliz e falhas (valor inválido, conta inexistente, `SaldoInsuficienteException`).
- **Integração** — `ContaControllerIntegrationTest`: MockMvc validando 200 e 400.
- **Interface** — `DepositoSeleniumTest`: preenche o formulário de depósito e confere o saldo exibido.

## TDD (Red → Green → Refactor)
Num repositório novo, rode `bash tdd-commits.sh`: ele cria os commits na ordem RED (testes) → GREEN (implementação mínima) → REFACTOR. Depois:
```bash
git remote add origin https://github.com/SEU_USUARIO/ewallet.git
git branch -M main && git push -u origin main
```
