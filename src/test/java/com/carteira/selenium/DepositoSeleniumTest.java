package com.carteira.selenium;

import com.carteira.model.Conta;
import com.carteira.repository.ContaRepository;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste de interface: abre a página de depósito no Chrome, preenche o formulário
 * e verifica o saldo exibido. O Selenium Manager baixa o driver automaticamente.
 * Para ver o navegador abrindo na apresentação, rode com: mvn test -Dheadless=false
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DepositoSeleniumTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ContaRepository repository;

    private WebDriver driver;
    private Long contaId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        contaId = repository.save(new Conta(null, "Ana", 100.0)).getId();

        ChromeOptions options = new ChromeOptions();
        if (!"false".equals(System.getProperty("headless"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    void deveRealizarDepositoPelaInterface() {
        driver.get("http://localhost:" + port + "/index.html");

        driver.findElement(By.id("contaId")).sendKeys(contaId.toString());
        driver.findElement(By.id("valor")).sendKeys("50");
        driver.findElement(By.id("btn-depositar")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resultado"), "Saldo"));

        assertEquals("Saldo atual: 150.00", driver.findElement(By.id("resultado")).getText());
    }

    @Test
    void deveExibirErroParaDepositoInvalido() {
        driver.get("http://localhost:" + port + "/index.html");

        driver.findElement(By.id("contaId")).sendKeys(contaId.toString());
        driver.findElement(By.id("valor")).sendKeys("-5");
        driver.findElement(By.id("btn-depositar")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resultado"), "Erro"));
    }
}
