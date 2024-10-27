package com.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    private final String baseUrl = "https://demoqa.com";
    private final String password = "Password123!";
    private String username;
    private String userId;
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = baseUrl;
        username = "user_" + Instant.now().getEpochSecond();
    }

    @Test
    public void createUserAndDeleteFromProfile() {
        createUser();

        if (userId != null) {
            setupWebDriver();
            loginProfile();
            deleteFromProfile();
            confirmDeleteFromProfile();
        }
    }

    private void createUser() {
        String createUserPayload = String.format("{ \"userName\": \"%s\", \"password\": \"%s\" }", username, password);

        Response response = given()
                .contentType("application/json")
                .body(createUserPayload)
                .when()
                .post("/Account/v1/User")
                .then()
                .statusCode(201)
                .extract().response();

        userId = response.path("userID");

        // Assert to validate that the userID is not null or empty
        assertTrue(userId != null && !userId.isEmpty(), "User ID should not be null or empty after user creation");

        // Optional: Validate additional fields if available
        List<Objects> booksValidation = response.path("books");
        assertEquals( 0, booksValidation.size());

        System.out.println("User created with ID: " + userId);
    }


    private void setupWebDriver() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    private void loginProfile() {
        driver.get(baseUrl + "/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='userName-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='userName']")));
        WebElement passwordField = driver.findElement(
                By.xpath("//div[@id='password-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='password']"));
        WebElement loginButton = driver.findElement(
                By.xpath("//div[@class='text-right button']/button[@id='login']"));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='text-right col-md-5 col-sm-12']/label[@id='userName-value' and text()='" + username + "']")));
        WebElement profileText = driver.findElement(
                By.xpath("//div[@class='text-right col-md-5 col-sm-12']/label[@id='userName-value' and text()='" + username + "']"));
        assertTrue(profileText.isDisplayed(), "User is not on the Profile page");
    }

    private void deleteFromProfile() {
        WebElement deleteButton = driver.findElement(
                By.xpath("//div[@class='text-center button']/button[@id='submit' and text()='Delete Account']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", deleteButton);
        deleteButton.click();

        WebElement okButton = driver.findElement(
                By.xpath("//button[@id='closeSmallModal-ok' and text()='OK']"));
        okButton.click();

        System.out.println("User deleted from the profile page.");
    }

    private void confirmDeleteFromProfile() {
        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='userName-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='userName']")));

        WebElement usernameField = driver.findElement(
                By.xpath("//div[@id='userName-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='userName']"));
        WebElement passwordField = driver.findElement(
                By.xpath("//div[@id='password-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='password']"));
        WebElement loginButton = driver.findElement(
                By.xpath("//div[@class='mt-2 buttonWrap row']/div[@class='text-right button']/button[@id='login' and text()='Login']"));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@id='output']/div[@class='col-md-12 col-sm-12']/p[@id='name' and contains(text(), 'Invalid username or password!')]")));
        WebElement loginError = driver.findElement(
                By.xpath("//div[@id='output']/div[@class='col-md-12 col-sm-12']/p[@id='name' and contains(text(), 'Invalid username or password!')]"));
        assertTrue(loginError.isDisplayed(), "User was not redirected to the login page after deletion.");
        System.out.println("User deletion confirmed.");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}