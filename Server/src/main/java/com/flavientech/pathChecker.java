package com.flavientech;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;

public class pathChecker {
    public static String checkPath(String fileName) {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        try {
            return Files.walk(currentDir)
                        .filter(Files::isRegularFile)
                        .filter(p -> !p.toString().contains("target"))
                        .filter(p -> p.getFileName().toString().equals(fileName))
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .findFirst()
                        .orElse("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while searching for the file";
        }
    }

    public static String getCachesDir() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path cachesDir = currentDir.resolve("Server/src/caches");
        if (Files.exists(cachesDir)) {
            return cachesDir.toAbsolutePath().toString() + File.separator;
        } else {
            cachesDir = currentDir.resolve("src/caches");
            if (Files.exists(cachesDir)) {
                return cachesDir.toAbsolutePath().toString() + File.separator;
            } else {
                return "Caches directory not found";
            }
        }
    }
    
    //public static void main(String[] args) {
        //System.out.println(checkPath("request.wav"));
       // System.out.println(getCachesDir() + "RECEIVED.ogg");
    //}
}