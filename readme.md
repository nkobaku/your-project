# Selenium BDD POM Utility Framework with Full Source

This repository contains a full Java-based Selenium framework with BDD (Cucumber), Page Object Model, LoadableComponent model, and reusable components/utilities.

---

## 📁 Project Structure and File Contents

### 📄 `pom.xml`
```
<!-- Add your full Maven configuration here if needed -->
```

---

### 📁 `src/main/java/com/example/framework/base/`

#### 📄 BasePage.java
```java
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
```

#### 📄 BaseComponent.java
```java
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
```

#### 📄 DriverManager.java
```java
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
```

---

### 📁 `components/`

#### 📄 HeaderComponent.java
```java
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
```

#### 📄 FooterComponent.java
```java
package com.example.framework.components;

import com.example.framework.base.BaseComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

public class FooterComponent extends BaseComponent<FooterComponent> {

    @FindBy(css = "footer")
    private WebElement footer;

    public FooterComponent(WebDriver driver, WebElement root) {
        super(driver, root);
    }

    public boolean isFooterVisible() {
        return footer.isDisplayed();
    }

    @Override
    protected void load() {}

    @Override
    protected void isLoaded() throws Error {
        if (!footer.isDisplayed()) {
            throw new Error("FooterComponent not loaded properly");
        }
    }
}
```

#### 📄 LeftNavComponent.java
```java
package com.example.framework.components;

import com.example.framework.base.BaseComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

public class LeftNavComponent extends BaseComponent<LeftNavComponent> {

    @FindBy(css = "nav.sidebar")
    private WebElement sidebar;

    public LeftNavComponent(WebDriver driver, WebElement root) {
        super(driver, root);
    }

    public boolean isSidebarVisible() {
        return sidebar.isDisplayed();
    }

    @Override
    protected void load() {}

    @Override
    protected void isLoaded() throws Error {
        if (!sidebar.isDisplayed()) {
            throw new Error("LeftNavComponent not loaded properly");
        }
    }
}
```

---

### 📁 `utils/`

#### 📄 ConfigReader.java
```java
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
```

#### 📄 WaitUtils.java
```java
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
```

#### 📄 JavaScriptUtils.java
```java
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
```

#### 📄 ScreenshotUtils.java
```java
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
```

#### 📄 LogUtils.java
```java
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
```

(continued in next update...)

