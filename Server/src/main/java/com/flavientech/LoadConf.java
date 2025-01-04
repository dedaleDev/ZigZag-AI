package com.flavientech;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadConf {
    private Properties properties;

    public LoadConf(String configFilePath) throws IOException {
        properties = new Properties();
        try (FileInputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
        }
    }

    public String getApiKeyPicoVoice() {
        return properties.getProperty("apiKeyPicoVoice");
    }

    public String getApiKeyOpenAi() {
        return properties.getProperty("apiKeyOpenAi");
    }

    public String getApiKeyWeather() {
        return properties.getProperty("apiKeyWeather");
    }

    public String getComArduino() {
        return properties.getProperty("comArduino");
    }
}