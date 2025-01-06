package com.flavientech;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class OnlineAPITools {
    
    // Backoff exponentiel
    public static void exponentialBackoff(int attempt) {
        try {
            long waitTime = (long) Math.pow(2, attempt) * 1000;  // Exponential backoff
            System.out.println("Backoff : Attente de " + waitTime + " ms avant de réessayer...");
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer un JSONObject à partir d'une URL
    public static JSONObject fetchUrl(String urlString, int retryCount) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                URI uri = new URI(urlString);
                URL url = uri.toURL();

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
                connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.5,en;q=0.3");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Connection", "keep-alive");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return new JSONObject(response.toString());

                } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    System.out.println("Erreur 403 : Accès interdit à " + urlString);
                    attempts++;
                    exponentialBackoff(attempts);
                    continue;  // Réessayer après un backoff
                } else {
                    System.out.println("Erreur dans la requête : " + responseCode + " " + connection.getResponseMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                exponentialBackoff(attempts);
            }

            attempts++;
        }
        System.out.println("Échec après " + retryCount + " tentatives. Abandon.");
        return null;
    }



    public static JSONObject fetchUrl(String urlString) {
        try {
            // Créer l'URI et ensuite l'URL
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    
            // Configurer la méthode de requête
            connection.setRequestMethod("GET");
    
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.5,en;q=0.3");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Connection", "keep-alive");
    
            // Vérifier le code de réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lire la réponse
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
    
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return new JSONObject(response.toString());
            } else {
                System.out.println("Erreur dans la requête : " + responseCode + " " + connection.getResponseMessage());
            }
        } catch (URISyntaxException e) {
            System.out.println("Erreur d'URI : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray fetchUrlArray(String urlString) {
        try {
            // Créer l'URI et ensuite l'URL
            URI uri = new URI(urlString);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.5,en;q=0.3");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Connection", "keep-alive");
            // Configurer la méthode de requête
            connection.setRequestMethod("GET");

            // Vérifier le code de réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lire la réponse
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Convertir la réponse en JSONArray
                return new JSONArray(response.toString());
            } else {
                System.out.println("Erreur dans la requête : " + responseCode);
            }
        } catch (URISyntaxException e) {
            System.out.println("Erreur d'URI : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}