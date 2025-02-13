package com.flavientech;
import ai.picovoice.leopard.*;

public class SpeechToText { 
    private Leopard leopard;

    public SpeechToText(String apiKey) {
        final String accessKey = apiKey;
        try {
            leopard = new Leopard.Builder().setAccessKey(accessKey).setModelPath(PathChecker.checkPath("ZigZag-leopard-v2.0.0-24-09-13--08-11-50.pv")).build();
        } catch (LeopardException ex) {
            System.out.println("Error during creating audio to text: " + ex);
        }
    }

    public String run(String audioFilePath) {
        try {
            LeopardTranscript result = leopard.processFile(audioFilePath);
            leopard.delete();
            System.out.println(result);
            return result.getTranscriptString();
        } catch (LeopardException ex) {
            System.out.println("Error during creating speech to text: " + ex);
        }
        return null; // or any appropriate default value
    }
}