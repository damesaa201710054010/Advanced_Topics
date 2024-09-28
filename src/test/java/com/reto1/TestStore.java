package com.reto1;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStore {

    WebDriver driver;

    @BeforeEach
    public void initConfig(){
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    public void SignUpSuccess() {
        //ARRANGE
        driver.get("https://teststore.automationtesting.co.uk/index.php");
        driver.findElement(By.xpath("//div[@id='_desktop_user_info']//a")).click();
        driver.findElement(By.xpath("//a[@data-link-action='display-register-form']")).click();

        // ACT
        WebElement inputSocialTitle = driver.findElement(By.xpath("//span[@class='custom-radio']//input[@id='field-id_gender-1']"));
        WebElement inputFirstName = driver.findElement(By.xpath("//div[@class='col-md-6 js-input-column']//input[@id='field-firstname']"));
        WebElement inputLastName = driver.findElement(By.xpath("//div[@class='col-md-6 js-input-column']//input[@id='field-lastname']"));
        WebElement inputEmail = driver.findElement(By.xpath("//div[@class='col-md-6 js-input-column']//input[@id='field-email']"));
        WebElement inputPassword = driver.findElement(By.xpath("//div[@class='col-md-6 js-input-column']//input[@id='field-password']"));
        WebElement inputTermsAndCond = driver.findElement(By.xpath("//span[@class='custom-checkbox']//input[@required]"));

        inputSocialTitle.click();
        inputFirstName.sendKeys("Test");
        inputLastName.sendKeys("Testington");
        inputEmail.sendKeys("test"+ System.currentTimeMillis()+"@testing.com");
        inputPassword.sendKeys("superpassword2024");
        inputTermsAndCond.click();

        driver.findElement(By.xpath("//footer[@class='form-footer clearfix']//button[@data-link-action='save-customer']")).click();

        //ASSERT
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        WebElement name = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='user-info']//a[@class='account']//span[@class='hidden-sm-down']")
        ));
        String accountName = name.getText();
        assertEquals(accountName,"Test Testington");
    }

    @AfterEach
    public void finishConfig(){
        WebElement logOut = driver.findElement(By.xpath("//div[@class='user-info']//a[@class='logout hidden-sm-down']"));
        logOut.click();
        driver.close();
    }
}
