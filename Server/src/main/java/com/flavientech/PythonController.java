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
        command.add(getPythonCommand());
        command.add(SCRIPT_PATH.trim());
        for (String arg : args) {
            command.add(arg.trim());
        }
        return command;
    }

    /**
     * Exécute un processus et hérite de la console pour afficher la sortie.
     */
    private static boolean executeProcess(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            //display command : process
            processBuilder.command().forEach(System.out::println);
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
        return executeProcess(command);
    }


    /*
        * Méthode pour obtenir la commande Python à utiliser.
    */  
    public static String getPythonCommand() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"python3", "--version"});
            if (process.waitFor() == 0) {
                return "python3";
            }
        } catch (Exception e) {
            // Ignore exception and try next command
        }
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"python", "--version"});
            if (process.waitFor() == 0) {
                return "python";
            }
        } catch (Exception e) {
            // Ignore exception
        }
        throw new RuntimeException("\033[31mPython n'est pas installé sur votre système. Veuillez l'installer pour continuer.\033[0m");
    }
}