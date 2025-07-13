package com.yourcompany.yourproject.stepdefs;

import com.yourcompany.yourproject.base.DriverManager;
import com.yourcompany.yourproject.enums.BrowserType;
import com.yourcompany.yourproject.pages.LoginPage;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;

public class LoginSteps {
    WebDriver driver;
    LoginPage loginPage;

    @Given("I am on the login page")
    public void i_am_on_the_login_page() {
        driver = DriverManager.getDriver(BrowserType.CHROME);
        driver.get("https://example.com/login");
        loginPage = new LoginPage(driver);
    }

    @When("I enter valid credentials")
    public void i_enter_valid_credentials() {
        loginPage.login("admin", "admin123");
    }

    @Then("I should be redirected to the dashboard")
    public void i_should_be_redirected_to_the_dashboard() {
        // assertion logic
    }
}
