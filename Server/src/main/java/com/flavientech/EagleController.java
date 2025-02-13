package com.flavientech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.flavientech.service.DatabaseController;

public class EagleController extends Thread {

    private static final String SCRIPT_PATH = PathChecker.checkPath("voiceRecognition.py");
    private static final String USERS_DIR = PathChecker.getCachesDir() +"users";

    /**
     * Exécute la commande Python pour l'enrôlement d'un utilisateur.
     *
     * @param accessKey Clé d'accès pour l'authentification.
     * @param userName  Nom de l'utilisateur à enrôler.
     * @return Retourne -1 si une erreur survient ou si le nom d'utilisateur est null.
     */
    /**
     * Enrôle un utilisateur (base de données + fichier).
     *
     * @param accessKey Clé d'accès pour l'authentification.
     * @param userName  Nom d'utilisateur.
     * @return -1 si une erreur survient, sinon le résultat.
     */
    public static float runEnroll(String accessKey, String userName) {
        if (userName == null || accessKey == null) {
            return -1;
        }
        
        List<String> command = buildEnrollCommand(accessKey, userName);
        System.out.println("Enroll: " + command);
        float result = executeProcess(command);
        if (result != -1) {
            // Crée l'utilisateur dans la base de données
            DatabaseController db = new DatabaseController();
            db.newUser(userName);
            db.closeConnection();
        }
        return result;
    }

    /**
     * Exécute la commande Python pour tester un fichier audio avec le modèle de l'utilisateur.
     *
     * @param accessKey Clé d'accès pour l'authentification.
     * @param userName  Nom de l'utilisateur.
     * @param audioPath Chemin du fichier audio à tester.
     * @return Retourne le score de l'audio ou -1 si une erreur est rencontrée.
     */
    public static float runTest(String accessKey, String userName, String audioPath) {
        if (audioPath == null || userName == null || accessKey == null) {
            return -1;
        }

        File audioFile = new File(audioPath);
        if (!audioFile.exists() || !audioFile.isFile()) {
            return -1;
        }

        List<String> command = buildTestCommand(accessKey, userName, audioPath);
        return executeProcessAndGetScore(command);
    }

    /**
     * Récupère la liste des utilisateurs enrôlés (fichiers .eagle).
     *
     * @return Liste des utilisateurs ou null si aucun fichier trouvé.
     */
    public static List<String> getUsersVoiceList() {
        File directory = new File(USERS_DIR);
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        @SuppressWarnings("unused")
        FilenameFilter filter = (dir, name) -> name.endsWith(".eagle");
        String[] files = directory.list(filter);
        if (files == null) {
            return null;
        }

        List<String> users = new ArrayList<>();
        for (String file : files) {
            users.add(file.replace(".eagle", ""));
        }
        return users;
    }

    
    /**
     * Supprime un utilisateur (base de données + fichiers).
     *
     * @param userName Nom d'utilisateur.
     */
    public static void deleteUser(String userName) {
        if (userName == null) {
            return;
        }

        // Supprime l'utilisateur de la base de données
        DatabaseController db = new DatabaseController();
        db.deleteUser(userName);
        db.closeConnection();

        // Supprime les fichiers associés
        File userFile = new File(USERS_DIR + "/" + userName + ".eagle");
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    
    /**
     * Construit la commande pour l'enrôlement.
     */
    private static List<String> buildEnrollCommand(String accessKey, String userName) {
        List<String> command = new ArrayList<>();
        command.add(PythonController.getPythonCommand());
        command.add(SCRIPT_PATH.trim());
        command.add("enroll");
        command.add(accessKey.trim());
        command.add("--user_name");
        command.add(userName.trim());
        command.add("--audio_path");
        command.add(PathChecker.getCachesDir() + "enroll.wav");
        command.add("--output_profile_path");
        command.add(USERS_DIR + "/" + userName.trim() + ".eagle");
        return command;
    }

    /**
     * Construit la commande pour le test audio.
     */
    private static List<String> buildTestCommand(String accessKey, String userName, String audioPath) {
        List<String> command = new ArrayList<>();
        command.add(PythonController.getPythonCommand());
        command.add(SCRIPT_PATH);
        command.add("test");
        command.add(accessKey);
        command.add("--model_path");
        command.add(USERS_DIR + "/" + userName + ".eagle");
        command.add("--audio_path");
        command.add(audioPath);
        return command;
    }

    /**
     * Exécute un processus et hérite de la console pour afficher la sortie.
     */
    private static float executeProcess(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("Process terminé avec le code : " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Exécute un processus et retourne le score si disponible dans la sortie.
     */
    private static float executeProcessAndGetScore(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);  // Redirige stderr vers stdout pour capturer les deux
    
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
    
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);  // Affiche la sortie en temps réel
            }
    
            int exitCode = process.waitFor();
            System.out.println("Process terminé avec le code : " + exitCode);
    
            // Vérifions si la sortie contient un score final de reconnaissance vocale
            String outputStr = output.toString();
            if (outputStr.contains("Score final:")) {
                return Float.parseFloat(outputStr.substring(outputStr.indexOf("Score final:") + 13).trim());
            }
    
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }
}