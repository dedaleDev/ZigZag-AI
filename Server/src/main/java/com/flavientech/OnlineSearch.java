package com.flavientech;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OnlineSearch {
    private static final String QWANT_API_URL = "https://api.qwant.com/v3/search/web";
    
    private static String[] QwantSearch(String query, int nbResults){
        try {
            query = query.replace(" ", "%20");
            System.out.println(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr");
            JSONObject resultAPI = OnlineAPITools.fetchUrl(QWANT_API_URL + "?q=" + query + "&count=10&offset=0&locale=fr_fr",2);
            String urls[] = new String[nbResults];
            for (int i =0; i<nbResults; i++){
                urls[i] = decodeJson(resultAPI,i);
            }
            return urls;
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

    private static String decodeJson(JSONObject json, int id){
        try{
            JSONObject jsonObj = null;
            JSONArray jsonArray = json.getJSONObject("data")
            .getJSONObject("result")
            .getJSONObject("items")
            .getJSONArray("mainline");
            for (int i = 0; i < jsonArray.length(); i++){
                if (jsonArray.getJSONObject(i).getInt("serpContextId") ==1002){
                    jsonObj = jsonArray.getJSONObject(i);
                    break;
                }
            }
            if (jsonObj == null){
                System.out.println("Pas de résultats trouvés");
                return null;
            }
            jsonObj= jsonObj
            .getJSONArray("items")
            .getJSONObject(0);
            String url = jsonObj.getString("url");
            System.out.println("URL : " + url);
            return url;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String search(String Keyword, String apiKeyOpenAI, String question){
        String url[] = QwantSearch(Keyword,5); 
        if (url == null) {
            return "Une erreur est survenue lorsque je surfais sur le web ! ";
        }
        System.out.println("URLs : " + url[0] + "\n" + url[1]);
        String html = "";
        int minFetch = 2;
        int maxFetch = 5;
        for (String u : url) {
            if (minFetch == 0){
                break;
            }
            if (maxFetch == 0){
                break;
            }
            String temp = OnlineAPITools.fetchHTML(u,1);
            if (temp == null){
                maxFetch--;
                continue;
            }
            minFetch--;
            html = html.concat(temp);
            System.out.println("HTML Text : " + OnlineAPITools.extractTextFromHtml(html));
        }
        if (html == null) {
            return "Une erreur est survenue lorsque je surfais sur le web ! ";
        }
        OpenAI openAI = new OpenAI(apiKeyOpenAI);
        String prompt = "Ceci est la suite d'une requête spéciale (OnlineSearch) que tu as déclenchée précédemment pour répondre à la question : %s. Voici les informations extraites du web, analyse-les pour répondre à la question.%s".formatted(question,limitText(OnlineAPITools.extractTextFromHtml(html), 2000));
        String result = openAI.sendCustomRequest(prompt);
        return openAI.cleanResponse(result).split("@")[0];   
    }
}
