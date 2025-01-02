package com.flavientech;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class pathChecker {
    public static String checkPath(String fileName) {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path serverDir = currentDir.resolve("Server");

        try {
            return Files.walk(serverDir)
                        .filter(Files::isRegularFile)
                        .filter(p -> !p.toString().contains("target")) // Exclude 'target' directory
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

    public static String getCachesDir(){
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        if (currentDir.endsWith("ZigZag")) {
            return currentDir.toString() + "/Server/src/caches/";
        }
        return currentDir + "../flavientech/com/java/caches/";
    }
    
    //public static void main(String[] args) {
        //System.out.println(checkPath("request.wav"));
       // System.out.println(getCachesDir() + "RECEIVED.ogg");
    //}
}