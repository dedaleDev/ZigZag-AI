package com.flavientech;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.flavientech.controller.MemoryController;

public class InteractWithOpenAI {

    @Autowired
    private static MemoryController memoryController;
    
        /**------------------------------------------------------interactWithOpenAI------------------------------------------------------
         * Interagit avec l'API OpenAI pour obtenir une réponse à la requête de l'utilisateur.
         -----------------------------------------------------------------------------------------------------------------------------------------*/
        public static String run(String apiKeyOpenAi, String currentUser, String userRequest) {
            System.out.println("Waiting for inference...");
            OpenAI api = new OpenAI(apiKeyOpenAi);
            api.setCurrentUser(currentUser);
    
            String context = memoryController.getLongMemory(currentUser).toString();
        System.out.println("Context : " + context);
        String initialRequest = currentUser.concat(" te demande : ").concat(userRequest);
        String apiResponse = api.sendRequest(initialRequest, context);

        // Rafraîchir la mémoire avec la nouvelle conversation
        memoryController.refreshFlashMemory("Question précédentes : ".concat(userRequest).concat("\nTu avais répondu : ").concat(api.cleanResponse(apiResponse).split("@")[0]));

        // --------   Vérifier s'il y a une action spéciale à effectuer
        //String finalResponse = api.specialFunction(apiResponse, apiKeyWeather, userRequest); //à compléter si le temps
        try {
            JSONObject json = new JSONObject(apiResponse);
            String content = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
            apiResponse = content;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String finalResponse = apiResponse;
    
        //met à jour la mémoire avec la réponse finale
        if (finalResponse != null) {
                finalResponse = memoryController.refreshLongMemoryAndCleanAnswer(api.cleanText(finalResponse));
                return finalResponse;
        } else {
            finalResponse = "Oups, j'ai glissé chef ! Mon cerveau a dérapé... Je reviens vers vous dans un instant, une fois les dégâts réparés.";
            return finalResponse;
        }
    }
    
}
