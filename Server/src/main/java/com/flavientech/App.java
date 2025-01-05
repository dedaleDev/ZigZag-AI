//Auteur: Flavien Diéval
package com.flavientech;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class App implements AudioFileListener {
    private String apiKeyPicoVoice;
    private String apiKeyOpenAi;
    private String apiKeyWeather;
    private String comArduino;

    private ArduinoSerial arduinoSerial;
    private final CountDownLatch latch = new CountDownLatch(1);

    private boolean isRunning;
    public App() {
    this.isRunning = false;
        this.apiKeyPicoVoice = LoadConf.getApiKeyPicoVoice();
        this.apiKeyOpenAi = LoadConf.getApiKeyOpenAi();
        this.apiKeyWeather = LoadConf.getApiKeyWeather();
        this.comArduino = LoadConf.getComArduino();

        if (apiKeyPicoVoice == null || apiKeyOpenAi == null || apiKeyWeather == null || comArduino == null) {
            System.out.println("\u001B[31mErreur lors du chargement de la configuration. Vérifiez que le fichier Server/src/main/resources/application.properties est correctement configuré.\u001B[0m");
            return;
        }
        System.out.println(" _______       ______                        _____ ");
        System.out.println(" |___  (_)     |___  /                  /\\   |_   _|");
        System.out.println("    / / _  __ _   / / __ _  __ _       /  \\    | |  ");
        System.out.println("   / / | |/ _` | / / / _` |/ _` |     / /\\ \\   | |  ");
        System.out.println("  / /__| | (_| |/ /_| (_| | (_| |    / ____ \\ _| |_ ");
        System.out.println(" /_____|_|\\__, /_____\\__,_|\\__, |   /_/    \\_\\_____|");
        System.out.println("           __/ |            __/ |_____              ");
        System.out.println("          |___/            |___/______|             ");

        System.out.println("\u001B[32m\nConfiguration chargée avec succès !\n" +
                "-----------------------------------\n" +
                "API PicoVoice : " + apiKeyPicoVoice + "\n" +
                "API OpenAI    : " + apiKeyOpenAi + "\n" +
                "API Weather   : " + apiKeyWeather + "\n" +
                "COM Arduino   : " + comArduino + "\n" +
                "-----------------------------------\u001B[0m");
    }

    public void run() {
        // Initialiser la communication série avec l'Arduino
        arduinoSerial = new ArduinoSerial(comArduino, apiKeyPicoVoice);
        arduinoSerial.setAudioFileListener(this);
        arduinoSerial.start();
        // Garder le thread principal actif pour continuer à écouter les événements
        try {
            latch.await(); // Attendre indéfiniment
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        latch.countDown(); // Permettre au thread principal de se terminer
    }

    @Override
    public void onAudioFileReceived(String filePath) {
        // Convertir le fichier audio en format WAV
        new PythonController(PathChecker.checkPath("oggToWav.py")).runPythonScript(PathChecker.getCachesDir() + "RECEIVED.ogg", PathChecker.getCachesDir() + "request.wav");
        runVoiceAI();
    }


    /** -----------------------------------------------------------------runVoiceAI------------------------------------------------------
     * Exécute l'assistant vocal.
     -----------------------------------------------------------------------------------------------------------------------------------------*/
    public void runVoiceAI() {
        List<String> users = EagleController.getUsersVoiceList();
        // show users 
        System.out.println("Liste des utilisateurs : " + users);
        long startTime = System.currentTimeMillis();

        // -------- détection de l'utilisateur et Speech To Text --------  
        CompletableFuture<String> userDetectionFuture = CompletableFuture.supplyAsync(() -> detectSpeakingUser(users));
        CompletableFuture<String> SpeechToTextFuture = CompletableFuture.supplyAsync(this::runSpeechToText);
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(userDetectionFuture, SpeechToTextFuture); //attendre la fin

        combinedFuture.thenRun(() -> {// Attendre que les deux tâches soient terminées avant de continuer
            try {
                //récupérer les résultats
                String currentSpeakingUser = userDetectionFuture.get();
                System.out.println("Utilisateur courant : " + currentSpeakingUser);
                String userRequest = SpeechToTextFuture.get();

                long endTime = System.currentTimeMillis();
                System.out.println("Temps d'exécution de la detection d'utilisateur + STT : " + (endTime - startTime) + " ms");
                // --------------- Appel à l'API OpenAI ---------------
                String openAIResponse = InteractWithOpenAI.run(this.apiKeyOpenAi, currentSpeakingUser, userRequest);
                // --------------- Synthèse vocale de la réponse ---------------
                if (openAIResponse != null) {
                    new TextToSpeech(openAIResponse);
                }
                // --------------- Renvoie le fichier audio de la réponse IA à l'arduino ---------------
                //System.out.println("Envoi de la réponse à l'Arduino...");
                //arduinoSerial.sendAudioFileToArduino(PathChecker.getCachesDir() + "answer.wav");
                
                // --------------- Lecture de la réponse vocale sur haut parleur PC ---------------
                new SoundPlayer(PathChecker.getCachesDir() + "answer.wav");

                System.out.println("Request terminé ! Temps d'exécution total : " + (System.currentTimeMillis() - startTime) + " ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).join();  // Attendre que toutes les tâches soient terminées avant de terminer le programme.
    }


    /**------------------------------------------------------detectSpeakingUser------------------------------------------------------
     * Détecte l'utilisateur avec EagleController et exécute la reconnaissance vocale pour chaque utilisateur.
     * Retourne l'utilisateur avec le score le plus élevé.
     * @param users Liste des utilisateurs à tester
     -----------------------------------------------------------------------------------------------------------------------------------------*/
    private String detectSpeakingUser(List<String> users) {
        if (users == null || users.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé.");
            return "Utilisateur inconnu";
        }
        float[] scoreBoard = new float[users.size()];
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = new ArrayList<>();// CompletableFuture -> threads efficace selon doc
        for (int i = 0; i < users.size(); i++) {
            String user = users.get(i);
            int index = i;
            futures.add(CompletableFuture.runAsync(() -> {
                scoreBoard[index] = EagleController.runTest(apiKeyPicoVoice, user, PathChecker.getCachesDir() + "request.wav");
                System.out.println("Utilisateur: " + user + ", Score: " + scoreBoard[index]);
            }));
        }
        // --- running threads ---
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // --------- Calcul du score le plus élevé ---------
        float maxScore = 0;
        int maxIndex = -1;
        for (int i = 0; i < scoreBoard.length; i++) {
            float score = scoreBoard[i];
            if (score == -1) {
                System.out.println("\u001B[31mErreur lors de la reconnaissance vocale de l'utilisateur " + users.get(i) + "\u001B[0m");
                continue; 
            }
            if (score > maxScore) {
                maxScore = score;
                maxIndex = i;
            }
        }
        String detectedUser = "Utilisateur inconnu";
        if (maxIndex != -1 && maxScore > 0) {
            detectedUser = users.get(maxIndex);
        }
        System.out.println("Temps d'exécution  de la detection d'utilisateur : " + (System.currentTimeMillis() - startTime) + " ms");
        return detectedUser;
    }

    /** ------------------------------------------------------runSpeechToText------------------------------------------------------
     * Exécute la reconnaissance vocale pour convertir la voix en texte.
     -----------------------------------------------------------------------------------------------------------------------------------------*/
    private String runSpeechToText() {
        try {
            long startTime = System.currentTimeMillis();
            SpeechToText recognizer = new SpeechToText(this.apiKeyPicoVoice);
            String result = recognizer.run(PathChecker.getCachesDir() + "request.wav");
            System.out.println("\u001B[33mRequête utilisateur : " + result + "\u001B[0m");
            System.out.println("Temps d'exécution : du speech to text " + (System.currentTimeMillis() - startTime) + " ms");
            return result;
        } catch (Exception e) {
            if (e.getMessage().contains("0000012C")){
                System.out.println("\u001B[31mErreur STT : fichier audio incorrect ou inexistant\u001B[0m");
            }
            e.printStackTrace();
        }
        return null;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static void main(String[] args) {
        App app = new App();
        while (!app.isRunning()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Que voulez-vous lancer ?");
            System.out.println("1. Serveur Web");
            System.out.println("2. Arduino et Serveur Web");
            System.out.println("3. Quitter");
            System.out.print("Entrez votre choix (1/2) : ");
            int choix;
            try {
                choix = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("\u001B[31mEntrée invalide. Veuillez entrer un nombre.\u001B[0m");
                System.out.println("-----------------------------------");
                continue;
            }

            switch (choix) {
                case 1:// Démarrer le serveur web
                    try {
                        app.setRunning(true);
                        WebServer.main(args);
                        //afficher que le serveur à bien démarré, en vert, avec le port et l'adresse
                        System.out.println("\u001B[32mServeur web démarré avec succès sur http://localhost:8080/\u001B[0m");
                    } catch (Exception e) {
                        System.out.println("Erreur lors du démarrage du serveur web : " + e.getMessage());
                    }
                    break;
                case 2:// Démarrer les deux
                    app.setRunning(true);
                    try {
                        Thread appThread = new Thread(() -> app.run());
                        appThread.start();
                    } catch (Exception e) {
                        System.out.println("Erreur lors de l'exécution de la partie Arduino." + e.getMessage());
                    }
                    try {
                        WebServer.main(args);
                    } catch (Exception e) {
                        System.out.println("Erreur lors du démarrage du serveur web : " + e.getMessage());
                    }
                    break;
                case 3:// Quitter
                    System.out.println("Fermeture de l'application...");
                    break;
                default:
                    System.out.println("\u001B[31mChoix invalide. Veuillez entrer 1, 2 ou 3.\u001B[0m");
                    System.out.println("-----------------------------------");
                    break;
            }
            scanner.close();
        }
        app.setRunning(false);
    }
}