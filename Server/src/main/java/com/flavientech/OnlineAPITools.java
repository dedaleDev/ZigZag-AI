package com.flavientech;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Random;

public class OnlineAPITools {
    
    


    // Liste des User-Agents pour rotation
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1"
    };


    // Obtenir un User-Agent aléatoire
    public static String getRandomUserAgent() {
        Random rand = new Random();
        return USER_AGENTS[rand.nextInt(USER_AGENTS.length)];
    }

    // Pause aléatoire pour éviter les détections
    public static void randomPause(int minMillis, int maxMillis) {
        Random rand = new Random();
        int pauseTime = rand.nextInt(maxMillis - minMillis + 1) + minMillis;
        try {
            Thread.sleep(pauseTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    // Méthode pour récupérer le contenu HTML d'une page avec gestion des erreurs
    public static String fetchHTML(String urlString, int retryCount) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                urlString = urlString.replaceAll(" ", "%20");
                URI uri = new URI(urlString);
                URL url = uri.toURL();

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("User-Agent", getRandomUserAgent());
                connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.8,en-US;q=0.5,en;q=0.3");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("DNT", "1");
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                connection.setRequestProperty("Sec-Fetch-Dest", "document");
                connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
                connection.setRequestProperty("Sec-Fetch-Site", "none");
                connection.setRequestProperty("Sec-Fetch-User", "?1");

                int responseCode = connection.getResponseCode();

                // Suivre manuellement les redirections
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String newUrl = connection.getHeaderField("Location");
                    System.out.println("Redirection vers : " + newUrl);
                    return fetchHTML(newUrl, retryCount);  // Récursion pour suivre la redirection
                }

                // Gérer les erreurs 403 et autres codes d'erreur
                if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    System.out.println("Erreur 403 : Accès interdit à " + urlString);
                    attempts++;
                    exponentialBackoff(attempts);
                    continue;  // Réessayer avec backoff
                } else if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("Erreur dans la requête : " + responseCode + " " + connection.getResponseMessage());
                    return null;
                }

                // Lire le contenu HTML si la réponse est OK
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                exponentialBackoff(attempts);  // Backoff en cas d'échec
            }

            attempts++;
        }
        System.out.println("Échec après " + retryCount + " tentatives. Abandon.");
        return null;
    }

    // Méthode pour récupérer un JSONObject à partir d'une URL
    public static JSONObject fetchUrl(String urlString, int retryCount) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                URI uri = new URI(urlString);
                URL url = uri.toURL();

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("User-Agent", getRandomUserAgent());
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

    /**
     * Extract text from HTML content.
     * @param html HTML content
     * @return Extracted text
     */
    public static String extractTextFromHtml(String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("h1, h2, h3, p, a");
        StringBuilder extractedText = new StringBuilder();
        for (Element element : elements) {
            extractedText.append(element.text()).append("\n");
        }
        return extractedText.toString().trim().replaceAll("\n", "");
    }
}