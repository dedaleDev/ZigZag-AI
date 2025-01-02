package com.flavientech;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PythonController {

    private final String SCRIPT_PATH;

    public PythonController(String SCRIPT_PATH) {
        this.SCRIPT_PATH = SCRIPT_PATH;
    }

    /**
     * Construit la commande pour exécuter le script Python.
     */
    private List<String> buildConvertCommand(String... args) {
        List<String> command = new ArrayList<>();
        command.add("python3");
        command.add(SCRIPT_PATH);
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    /**
     * Exécute un processus et hérite de la console pour afficher la sortie.
     */
    private static boolean executeProcess(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // Affiche la sortie en temps réel
            }

            int exitCode = process.waitFor();
            System.out.println("Process terminé avec le code : " + exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean runPythonScript(String... args) {
        List<String> command = buildConvertCommand(args);
        //display command : 
        System.out.println(command);
        return executeProcess(command);
    }
}