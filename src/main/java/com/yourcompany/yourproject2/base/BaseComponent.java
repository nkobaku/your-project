package com.yourcompany.yourproject.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class BaseComponent {
    protected WebDriver driver;

    public BaseComponent(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        isLoaded();
    }

    protected abstract void isLoaded();
}
