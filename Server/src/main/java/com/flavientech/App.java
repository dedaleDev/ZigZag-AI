package com.flavientech;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class App {
    private final String apiKeyPicoVoice = "NA0NuP+5Orn3NUuj8UHB6Sj1VaolSuM2qvYlQeeWbYs6epGzsACtYA==";  
    private final String apiKeyOpenAi = "sk-svcacct-zjaiQzqp_UELmtfRWJJdISaTt3ICHrOkeOU3tufzXe_ijWOoff8uWskYJK3yFxPT3BlbkFJeH3bRVzGMlZqLrrrynoDlsjKOl9ekB5QuKFTcJ6E0jM5-KqRgeiz2z2d43TW4AA";  
    private final String apiKeyWeather = "e7e87ddf5ee17846572597c477aa9b95"; 

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    /**
     * Exécute l'assistant vocal.
     */
    public void run() {
        List<String> users = EagleController.getUsersVoiceList();
        // show users 
        System.out.println("Liste des utilisateurs : " + users);
        long startTime = System.currentTimeMillis();

        // -------- détection de l'utilisateur et Speech To Text --------  
        CompletableFuture<String> userDetectionFuture = CompletableFuture.supplyAsync(() -> detectSpeakingUser(users));
        CompletableFuture<String> speechToTextFuture = CompletableFuture.supplyAsync(this::runSpeechToText);
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(userDetectionFuture, speechToTextFuture); //attendre la fin

        combinedFuture.thenRun(() -> {// Attendre que les deux tâches soient terminées avant de continuer
            try {
                //récupérer les résultats
                String currentSpeakingUser = userDetectionFuture.get();
                System.out.println("Utilisateur courant : " + currentSpeakingUser);
                String userRequest = speechToTextFuture.get();

                long endTime = System.currentTimeMillis();
                System.out.println("Temps d'exécution de la detection d'utilisateur + STT : " + (endTime - startTime) + " ms");
                // --------------- Appel à l'API OpenAI ---------------
                String openAIResponse = interactWithOpenAI(currentSpeakingUser, userRequest);

                // --------------- Synthèse vocale de la réponse ---------------
                if (openAIResponse != null) {
                    new textToSpeech(openAIResponse);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).join();  // Attendre que toutes les tâches soient terminées avant de terminer le programme.
    }

    /**
     * Détecte l'utilisateur avec EagleController et exécute la reconnaissance vocale pour chaque utilisateur.
     * Retourne l'utilisateur avec le score le plus élevé.
     * @param users Liste des utilisateurs à tester
     */
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
                scoreBoard[index] = EagleController.runTest(apiKeyPicoVoice, user, "src/caches/request.wav");
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

    /**
     * Exécute la reconnaissance vocale pour convertir la voix en texte.
     */
    private String runSpeechToText() {
        try {
            long startTime = System.currentTimeMillis();
            speechToText recognizer = new speechToText(apiKeyPicoVoice);
            String result = recognizer.run("src/caches/request.wav");
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

    /**
     * Interagit avec l'API OpenAI pour obtenir une réponse à la requête de l'utilisateur.
     */
    private String interactWithOpenAI(String currentUser, String userRequest) {
        System.out.println("Waiting for inference...");
        OpenAI api = new OpenAI(this.apiKeyOpenAi);
        api.setCurrentUser(currentUser);

        String context = Memory.getLongMemory();
        String initialRequest = currentUser.concat(" te demande : ").concat(userRequest);
        String apiResponse = api.sendRequest(initialRequest, context);

        // Rafraîchir la mémoire avec la nouvelle conversation
        Memory.refreshFlashMemory("Question précédentes : ".concat(userRequest).concat("\nTu avais répondu : ").concat(api.cleanResponse(apiResponse).split("@")[0]));

        // --------   Vérifier s'il y a une action spéciale à effectuer
        String finalResponse = api.specialFunction(apiResponse, apiKeyWeather, userRequest);
       
        //met à jour la mémoire avec la réponse finale
       if (finalResponse != null) {
            finalResponse = Memory.addData(api.cleanText(finalResponse));
        }
        return finalResponse;
    }
}