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


        /**
         * Vérifie et installe les dépendances Python nécessaires.
         */
        public static boolean checkAndInstallDependencies() {
        String[] dependencies = {"edge-tts", "pydub", "PyAudio", "pveagle"};
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        for (String dependency : dependencies) {
            if (!isDependencyInstalled(dependency)) {
            System.out.println("La dépendance " + dependency + " n'est pas installée. Voulez-vous tenter une installation automatique ? (y/n)");
            try {
                String response = reader.readLine().trim().toLowerCase();
                if ("y".equals(response)) {
                if (!installDependency(dependency)) {
                    System.out.println("Échec de l'installation de la dépendance : " + dependency);
                    return false;
                }
                } else {
                System.out.println("Installation de la dépendance " + dependency + " annulée par l'utilisateur.");
                return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            } else {
            System.out.println("La dépendance est déjà installée : " + dependency);
            }
        }
        return true;
        }

        /**
     * Vérifie si une dépendance Python est installée.
     */
    private static boolean isDependencyInstalled(String dependency) {
        List<String> command = new ArrayList<>();
        command.add(getPythonCommand());
        command.add("-m");
        command.add("pip");
        command.add("show");
        command.add(dependency);

        return executeProcess(command);
    }


    /**
     * Installe une dépendance Python via pip.
     */
    private static boolean installDependency(String dependency) {
        List<String> command = new ArrayList<>();
        command.add(getPythonCommand());
        command.add("-m");
        command.add("pip");
        command.add("install");
        command.add(dependency);

        return executeProcess(command);
    }

}