package com.flavientech;

import com.flavientech.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class EagleController {

    private static final String SCRIPT_PATH = PathChecker.checkPath("voiceRecognition.py");
    private static final String USERS_DIR = PathChecker.getCachesDir() + "users";

    private static UserController userController;

    @Autowired
    public EagleController(UserController userController) {
        EagleController.userController = userController;
    }

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

        // Crée l'utilisateur dans la base de données
        if (userController.getUserByUsername(userName).isEmpty()) {
            userController.createUser(userName);
        }

        List<String> command = buildEnrollCommand(accessKey, userName);
        System.out.println("Enroll: " + command);
        return executeProcess(command);
    }

    /**
     * Teste un fichier audio avec le modèle de l'utilisateur.
     *
     * @param accessKey Clé d'accès.
     * @param userName  Nom d'utilisateur.
     * @param audioPath Chemin du fichier audio.
     * @return Score ou -1 si erreur.
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
     * Liste les utilisateurs enrôlés (base de données).
     */
    public static List<String> getUsersVoicesList() {
        return userController.getAllUsernames();
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
        userController.deleteUserByUsername(userName);

        // Supprime les fichiers associés
        File userFile = new File(USERS_DIR + "/" + userName + ".eagle");
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    public static List<String> getUsersVoiceList() {
        return userController.getAllUsernames(); // Accès à l'instance
    }

    public static List<String> getUsersVoiceListInFolder() {
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

    // Méthodes privées pour construire les commandes Python
    private static List<String> buildEnrollCommand(String accessKey, String userName) {
        List<String> command = new ArrayList<>();
        command.add("python3");
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

    private static List<String> buildTestCommand(String accessKey, String userName, String audioPath) {
        List<String> command = new ArrayList<>();
        command.add("python3");
        command.add(SCRIPT_PATH);
        command.add("test");
        command.add(accessKey);
        command.add("--model_path");
        command.add(USERS_DIR + "/" + userName + ".eagle");
        command.add("--audio_path");
        command.add(audioPath);
        return command;
    }

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

    private static float executeProcessAndGetScore(List<String> command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Process terminé avec le code : " + exitCode);

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