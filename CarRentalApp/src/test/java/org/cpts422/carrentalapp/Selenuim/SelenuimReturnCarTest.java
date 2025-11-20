package org.cpts422.carrentalapp.Selenuim;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SelenuimReturnCarTest {

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
    void testUserCanReturnCarWithoutPenalty() {

        driver.get(baseUrl + "/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")))
                .sendKeys("testuser");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.cssSelector("button.btn[type='submit']")).click();
        driver.get(baseUrl + "/vehicles");

        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("form[action='/cart/add-rent'] button[type='submit']")));
        addButton.click();
        wait.until(ExpectedConditions.urlContains("/cart"));

        WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Checkout Rentals']")));
        checkoutButton.click();
        wait.until(ExpectedConditions.urlContains("/my-rentals"));

        List<WebElement> returnButtonsBefore =
                driver.findElements(By.xpath("//table//button[normalize-space()='Return']"));
        assertFalse(returnButtonsBefore.isEmpty(),
                "Expected at least one Return button before returning a rental.");
        WebElement firstReturnButton = returnButtonsBefore.get(0);
        firstReturnButton.click();

        // 5) Explicitly wait for 'return'
        boolean sawReturnMessage = wait.until(driver -> {
            List<WebElement> messages =
                    driver.findElements(By.xpath("//p[normalize-space()!='']"));
            return messages.stream()
                    .map(WebElement::getText)
                    .map(String::toLowerCase)
                    .anyMatch(text -> text.contains("return"));
        });

        assertTrue(sawReturnMessage,
                "Expected a message saying the car was returned.");

        // Check one less return button after returning vehicle
        List<WebElement> returnButtonsAfter =
                driver.findElements(By.xpath("//table//button[normalize-space()='Return']"));

        assertTrue(returnButtonsAfter.size() < returnButtonsBefore.size(),
                "There should be fewer Return buttons after returning the car.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
