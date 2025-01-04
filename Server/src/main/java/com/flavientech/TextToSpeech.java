package com.flavientech;

public class TextToSpeech {
    private String textVoice;
    private static final String VOICE_DIR = PathChecker.getCachesDir() +"eagleVoices.txt";
    private static final String currentVoice = LoadConf.getVoice();
    private PythonController pythonController;

    public TextToSpeech(String textVoice) {
        this.textVoice = textVoice;
        this.pythonController = new PythonController(PathChecker.checkPath("edgeTTS.py"));
        this.runEdgeTTS();
    }

    public void runEdgeTTS() {
        try {
            // Échapper correctement le texte pour qu'il soit entouré de guillemets
            String escapedTextVoice = this.textVoice.replace("\"", "\\\"").replace("'", "\\'");
            boolean success = pythonController.runPythonScript(escapedTextVoice, currentVoice);
            if (success) {
                System.out.println("Le script Python s'est exécuté avec succès.");
            } else {
                System.err.println("Le script Python a échoué.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}