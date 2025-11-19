package org.cpts422.carrentalapp.Selenuim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SelenuimRegisterTest {

    private static WebDriver driver;
    private WebDriverWait wait;

     @BeforeEach
    void setUp() {
         ChromeOptions options = new ChromeOptions();
         driver = new ChromeDriver(options);
         wait = new WebDriverWait(driver, Duration.ofSeconds(10));

     }

    @Test
    void testRegisterUser() {
        driver.get("http://localhost:8080/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("tytyruss");
        driver.findElement(By.id("age")).sendKeys("22");
        driver.findElement(By.id("password")).sendKeys("wsucompsci");
        driver.findElement(By.id("driversLicenseNumber")).sendKeys("DL-1234567");
        driver.findElement(By.id("driversLicenseExpiry")).sendKeys("11-19-2026");

        WebElement membershipSelect = driver.findElement(By.id("membershipType"));
        membershipSelect.findElement(By.cssSelector("option[value='STANDARD']")).click();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement successMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card .title"))
        );

        assertEquals("Your account was created", successMessage.getText());

        System.out.println("Registration test completed.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
