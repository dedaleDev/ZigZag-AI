package com.flavientech;

import org.json.JSONArray;
import org.json.JSONObject;

public class OnlineSearch {
    private static final String QWANT_API_URL = "https://api.qwant.com/v3/search/web";
    
    private static String QwantSearch(String query){
        try {
            query = query.replace(" ", "%20");
            System.out.println(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr");
            String resultAPI = OnlineAPITools.fetchUrl(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr",2).toString();
            String descriptions = limitText(decodeJson(resultAPI), 2000);
            return descriptions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String limitText(String text, int limit){
        if (text.length() > limit){
            return text.substring(0, limit);
        }
        return text;
    }

    private  static String decodeJson(String json) {
        StringBuilder result = new StringBuilder();

        try {
            // Parse le JSON
            JSONObject root = new JSONObject(json);

            // Vérifie le statut
            if (!root.optString("status").equalsIgnoreCase("success")) {
                return "Le statut n'est pas 'success'.";
            }

            // Accède aux items
            JSONObject resultObj = root.getJSONObject("result");
            JSONArray mainline = resultObj.getJSONArray("items").getJSONObject(0).getJSONArray("items");

            // Parcourt les items pour récupérer les titres et descriptions
            for (int i = 0; i < mainline.length(); i++) {
                JSONObject item = mainline.getJSONObject(i);
                String title = item.optString("title", "Titre non disponible");
                String desc = item.optString("desc", "Description non disponible");

                // Ajoute les informations au résultat final
                result.append("titre : \"").append(title).append("\" / description : \"").append(desc).append("\"\n");
            }

        } catch (Exception e) {
            return "Erreur lors du traitement du JSON : " + e.getMessage();
        }
        System.out.println("Result  OnlineSearch : " + result.toString());
        return result.toString();
    }

    public static String search(String Keyword, String apiKeyOpenAI, String question){
        String web = QwantSearch(Keyword); 
        if (web == null) {
            return "Une erreur est survenue lorsque je surfais sur le web ! ";
        }
        OpenAI openAI = new OpenAI(apiKeyOpenAI);
        String prompt = "Ceci est la suite d'une requête spéciale (OnlineSearch) que tu as déclenchée précédemment pour répondre à la question : %s. Voici les informations extraites du web, analyse-les pour répondre à la question.%s".formatted(question,web);
        System.out.println("Prompt Online Search: " + prompt);
        String result = openAI.sendCustomRequest(prompt);
        return openAI.cleanResponse(result).split("@")[0];   
    }
}
