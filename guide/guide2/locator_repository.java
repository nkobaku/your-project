package com.framework.locators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.config.ConfigReader;
import com.framework.model.ElementLocator;
import com.framework.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LocatorRepository {

    private static final String LOCATOR_FOLDER = "src/test/resources/locators";
    private static final Map<String, Map<String, ElementLocator>> locatorMap = new HashMap<>();

    static {
        loadAllLocatorFiles();
    }

    public static ElementLocator getLocator(String pageName, String key) {
        Map<String, ElementLocator> pageLocators = locatorMap.get(pageName);
        if (pageLocators == null) {
            throw new RuntimeException("No locators found for page/component: " + pageName);
        }
        ElementLocator locator = pageLocators.get(key);
        if (locator == null) {
            throw new RuntimeException("No locator found for key: " + key + " in page/component: " + pageName);
        }
        return locator;
    }

    private static void loadAllLocatorFiles() {
        File dir = new File(LOCATOR_FOLDER);
        if (!dir.exists()) throw new RuntimeException("Locator folder not found at: " + LOCATOR_FOLDER);

        String env = ConfigReader.getEnv();
        ObjectMapper mapper = new ObjectMapper();

        for (File file : FileUtils.getAllYamlFiles(dir)) {
            try {
                String fileName = file.getName();
                boolean isEnvFile = fileName.contains("-" + env);
                String basePageName = fileName
                        .replace("-" + env + ".yaml", "")
                        .replace(".yaml", "")
                        .replace(".yml", "");

                Map<String, Map<String, ElementLocator>> parsed = mapper.readValue(
                        file,
                        mapper.getTypeFactory().constructMapType(
                                Map.class,
                                String.class,
                                mapper.getTypeFactory().constructMapType(Map.class, String.class, ElementLocator.class)
                        )
                );

                locatorMap.putIfAbsent(basePageName, new HashMap<>());
                Map<String, ElementLocator> target = locatorMap.get(basePageName);

                for (Map.Entry<String, Map<String, ElementLocator>> compEntry : parsed.entrySet()) {
                    String compName = compEntry.getKey();
                    Map<String, ElementLocator> locators = compEntry.getValue();
                    for (Map.Entry<String, ElementLocator> loc : locators.entrySet()) {
                        String finalKey = compName + "." + loc.getKey();
                        if (isEnvFile || !target.containsKey(finalKey)) {
                            target.put(finalKey, loc.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse locator file: " + file.getName(), e);
            }
        }
        log.info("LocatorRepository initialized for env [{}] with {} pages/components.", env, locatorMap.size());
    }
}
