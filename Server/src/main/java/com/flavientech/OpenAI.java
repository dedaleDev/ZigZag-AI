package com.flavientech;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.flavientech.controller.MemoryController;

public class OpenAI {
    private final String API_KEY;
    private String prompt;
    private String currentUser;


    @Autowired
    private MemoryController memoryController;

    private OkHttpClient createHttpClient() {//Créer un client HTTP avec un timeout de 30 secondes
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public OpenAI(String API_KEY) {
        this.API_KEY = API_KEY;
        this.currentUser = "inconnu";
        this.prompt = readFileAsString(PathChecker.checkPath("promptGPT4o-Mini.txt"));
    }

    public void setCurrentUser(String user) {
        this.currentUser = user;
    }

    public String sendRequest(String request, String context) {
        OkHttpClient client = createHttpClient();
        String jsonPayload = generateJsonPayload(request);
        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8")))
                .build();
        return executeRequest(client, req, jsonPayload);
    }

    private String generateJsonPayload(String request) {
        JSONObject json = new JSONObject();
        try {
            json.put("model", "gpt-4o-mini");
        
            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", cleanText(prompt));
        
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", cleanText(getInfos() + " Requête utilisateur : " + request));
        
            messages.put(systemMessage);
            messages.put(userMessage);
        
            json.put("messages", messages);
            json.put("temperature", 0.7);
        
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println(json.toString());
        }
        return null;
        
    }

    private String executeRequest(OkHttpClient client, Request req, String jsonPayload) {
        try (Response response = client.newCall(req).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response + " Payload: " + jsonPayload);
            }
            String responseBody = response.body().string();
            System.out.println(responseBody);
            return responseBody;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readFileAsString(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String cleanResponse(String APIResult) {
        if (APIResult == null) {
            System.err.println("APIResult is null");
            return null;
        }
        try {
            JSONObject json = new JSONObject(APIResult);
            String content = json.getJSONArray("choices")
                                 .getJSONObject(0)
                                 .getJSONObject("message")
                                 .getString("content");

            return content == null ? "Oups, une erreur s'est produite. Veuillez réessayer.": cleanText(content.split("&")[0]);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getInfos() {
        return String.format("Utilisateur actuel : %s | Infos : %s | Rappel : %s | Conversations précédentes : %s",currentUser, Time.getDateTime(), memoryController.getLongMemory(this.currentUser), memoryController.getFlashMemory());
    }

    public String specialFunction(String response, String apiKeyWeather, String question) {
        try {
            JSONObject json = new JSONObject(response);
            String content = json.getJSONArray("choices")
                                 .getJSONObject(0)
                                 .getJSONObject("message")
                                 .getString("content");
            response = content;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (response == null) return null;
        if (response.contains("&Weather")){
            WeatherService weather = new WeatherService(response, apiKeyWeather);
            if (response.contains("&WeatherToday,")) {
                return weather.forecast(weather.getCurrentWeather(), API_KEY, question);
            } else if (response.contains("&WeatherDate,")) {
                return weather.forecast(weather.getWeatherDate(), API_KEY, question);
            } else if (response.contains("&Weather5Day")) {
                return weather.forecast(weather.getWeatherForNextFiveDays(), API_KEY, question);
            }
        } else if (response.contains("&OnlineSearch,")) {
            System.out.println("Action spécifique : Recherche en ligne");
            return OnlineSearch.search(response.split("&OnlineSearch,")[1].split("&")[0], API_KEY, question);
        }
        else {
            System.out.println("Aucune action spécifique n'a été trouvée.");
            System.out.println("Réponse : " + response);
        }
        return response;
    }

    public String sendCustomRequest(String request) {
        OkHttpClient client = new OkHttpClient();
        String jsonPayload = generateJsonPayload(request.replaceAll("\n", " "));
        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8")))
                .build();
        System.out.println("Sending custom request: " + request);
        return executeRequest(client, req, jsonPayload);
    }

    public String cleanText(String text) {
        return text.replaceAll("[*_`\"\\\\#]", "").trim();
    }
}