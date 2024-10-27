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

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest{

    private final String baseUrl = "https://demoqa.com";
    private final String username = "damesaa1";
    private final String password = "Password123!";
    private String userId;
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = baseUrl;
        // Configurar WebDriver para Selenium
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        //driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    }

    @Test
    public void createUserAndDeleteFromProfile() {
        // 1. Create User
        createUser();
        // 2. Login through the UI and delete from profile
        loginProfile();
        deleteFromProfile();
        confirmDeleteFromProfile();

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
        System.out.println("User created with ID: " + userId);
    }

    private void loginProfile() {
        // Open the login page
        driver.get(baseUrl + "/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Find the username and password fields and login button
        WebElement usernameField = driver.findElement(By.xpath("//div[@id='userName-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='userName']\n"));
        WebElement passwordField = driver.findElement(By.xpath("//div[@id='password-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='password']\n"));
        WebElement loginButton = driver.findElement(By.xpath("//div[@class='text-right button']/button[@id='login']\n"));

        // Input the username and password and click login
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='text-right col-md-5 col-sm-12']/label[@id='userName-value' and text()='damesaa1']\n")));
        // Validate that we have been redirected to the profile page
        WebElement profileText = driver.findElement(By.xpath("//div[@class='text-right col-md-5 col-sm-12']/label[@id='userName-value' and text()='damesaa1']\n"));
        assertTrue(profileText.isDisplayed(), "User is not on the Profile page");
    }


    private void deleteFromProfile() {
        WebElement deleteButton = driver.findElement(By.xpath("//div[@class='text-center button']/button[@id='submit' and text()='Delete Account']\n"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", deleteButton);
        deleteButton.click();

        WebElement okButton = driver.findElement(By.xpath("//button[@id='closeSmallModal-ok' and text()='OK']\n"));
        okButton.click();

//        Alert alert = driver.switchTo().alert();
//        String alertText = alert.getText();
//        assertEquals("User Deleted.", alertText, "confirm deleted");
//
//        // Handle any confirmation prompts (assuming a confirmation is needed)
//        driver.switchTo().alert().accept();

        System.out.println("User deleted from the profile page.");
    }

    private void confirmDeleteFromProfile() {
        driver.manage().deleteAllCookies();
        driver.get(baseUrl+"/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='userName-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='userName']\n")));
        WebElement usernameField = driver.findElement(By.xpath("//div[@id='userName-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='userName']\n"));
        WebElement passwordField = driver.findElement(By.xpath("//div[@id='password-wrapper']/div[@class='col-md-9 col-sm-12']/input[@id='password']\n"));
        WebElement loginButton = driver.findElement(By.xpath("//div[@class='mt-2 buttonWrap row']/div[@class='text-right button']/button[@id='login' and text()='Login']\n"));

        // Input the username and password and click login
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loginButton);
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='output']/div[@class='col-md-12 col-sm-12']/p[@id='name' and contains(text(), 'Invalid username or password!')]\n")));
        WebElement loginHeader = driver.findElement(By.xpath("//div[@id='output']/div[@class='col-md-12 col-sm-12']/p[@id='name' and contains(text(), 'Invalid username or password!')]\n"));
        assertTrue(loginHeader.isDisplayed(), "User was not redirected to the login page after deletion.");
        System.out.println("User deleted from the profile page.");
    }


    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

