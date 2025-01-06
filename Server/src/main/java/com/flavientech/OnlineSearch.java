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
        StringBuilder result = new StringBuilder();
    
        try {
            // Naviguer dans l'arborescence JSON jusqu'au tableau des items
            JSONObject resultObj = root.optJSONObject("result");
            System.out.println("Result : " + resultObj);
            if (resultObj == null) {
                return "Erreur : Clé 'result' absente dans le JSON.";
            }

            JSONObject items = resultObj.optJSONObject("items");
            if (items == null) {
                return "Erreur : Clé 'items' absente dans 'result'.";
            }

            JSONArray mainline = items.optJSONArray("mainline");
            if (mainline == null) {
                return "Erreur : Clé 'mainline' absente ou non valide dans 'items'.";
            }

            for (int i = 0; i < mainline.length(); i++) {
                JSONObject mainlineObject = mainline.optJSONObject(i);
                if (mainlineObject == null) {
                    continue; // Passer si l'objet est null
                }

                // Vérifier que le type est "web"
                if ("web".equals(mainlineObject.optString("type"))) {
                    JSONArray itemsArray = mainlineObject.optJSONArray("items");
                    if (itemsArray == null) {
                        continue; // Passer si 'items' est absent ou non valide
                    }

                    // Parcourir les éléments du tableau "items"
                    for (int j = 0; j < itemsArray.length(); j++) {
                        JSONObject item = itemsArray.optJSONObject(j);
                        if (item == null) {
                            continue; // Passer si l'élément est null
                        }

                        // Extraire le titre et la description
                        String title = item.optString("title", "N/A");
                        String desc = item.optString("desc", "N/A");

                        // Ajouter au résultat au format attendu
                        result.append("titre : \"").append(title).append("\" / description : \"").append(desc).append("\"\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors du traitement du JSON : " + e.getMessage();
        }
    
        return result.toString().trim(); // Supprimer les espaces ou sauts de ligne en trop
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
