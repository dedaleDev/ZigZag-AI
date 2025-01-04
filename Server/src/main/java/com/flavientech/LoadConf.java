package com.flavientech;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadConf {
    private static Properties properties = new Properties();
    private static final String CONFIG_FILE_PATH = pathChecker.checkPath("application.properties");

    static {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading config file, please create or rename and complete the conf.example.properties file to " + CONFIG_FILE_PATH);
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
        properties.setProperty("comArduino", comArduino);
        saveProperties();
        return comArduino;
    }

    public static String writeEagleVoice(String voice) {
        properties.setProperty("voice", voice);
        saveProperties();
        return voice;
    }

    private static void saveProperties() {
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(output, null);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving config file to " + CONFIG_FILE_PATH, e);
        }
    }
}