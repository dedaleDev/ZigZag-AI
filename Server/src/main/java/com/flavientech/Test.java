package com.flavientech;

public class Test {
    public static void main(String[] args) {
        System.out.println("Run tests...");
        // Arguments pour le script Python
        String accessKey = "NA0NuP+5Orn3NUuj8UHB6Sj1VaolSuM2qvYlQeeWbYs6epGzsACtYA==";
        //ArrayList<String> users = EagleController.getUsersVoiceList();
        //String audioPath = "src/caches/request.wav";  // Nécessaire pour l'action "test"
        EagleController.runEnroll(accessKey, "Flavien");
        /*
        // Créer une instance de PythonProcessRunner et exécuter le script Python
        long startTime = System.currentTimeMillis(); // Début de la mesure du temps
        if (users != null && !users.isEmpty()) {
            List<Thread> threads = new ArrayList<>();
            // Créer un thread pour chaque utilisateur
            for (String user : users) {
                Thread thread = new Thread(() -> {
                    float score = EagleController.runTest(accessKey, user, audioPath);
                    System.out.println("Utilisateur: " + user + ", Score: " + score);
                });
                threads.add(thread);
                thread.start();
            }
            // Attendre que tous les threads se terminent
            for (Thread thread : threads) {
                try {
                    thread.join(); // Attend que le thread se termine
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Aucun utilisateur trouvé.");
        }
        long endTime = System.currentTimeMillis(); // Fin de la mesure du temps
        System.out.println("Temps d'exécution : " + (endTime - startTime) + " ms = " + (endTime - startTime) / 1000 + "s");
    */
        }
}