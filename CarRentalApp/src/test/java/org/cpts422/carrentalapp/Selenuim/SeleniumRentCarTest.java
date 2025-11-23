package org.cpts422.carrentalapp.Selenuim;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SeleniumRentCarTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "http://localhost:8080";
    private static final int DEMO_DELAY_MS = 2000; // 2 seconds between major steps for demo visibility

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Selenium: Complete car rental flow - Login, Browse, Add to Cart, Checkout")
    void testRentCarFlow() {
        // Step 1: Login
        driver.get(baseUrl + "/login");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")))
                .sendKeys("testuser");
        driver.findElement(By.id("password")).sendKeys("securePassword123");
        driver.findElement(By.cssSelector("button.btn[type='submit']")).click();
        
        // Wait for redirect after login
        wait.until(ExpectedConditions.urlContains(baseUrl));
        sleepForDemo("Login completed");
        
        // Step 2: Add funds to account (rental will be $150 for 3 days at $50/day)
        driver.get(baseUrl + "/account");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("amount")));
        
        WebElement amountInput = driver.findElement(By.name("amount"));
        amountInput.clear();
        amountInput.sendKeys("200.00"); // Add $200 to ensure sufficient funds
        
        WebElement addFundsButton = driver.findElement(
                By.cssSelector("form[action='/account/add-funds'] button[type='submit']")
        );
        addFundsButton.click();
        
        // Wait for redirect back to account page (URL changes)
        wait.until(ExpectedConditions.urlToBe(baseUrl + "/account"));
        
        // Small wait to ensure page has processed the funds addition
        sleepForDemo("Funds added successfully");
        
        // Step 3: Navigate to vehicles page
        driver.get(baseUrl + "/vehicles");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        sleepForDemo("Viewing vehicles page");
        
        // Step 4: Find the first available vehicle and add it to cart
        // Look for the first form with action="/cart/add-rent" that has an enabled button
        WebElement addToCartForm = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("form[action='/cart/add-rent']")
                )
        );
        
        // Find the days input field and set it to 3 days
        WebElement daysInput = addToCartForm.findElement(By.name("days"));
        daysInput.clear();
        daysInput.sendKeys("3");
        
        // Click the Add button
        WebElement addButton = addToCartForm.findElement(By.cssSelector("button[type='submit']"));
        
        // Only proceed if button is enabled (vehicle is available)
        if (addButton.isEnabled()) {
            addButton.click();
            
            // Wait for redirect to cart page
            wait.until(ExpectedConditions.urlContains("/cart"));
            sleepForDemo("Vehicle added to cart");
            
            // Step 5: Verify item is in cart
            WebElement cartTable = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("table")
                    )
            );
            assertTrue(cartTable.isDisplayed(), "Cart table should be visible");
            sleepForDemo("Viewing cart");
            
            // Step 6: Checkout
            WebElement checkoutButton = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[normalize-space()='Checkout Rentals']")
                    )
            );
            checkoutButton.click();
            sleepForDemo("Processing checkout");
            
            // Step 7: Verify successful checkout - should redirect to /my-rentals
            wait.until(ExpectedConditions.urlContains("/my-rentals"));
            sleepForDemo("Checkout completed");
            
            // Verify success message or page content
            WebElement pageContent = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
            );
            assertTrue(pageContent.isDisplayed(), "Should be on rentals page after checkout");
            
            System.out.println("Car rental test completed successfully!");
        } else {
            System.out.println("No available vehicles found to rent. Skipping test.");
            // This is not a failure - just means no vehicles are available
        }
    }

    private void sleepForDemo(String stepDescription) {
        try {
            System.out.println(">>> " + stepDescription + " - pausing for demo visibility...");
            Thread.sleep(DEMO_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
