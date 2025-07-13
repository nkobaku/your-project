package com.yourcompany.yourproject.components;

import com.yourcompany.yourproject.base.BaseComponent;
import org.openqa.selenium.WebDriver;

public class FooterComponent extends BaseComponent {
    public FooterComponent(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void isLoaded() {
        // Add logic to verify footer is loaded
    }
}
