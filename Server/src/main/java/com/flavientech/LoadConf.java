package com.flavientech;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadConf {
    private static Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream(pathChecker.checkPath("application.properties"))) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading config file, please create or rename and complete the conf.example.properties file to " + pathChecker.checkPath("conf.properties"));
        }
    }

    public static String getApiKeyPicoVoice() {
        return properties.getProperty("apiKeyPicoVoice");
    }

    public static String getApiKeyOpenAi() {
        return properties.getProperty("apiKeyOpenAi");
    }

    public static String getApiKeyWeather() {
        return properties.getProperty("apiKeyWeather");
    }

    public static String getComArduino() {
        return properties.getProperty("comArduino");
    }

    public static String getVoice() {
        return properties.getProperty("voice");
    }

    public static String writeArduinoCom(String comArduino) {
        return properties.setProperty("comArduino", comArduino).toString();
    }

    public static String writeEagleVoice(String voice) {
        return properties.setProperty("voice", voice).toString();
    }
}