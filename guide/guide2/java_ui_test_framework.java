// ✅ PROJECT STRUCTURE OVERVIEW (JAVA SELENIUM BDD FRAMEWORK)

📁 com.elevate.testautomation
│
├── 📁 config
│   ├── ConfigReader.java
│   ├── Environment.java (enum for ist/uat)
│   ├── application-ist.yaml
│   ├── application-uat.yaml
│   └── config.properties
│
├── 📁 driver
│   ├── DriverFactory.java
│   └── DriverManager.java
│
├── 📁 utils
│   ├── WaitUtils.java
│   ├── AssertionUtils.java
│   ├── ElementUtils.java
│   ├── JavaScriptUtils.java
│   ├── ScreenshotUtils.java
│   ├── StepLogger.java
│   ├── WordEvidenceBuilder.java
│   ├── FileUtils.java
│   ├── TestDataUtils.java
│   └── LocatorValidator.java
│
├── 📁 locators
│   ├── ElementLocator.java
│   ├── LocatorRepository.java
│   └── homePage.yaml
│
├── 📁 annotations
│   └── VerifyOnLoad.java
│
├── 📁 components
│   ├── BaseComponent.java
│   ├── FlakyComponent.java
│   └── HeaderComponent.java
│
├── 📁 pages
│   ├── BasePage.java
│   └── LoginPage.java
│   └── DashboardPage.java
│
├── 📁 hooks
│   └── Hooks.java
│
├── 📁 runners
│   └── TestRunner.java
│
├── 📁 stepdefinitions
│   └── LoginSteps.java
│
├── 📁 data
│   ├── loginTest.yaml
│   ├── loginData.xlsx
│   └── loginData.json
│
├── 📁 features
│   └── login.feature
│
├── 📁 logs
│   └── log-test-<timestamp>.log
│
├── 📁 evidence
│   └── evidence-<testname>.docx
│
├── 📁 reports
│   └── cucumber-html-reports
│
└── 📄 build.gradle

---

// ✅ SAMPLE CONFIG FILES

// config.properties
execution.env=ist
screenshot.on.failure=true
screenshot.on.navigation=true
screenshot.on.navigation.delay=1000
headless=true
retry.count=2

// application-ist.yaml
browser: chrome
baseUrl: "https://testsite.ist.com"
timeouts:
  implicit: 5
  explicit: 15

// application-uat.yaml
browser: edge
baseUrl: "https://uatsite.com"
timeouts:
  implicit: 10
  explicit: 30

---

// ✅ build.gradle (summary)
plugins {
    id 'java'
    id 'application'
}

group = 'com.elevate'
version = '1.0.0'

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testImplementation 'io.cucumber:cucumber-java:7.15.0'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.15.0'
    testImplementation 'org.seleniumhq.selenium:selenium-java:4.19.1'
    implementation 'org.apache.poi:poi-ooxml:5.2.3'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1'
    implementation 'org.slf4j:slf4j-api:2.0.9'
    implementation 'ch.qos.logback:logback-classic:1.4.14'
    implementation 'org.docx4j:docx4j-core:11.4.10'
    implementation 'org.docx4j:docx4j-documents4j:11.4.10'
    implementation 'org.yaml:snakeyaml:2.2'
    implementation 'org.apache.commons:commons-collections4:4.4'
    implementation 'org.apache.commons:commons-io:1.3.2'
    compileOnly 'org.projectlombok:lombok:1.18.32'
    annotationProcessor 'org.projectlombok:lombok:1.18.32'
}

test {
    useJUnitPlatform()
    systemProperty 'cucumber.filter.tags', System.getProperty('cucumber.filter.tags', '')
}

def timestamp = new Date().format("yyyyMMdd-HHmmss")
application {
    mainClass = 'com.elevate.runners.TestRunner'
}

---

// ✅ run.sh
#!/bin/bash
./gradlew clean test -Dcucumber.filter.tags="$1"

// ✅ run.bat
@echo off
gradlew.bat clean test -Dcucumber.filter.tags=%1

---

// ✅ logback.xml (placed under resources)
<configuration>
  <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/test-log-%d{yyyy-MM-dd_HH-mm-ss}.log</file>
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>

  <root level="debug">
    <appender-ref ref="FILE"/>
  </root>
</configuration>

---

// ✅ launcher: TestRunner.java
package com.elevate.runners;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty, html:reports/cucumber-html-reports/report.html")
@ConfigurationParameter(key = "cucumber.glue", value = "com.elevate.stepdefinitions,com.elevate.hooks")
public class TestRunner {}

---

This full structure, config, and Gradle setup is now ready for:
- Component-based Selenium BDD testing
- Parallel-safe execution
- Word + HTML reporting
- Screenshot & evidence handling
- YAML/Excel/JSON data + locators
- Reflection-based page/component validation

Let me know and I’ll generate the next file/code you need. ✅
