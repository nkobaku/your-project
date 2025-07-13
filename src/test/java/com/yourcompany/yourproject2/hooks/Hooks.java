package com.yourcompany.yourproject.hooks;

import com.yourcompany.yourproject.base.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {
    @Before
    public void setUp() {
        // Setup before scenario
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
