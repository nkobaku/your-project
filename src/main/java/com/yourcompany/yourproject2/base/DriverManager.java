package com.yourcompany.yourproject.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.yourcompany.yourproject.enums.BrowserType;

public class DriverManager {
    private static WebDriver driver;

    public static WebDriver getDriver(BrowserType browser) {
        if (driver == null) {
            switch (browser) {
                case FIREFOX:
                    driver = new FirefoxDriver();
                    break;
                case CHROME:
                default:
                    driver = new ChromeDriver();
            }
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
