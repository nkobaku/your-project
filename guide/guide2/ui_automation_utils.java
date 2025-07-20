package com.framework.utils;

import com.framework.config.ConfigReader;
import com.framework.logging.StepLogger;
import com.framework.reporting.WordEvidenceBuilder;
import com.framework.utils.driver.DriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WaitUtils {
    private static final Map<String, By> cachedLocators = new ConcurrentHashMap<>();
    private static final int DEFAULT_TIMEOUT = ConfigReader.getInt("wait.default.timeout", 10);
    private static final int DEFAULT_RETRY = ConfigReader.getInt("wait.default.retry", 3);

    public static WebDriverWait getWait(int timeoutSeconds) {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(timeoutSeconds));
    }

    public static WebElement waitForElementVisible(By locator) {
        return waitForElementVisible(locator, DEFAULT_TIMEOUT);
    }

    public static WebElement waitForElementVisible(By locator, int timeout) {
        try {
            return getWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            StepLogger.error("Element not visible: " + locator);
            WordEvidenceBuilder.captureScreenshot("Element Not Visible", locator.toString());
            throw e;
        }
    }

    public static boolean waitForElementInvisible(By locator, int timeout) {
        try {
            return getWait(timeout).until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            StepLogger.warn("Element still visible after timeout: " + locator);
            return false;
        }
    }

    public static void waitUntilPageLoads() {
        ExpectedCondition<Boolean> pageLoadCondition = driver ->
                ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
        getWait(DEFAULT_TIMEOUT).until(pageLoadCondition);
    }

    public static boolean retryUntilTrue(String description, int attempts, int delayMs, RetryableCondition condition) {
        for (int i = 1; i <= attempts; i++) {
            try {
                if (condition.check()) {
                    log.info("Retry success [{}] attempt {}/{}", description, i, attempts);
                    return true;
                }
                Thread.sleep(delayMs);
            } catch (Exception e) {
                log.warn("Retry failed on attempt {} due to: {}", i, e.getMessage());
            }
        }
        log.error("Retry exhausted: {} after {} attempts", description, attempts);
        return false;
    }

    public interface RetryableCondition {
        boolean check() throws Exception;
    }
}


@Slf4j
public class AssertionUtils {
    private static final ThreadLocal<List<String>> softErrors = ThreadLocal.withInitial(ArrayList::new);

    public static void assertTrue(boolean condition, String message) {
        try {
            if (!condition) {
                WordEvidenceBuilder.captureScreenshot("Assertion Failed", message);
                StepLogger.error("Assertion Failed: " + message);
                throw new AssertionError(message);
            } else {
                StepLogger.info("Assertion Passed: " + message);
            }
        } catch (Throwable t) {
            throw t;
        }
    }

    public static void softAssertTrue(boolean condition, String message) {
        if (!condition) {
            StepLogger.warn("Soft Assertion Failed: " + message);
            softErrors.get().add(message);
        } else {
            StepLogger.info("Soft Assertion Passed: " + message);
        }
    }

    public static void assertAll() {
        List<String> errors = softErrors.get();
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Soft assertion failures:\n");
            errors.forEach(msg -> sb.append("- ").append(msg).append("\n"));
            softErrors.remove();
            throw new AssertionError(sb.toString());
        }
    }
}

@Slf4j
public class ElementUtils {
    public static void click(By locator, String page, String description) {
        WebElement element = WaitUtils.waitForElementVisible(locator);
        highlight(element);
        element.click();
        StepLogger.info("Clicked on: {} | {} | {}", page, description, locator);
        WordEvidenceBuilder.captureScreenshot("Click: " + description, locator.toString());
    }

    public static void sendKeys(By locator, String value, String page, String description) {
        WebElement element = WaitUtils.waitForElementVisible(locator);
        element.clear();
        element.sendKeys(value);
        StepLogger.info("SendKeys to: {} | {} | {} = '{}'", page, description, locator, value);
        WordEvidenceBuilder.captureScreenshot("SendKeys: " + description, locator.toString());
    }

    public static void highlight(WebElement element) {
        JavascriptUtils.highlightElement(element);
    }
}

@Slf4j
public class JavaScriptUtils {
    public static void highlightElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        js.executeScript("arguments[0].style.border='3px solid red'", element);
    }

    public static void scrollIntoView(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public static void clickWithJS(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) DriverManager.getDriver();
        js.executeScript("arguments[0].click();", element);
    }
}

@Slf4j
public class ScreenshotUtils {
    public static File takeScreenshot(String name) {
        File src = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
        String path = FileUtils.getEvidencePath(name);
        try {
            Files.copy(src.toPath(), Paths.get(path));
            return new File(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save screenshot: " + path, e);
        }
    }
}

public class FileUtils {
    private static final String EVIDENCE_DIR = "target/evidence";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

    public static String getEvidencePath(String name) {
        String timestamp = FORMAT.format(new Date());
        String filename = name.replaceAll("[^a-zA-Z0-9-_]", "_") + "_" + timestamp + ".png";
        new File(EVIDENCE_DIR).mkdirs();
        return EVIDENCE_DIR + "/" + filename;
    }
}

public class TestDataUtils {
    public static Map<String, Object> loadYaml(String path) {
        try (FileInputStream input = new FileInputStream(path)) {
            return new org.yaml.snakeyaml.Yaml().load(input);
        } catch (IOException e) {
            throw new RuntimeException("YAML load failed: " + path, e);
        }
    }

    public static List<Map<String, String>> loadExcel(String path, String sheet) {
        // Simplified for brevity
        return Collections.emptyList();
    }

    public static Map<String, Object> loadJson(String path) {
        // Simplified for brevity
        return Collections.emptyMap();
    }
}

public class LocatorValidator {
    public static void validate(Map<String, Object> locatorMap) {
        for (Map.Entry<String, Object> entry : locatorMap.entrySet()) {
            if (!(entry.getValue() instanceof Map)) {
                throw new RuntimeException("Invalid locator format for: " + entry.getKey());
            }
        }
    }
}
