package com.yourcompany.yourproject.components;

import com.yourcompany.yourproject.base.BaseComponent;
import org.openqa.selenium.WebDriver;

public class LeftNavComponent extends BaseComponent {
    public LeftNavComponent(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void isLoaded() {
        // Add logic to verify left nav is loaded
    }
}
