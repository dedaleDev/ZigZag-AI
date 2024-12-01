package com.flavientech;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Memory {

    private static final Path LONG_MEMORY_PATH = Paths.get("src/caches/longMemory.txt");
    private static final Path FLASH_MEMORY_PATH = Paths.get("src/caches/flashMemory.txt");
    private static final Path SETTINGS_PATH = Paths.get("src/caches/settings.txt");

    /**
     * Read data from a file
     * @param path Path to the file
     * @return Data as a string
     */
    private static String readFileAsString(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            return Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Write data to a file
     * @param path Path to the file
     * @param data Data to write
     */
    private static void writeFile(Path path, String data) {
        try {
            Files.writeString(path, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add data to the long memory
     * @param fullData Data to add
     * @return Data without the part added to the long memory
     */
    public static String addData(String fullData) {
        String longMemory = readFileAsString(LONG_MEMORY_PATH);
        int startIndex = fullData.indexOf('@');
        int endIndex = fullData.indexOf('@', startIndex + 1);

        if (startIndex != -1 && endIndex != -1) {
            // Extraction de la sous-chaîne entre les @
            longMemory += "\n" + fullData.substring(startIndex + 1, endIndex);
            // Mise à jour du fichier longMemory.txt
            writeFile(LONG_MEMORY_PATH, longMemory);
            return fullData.substring(0, startIndex) + fullData.substring(endIndex + 1);
        }
        return fullData;
    }

    /**
     * Refresh the flash memory
     * @param data
     */
    public static void refreshFlashMemory(String data) {
        String flashMemory = readFileAsString(FLASH_MEMORY_PATH) + "\n" + data;
        writeFile(FLASH_MEMORY_PATH, flashMemory);
    }

    // Récupérer la mémoire longue
    public static String getLongMemory() {
        return readFileAsString(LONG_MEMORY_PATH);
    }

    // Augmenter le compteur de 1
    public static void add1ToCmpt() {
        String data = readFileAsString(SETTINGS_PATH);
        if (!data.contains("cmpt:")) {
            data = "cmpt:0";
        }

        int startIndex = data.indexOf("cmpt:") + 5;
        int endIndex = data.indexOf("\n", startIndex);
        if (endIndex == -1) endIndex = data.length();

        try {
            int cmpt = Integer.parseInt(data.substring(startIndex, endIndex));
            cmpt++;
            data = data.substring(0, startIndex) + cmpt + data.substring(endIndex);
            writeFile(SETTINGS_PATH, data);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // Obtenir la valeur du compteur
    public static int getCmpt() {
        String data = readFileAsString(SETTINGS_PATH);
        if (!data.contains("cmpt:")) {
            writeFile(SETTINGS_PATH, "cmpt:0");
            return 0;
        }

        int startIndex = data.indexOf("cmpt:") + 5;
        int endIndex = data.indexOf("\n", startIndex);
        if (endIndex == -1) endIndex = data.length();

        try {
            return Integer.parseInt(data.substring(startIndex, endIndex));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Réinitialiser le compteur
    public static void resetCmpt() {
        String data = readFileAsString(SETTINGS_PATH);
        int startIndex = data.indexOf("cmpt:") + 5;

        if (startIndex == 4) { // Si "cmpt:" n'est pas trouvé
            System.err.println("Erreur : 'cmpt:' introuvable dans 'settings.txt'");
            return;
        }

        int endIndex = data.indexOf("\n", startIndex);
        if (endIndex == -1) endIndex = data.length();

        data = data.substring(0, startIndex) + "0" + data.substring(endIndex);
        writeFile(SETTINGS_PATH, data);
    }

    // Récupérer la mémoire flash
    public static String getFlashMemory() {
        String flashMemory = readFileAsString(FLASH_MEMORY_PATH).replaceAll("\n", " ");

        // Si le compteur est supérieur à 1 et que la mémoire flash est trop grande, la supprimer
        if (getCmpt() > 1 && flashMemory.length() > 500) {
            System.out.println("Flash memory will be deleted");
            deleteFlashMemory();
        }

        add1ToCmpt();
        return flashMemory;
    }

    // Supprimer la mémoire longue
    public static void deleteLongMemory() {
        writeFile(LONG_MEMORY_PATH, "");
    }

    // Supprimer la mémoire flash
    public static void deleteFlashMemory() {
        resetCmpt();
        writeFile(FLASH_MEMORY_PATH, "");
        System.out.println("Flash memory deleted");
    }
}