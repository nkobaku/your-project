# Define Java source files with their paths and content
java_sources = {
    "src/main/java/com/example/framework/utils/ConfigReader.java": """
package com.example.framework.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties props = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(props.getProperty(key));
    }
}
""",
    "src/main/java/com/example/framework/base/DriverManager.java": """
package com.example.framework.base;

import org.openqa.selenium.WebDriver;

public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void setDriver(WebDriver driverInstance) {
        driver.set(driverInstance);
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
""",
    "src/main/java/com/example/framework/utils/WaitUtils.java": """
package com.example.framework.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class WaitUtils {
    public static WebElement waitForVisible(WebDriver driver, WebElement element, int timeoutSec) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.visibilityOf(element));
    }

    public static void clickWhenReady(WebDriver driver, WebElement element, int timeoutSec) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                .until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    public static boolean isElementPresent(WebDriver driver, By locator, int timeoutSec) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSec))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
""",
    "src/main/java/com/example/framework/utils/JavaScriptUtils.java": """
package com.example.framework.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JavaScriptUtils {
    public static void clickWithJS(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public static void scrollIntoView(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
""",
    "src/main/java/com/example/framework/utils/LogUtils.java": """
package com.example.framework.utils;

import java.time.LocalDateTime;

public class LogUtils {
    public static void info(String message) {
        System.out.println(LocalDateTime.now() + " [INFO] " + message);
    }

    public static void error(String message) {
        System.err.println(LocalDateTime.now() + " [ERROR] " + message);
    }
}
""",
    "src/main/java/com/example/framework/utils/ScreenshotUtils.java": """
package com.example.framework.utils;

import org.openqa.selenium.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtils {
    public static void captureScreenshot(WebDriver driver, String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dest = Paths.get("screenshots", name + "_" + timestamp + ".png");
            Files.createDirectories(dest.getParent());
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }
}
""",
    "src/main/java/com/example/framework/base/BasePage.java": """
package com.example.framework.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;

public abstract class BasePage<T extends LoadableComponent<T>> extends LoadableComponent<T> {
    protected WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, 10), this);
    }
}
""",
    "src/main/java/com/example/framework/base/BaseComponent.java": """
package com.example.framework.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;

public abstract class BaseComponent<T extends LoadableComponent<T>> extends LoadableComponent<T> {
    protected WebDriver driver;
    protected WebElement root;

    public BaseComponent(WebDriver driver, WebElement root) {
        this.driver = driver;
        this.root = root;
        PageFactory.initElements(new AjaxElementLocatorFactory(root, 10), this);
    }
}
""",
    "src/main/java/com/example/framework/components/HeaderComponent.java": """
package com.example.framework.components;

import com.example.framework.base.BaseComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

public class HeaderComponent extends BaseComponent<HeaderComponent> {

    @FindBy(css = "#logo")
    private WebElement logo;

    @FindBy(css = ".user-profile")
    private WebElement profileIcon;

    public HeaderComponent(WebDriver driver, WebElement root) {
        super(driver, root);
    }

    public boolean isLogoDisplayed() {
        return logo.isDisplayed();
    }

    public void clickProfile() {
        profileIcon.click();
    }

    @Override
    protected void load() {}

    @Override
    protected void isLoaded() throws Error {
        if (!logo.isDisplayed()) {
            throw new Error("HeaderComponent not loaded properly");
        }
    }
}
""",
    "src/test/java/com/example/framework/hooks/Hooks.java": """
package com.example.framework.hooks;

import com.example.framework.base.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Hooks {
    @Before
    public void beforeScenario() {
        WebDriver driver = new ChromeDriver();
        DriverManager.setDriver(driver);
    }

    @After
    public void afterScenario() {
        DriverManager.quitDriver();
    }
}
""",
    "src/test/java/com/example/framework/runner/TestRunner.java": """
package com.example.framework.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.example.framework.stepdefs",
    plugin = {"pretty", "html:target/cucumber-report.html"},
    monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {
}
""",
    "src/test/java/com/example/framework/stepdefs/SampleSteps.java": """
package com.example.framework.stepdefs;

import com.example.framework.base.DriverManager;
import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;

public class SampleSteps {
    WebDriver driver = DriverManager.getDriver();

    @Given("^user opens application$")
    public void openApp() {
        driver.get("https://example.com");
    }
}
"""
}

# Write each Java file to its location
for path, content in java_sources.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w") as f:
        f.write(content)

# Re-zip the project with Java files included
updated_zip_path = "/mnt/data/selenium-bdd-pom-utility-framework-fixed.zip"
with ZipFile(updated_zip_path, "w") as zipf:
    for root, dirs, files in os.walk(base_dir):
        for file in files:
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, base_dir)
            zipf.write(full_path, arcname=os.path.join("selenium-bdd-pom-utility-framework", rel_path))

updated_zip_path
