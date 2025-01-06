package com.flavientech;

import java.util.regex.Pattern;

import org.json.JSONObject;

import java.util.regex.Matcher;

public class OnlineSearch {
    private static final String QWANT_API_URL = "https://api.qwant.com/v3/search/web";
    
    private static String QwantSearch(String query){
        try {
            query = query.replace(" ", "%20");
            System.out.println(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr");
            JSONObject resultAPI = OnlineAPITools.fetchUrl(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr",2);
            String descriptions = limitText(extractTitleAndDesc(resultAPI.toString()), 2000);
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

    public static String extractTitleAndDesc(String input) {
        // Pattern pour trouver les clés "title" et "desc" avec leurs valeurs associées
        Pattern pattern = Pattern.compile("\"(title|desc)\":\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();

        // On parcourt toutes les correspondances dans la chaîne
        while (matcher.find()) {
            String key = matcher.group(1); // Récupère "title" ou "desc"
            String value = matcher.group(2); // Récupère la valeur associée
            result.append(key).append(": ").append(value).append("\n");
        }

        return result.toString().trim(); // Retourne le résultat sans espace ou ligne vide à la fin
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
