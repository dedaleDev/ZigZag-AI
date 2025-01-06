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
    private static boolean executeProcess(List<String> command, boolean verbose) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            //processBuilder.command().forEach(System.out::println);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            if (verbose) {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);  // Affiche la sortie en temps réel
                }
            }

            int exitCode = process.waitFor();
            if (verbose) {
                System.out.println("Process terminé avec le code : " + exitCode);
            }
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean runPythonScript(String... args) {
        List<String> command = buildConvertCommand(args);
        //display command : 
        return executeProcess(command, true);
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
     * @return true si les dépendances sont installés et python compatible, false sinon.
    **/
    public static boolean checkAndInstallDependencies() {
        if (!isPythonVersionCompatible()) {
            System.out.println("La version de Python doit être 3.12.1 ou ultérieure.");
            return false;
        }
        String[] dependencies = {"edge-tts", "pydub", "pyaudio", "pveagle"};
        if (isPython13()) {
            dependencies = new String[]{"edge-tts", "pydub", "pyaudio", "pveagle", "audioop-lts"};
        }

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

        return executeProcess(command, false);
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

        return executeProcess(command, true);
    }

    public static boolean isPython13() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{getPythonCommand(), "--version"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String versionOutput = reader.readLine();
            process.waitFor();
    
            if (versionOutput != null && versionOutput.startsWith("Python")) {
                String version = versionOutput.split(" ")[1];
                String[] versionParts = version.split("\\.");
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1]);
                System.out.println("Python version : " + major + "." + minor);
                return (major > 3) || (major == 3 && minor >= 13);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    // ...existing code...

    /**
     * Vérifie si la version de Python est 3.12.0 ou ultérieure.
     */
    public static boolean isPythonVersionCompatible() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{getPythonCommand(), "--version"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String versionOutput = reader.readLine();
            process.waitFor();

            if (versionOutput != null && versionOutput.startsWith("Python")) {
                String version = versionOutput.split(" ")[1];
                String[] versionParts = version.split("\\.");
                int major = Integer.parseInt(versionParts[0]);
                int minor = Integer.parseInt(versionParts[1]);

                return (major > 3) || (major == 3 && minor >= 12);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}