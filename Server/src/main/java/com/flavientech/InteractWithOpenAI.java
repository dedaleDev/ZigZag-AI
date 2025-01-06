package com.flavientech;

import org.json.JSONException;
import org.json.JSONObject;

import com.flavientech.service.DatabaseController;

public class InteractWithOpenAI {

    /**------------------------------------------------------interactWithOpenAI------------------------------------------------------
     * Interagit avec l'API OpenAI pour obtenir une réponse à la requête de l'utilisateur.
     -----------------------------------------------------------------------------------------------------------------------------------------*/
    public static String run(String apiKeyOpenAi, String currentUser, String userRequest) {
        System.out.println("Waiting for inference...");
        OpenAI api = new OpenAI(apiKeyOpenAi);
        api.setCurrentUser(currentUser);

        DatabaseController db = new DatabaseController();
        String context = db.getLongMemory(currentUser).toString();
        String initialRequest = currentUser.concat(" te demande : ").concat(userRequest);
        String apiResponse = api.sendRequest(initialRequest, context);

        // Rafraîchir la mémoire avec la nouvelle conversation
        db.updateFlashMemory(userRequest, api.cleanResponse(apiResponse).split("@")[0]);

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
                finalResponse = db.updateLongMemoryAndCleanAnswer(api.cleanText(finalResponse));
                db.closeConnection();
                return finalResponse;
        } else {
            finalResponse = "Oups, j'ai glissé chef ! Mon cerveau a dérapé... Je reviens vers vous dans un instant, une fois les dégâts réparés.";
            db.closeConnection();
            return finalResponse;
        }
    }
}
