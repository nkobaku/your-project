📦 src/test/java
└── com
    └── framework
        ├── annotations
        │   └── VerifyOnLoad.java
        ├── components
        │   ├── BaseComponent.java
        │   └── retry
        │       └── RetryCondition.java
        ├── core
        │   ├── BasePage.java
        │   ├── DriverManager.java
        │   └── LoadableComponent.java
        ├── reporting
        │   └── WordEvidenceBuilder.java
        └── utils
            └── ReflectionUtils.java

└── com
    └── app
        ├── components
        │   ├── AccountSection.java
        │   └── DetailsForm.java
        └── pages
            └── DashboardPage.java


// --- annotations/VerifyOnLoad.java ---
package com.framework.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VerifyOnLoad {
    int retryCount() default 3;
    long retryDelayMs() default 1000;
}

// --- components/BaseComponent.java ---
package com.framework.components;

import com.framework.annotations.VerifyOnLoad;
import com.framework.reporting.WordEvidenceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.Field;

@Slf4j
public abstract class BaseComponent {
    protected final WebDriver driver;

    public BaseComponent(WebDriver driver) {
        this.driver = driver;
    }

    public abstract boolean isLoaded();

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void validateNestedComponents(WordEvidenceBuilder evidenceBuilder) {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(VerifyOnLoad.class)) continue;

            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (!(value instanceof BaseComponent nested)) continue;

                VerifyOnLoad annotation = field.getAnnotation(VerifyOnLoad.class);

                boolean success = retryWithDelay(
                        annotation.retryCount(),
                        annotation.retryDelayMs(),
                        () -> {
                            boolean loaded = nested.isLoaded();
                            evidenceBuilder.logComponentCheck(nested.getName(), loaded);
                            return loaded;
                        }
                );

                if (!success) {
                    throw new AssertionError("Nested Component " + nested.getName() + " failed to load.");
                }

                nested.validateNestedComponents(evidenceBuilder);

            } catch (Exception e) {
                throw new RuntimeException("Error validating nested component: " + field.getName(), e);
            }
        }
    }

    private boolean retryWithDelay(int maxRetries, long delayMs, RetryCondition condition) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                if (condition.check()) return true;
            } catch (Exception ignored) {}
            attempts++;
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    @FunctionalInterface
    public interface RetryCondition {
        boolean check();
    }
}

// --- app/components/AccountSection.java ---
package com.app.components;

import com.framework.annotations.VerifyOnLoad;
import com.framework.components.BaseComponent;
import org.openqa.selenium.WebDriver;

public class AccountSection extends BaseComponent {

    @VerifyOnLoad
    private final DetailsForm detailsForm;

    public AccountSection(WebDriver driver) {
        super(driver);
        this.detailsForm = new DetailsForm(driver);
    }

    @Override
    public boolean isLoaded() {
        return true; // TODO: add locator checks
    }
}

// --- app/components/DetailsForm.java ---
package com.app.components;

import com.framework.components.BaseComponent;
import org.openqa.selenium.WebDriver;

public class DetailsForm extends BaseComponent {
    public DetailsForm(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isLoaded() {
        return true; // TODO: add field presence check
    }
}

// --- app/pages/DashboardPage.java ---
package com.app.pages;

import com.app.components.AccountSection;
import com.framework.annotations.VerifyOnLoad;
import com.framework.core.BasePage;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage<DashboardPage> {

    @VerifyOnLoad
    private final AccountSection accountSection;

    public DashboardPage(WebDriver driver) {
        super(driver);
        this.accountSection = new AccountSection(driver);
    }

    @Override
    protected void load() {
        // nav logic if needed
    }

    @Override
    protected void isLoadedAssert() throws Error {
        validateAnnotatedComponents();
    }
}

// --- core/BasePage.java ---
package com.framework.core;

import com.framework.annotations.VerifyOnLoad;
import com.framework.components.BaseComponent;
import com.framework.reporting.WordEvidenceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.LoadableComponent;

import java.lang.reflect.Field;

@Slf4j
public abstract class BasePage<T extends LoadableComponent<T>> extends LoadableComponent<T> {
    protected final WebDriver driver;
    protected final WordEvidenceBuilder evidenceBuilder;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.evidenceBuilder = WordEvidenceBuilder.getInstance();
    }

    protected void validateAnnotatedComponents() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(VerifyOnLoad.class)) continue;

            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (!(value instanceof BaseComponent component)) continue;

                VerifyOnLoad ann = field.getAnnotation(VerifyOnLoad.class);
                boolean result = retryWithDelay(
                        ann.retryCount(),
                        ann.retryDelayMs(),
                        () -> {
                            boolean loaded = component.isLoaded();
                            evidenceBuilder.logComponentCheck(component.getName(), loaded);
                            return loaded;
                        });

                if (!result) throw new AssertionError("Component failed: " + component.getName());

                component.validateNestedComponents(evidenceBuilder);

            } catch (Exception e) {
                throw new RuntimeException("Component validation failed: " + field.getName(), e);
            }
        }
    }

    private boolean retryWithDelay(int maxRetries, long delayMs, BaseComponent.RetryCondition condition) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                if (condition.check()) return true;
            } catch (Exception ignored) {}
            attempts++;
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
        }
        return false;
    }
}

// --- reporting/WordEvidenceBuilder.java ---
package com.framework.reporting;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WordEvidenceBuilder {
    private static final ThreadLocal<WordEvidenceBuilder> INSTANCE = ThreadLocal.withInitial(WordEvidenceBuilder::new);

    public static WordEvidenceBuilder getInstance() {
        return INSTANCE.get();
    }

    public void logComponentCheck(String name, boolean result) {
        log.info("[EVIDENCE] ✅ Component: {} - {}", name, result ? "Loaded" : "FAILED");
    }

    public void startComponentGroup(String name) {
        log.info("[EVIDENCE] === Component Group: {} ===", name);
    }

    // TODO: build actual Word doc
}
