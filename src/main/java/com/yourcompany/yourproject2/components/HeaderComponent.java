package com.yourcompany.yourproject.components;

import com.yourcompany.yourproject.base.BaseComponent;
import org.openqa.selenium.WebDriver;

public class HeaderComponent extends BaseComponent {
    public HeaderComponent(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void isLoaded() {
        // Add logic to verify header is loaded
    }
}
