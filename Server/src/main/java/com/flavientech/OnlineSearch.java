package com.flavientech;

import org.json.JSONArray;
import org.json.JSONObject;

public class OnlineSearch {
    private static final String QWANT_API_URL = "https://api.qwant.com/v3/search/web";
    
    private static String QwantSearch(String query){
        try {
            query = query.replace(" ", "%20");
            System.out.println(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr");
            JSONObject resultAPI = OnlineAPITools.fetchUrl(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr",2);
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

    private static String decodeJson(JSONObject root) {
        System.out.println(root);
        StringBuilder result = new StringBuilder();

        try {
            // Récupérer les éléments principaux de l'objet JSON
            JSONObject resultObject = root.getJSONObject("result");
            JSONObject itemsObject = resultObject.getJSONObject("items");
            JSONArray mainlineArray = itemsObject.getJSONArray("mainline");

            // Parcourir les éléments "mainline"
            for (int i = 0; i < mainlineArray.length(); i++) {
                JSONObject mainlineItem = mainlineArray.getJSONObject(i);
                if (mainlineItem.has("items")) {
                    JSONArray itemArray = mainlineItem.getJSONArray("items");

                    // Parcourir les objets individuels dans "items"
                    for (int j = 0; j < itemArray.length(); j++) {
                        JSONObject item = itemArray.getJSONObject(j);
                        String title = item.optString("title", "Titre non disponible");
                        String description = item.optString("desc", "Description non disponible");

                        // Ajouter le titre et la description au résultat
                        result.append("Titre: ").append(title).append("\n");
                        result.append("Description: ").append(description).append("\n");
                        result.append("----\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors du traitement du JSON.";
        }

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
