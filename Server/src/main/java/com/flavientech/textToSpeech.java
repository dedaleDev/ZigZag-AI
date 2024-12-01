package com.flavientech;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class textToSpeech {
    private String textVoice;

    public textToSpeech(String textVoice) {
        this.textVoice = textVoice.replace("*", "").replace("`", "").replace("_", "").replace("~", "");
        this.runEdgeTTS();
    }

    public void runEdgeTTS() {
        try {
            // Échapper correctement le texte pour qu'il soit entouré de guillemets
            String command = "python3 src/main/java/com/flavientech/edgeTTS.py \"" + this.textVoice + "\"";
            
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            
            // Démarrer le processus
            Process process = processBuilder.start();
            
            // Lire la sortie
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            // Lire les erreurs
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }
            
            // Attendre que le processus se termine
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}