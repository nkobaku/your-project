package com.yourcompany.yourproject.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.yourcompany.yourproject.stepdefs",
        plugin = {"pretty", "html:target/cucumber-reports.html"},
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests { }
