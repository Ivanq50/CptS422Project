package org.cpts422.carrentalapp.Selenuim;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SelenuimInsufficientFundsTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    void testCheckoutFailsWhenUserHasInsufficientFunds() {

        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")))
                .sendKeys("testuser");
        driver.findElement(By.id("password")).sendKeys("securePassword123");
        driver.findElement(By.cssSelector("button.btn[type='submit']")).click();

        for (int i = 0; i < 10; i++) {
            driver.get(baseUrl + "/vehicles");

            WebElement addButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("form[action='/cart/add-rent'] button[type='submit']"))
            );
            addButton.click();
            wait.until(ExpectedConditions.urlContains("/cart"));
        }

        WebElement checkoutButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space()='Checkout Rentals']"))
        );
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("/cart"));

        boolean sawInsufficientFundsMessage = wait.until(driver -> {
            List<WebElement> messages =
                    driver.findElements(By.xpath("//p[normalize-space()!='']"));
            return messages.stream()
                    .map(WebElement::getText)
                    .map(String::toLowerCase)
                    .anyMatch(text ->
                            text.contains("insufficient funds"));
        });
        assertTrue(
                sawInsufficientFundsMessage,
                "Expected an error message about insufficient funds when checking out 10 rentals."
        );
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
