package com.flavientech;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherService {
    String apiKey;
    String city;
    double[] coordinates;
    String date;

    /**
     * Build the WeatherService object
     * @param response The response from the user
     * @param apiKey The API key for the OpenWeatherMap API
     */
    public WeatherService(String response, String apiKey) {
        this.apiKey = apiKey;
        this.city = response.split("&")[1].split(",")[1];
        try {
            this.date = response.split("&")[1].split(",")[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.date = Time.getDate();
        }
        this.coordinates = this.convertCityToCoordinates(city.strip());
    }

    /**
     * Convert the city to coordinates
     * @param city The city to convert in coordinates
     * @return The coordinates of the city in an array [latitude, longitude]
     */
    public double[] convertCityToCoordinates(String city) {
        OnlineAPITools api = new OnlineAPITools();
        JSONArray result  = api.fetchUrlArray("https://api.openweathermap.org/geo/1.0/direct?q="+city+"&limit=1&appid="+this.apiKey);
        double[] coordinates = new double[2];
        try {
            JSONObject location = result.getJSONObject(0);
            coordinates[0] = location.getDouble("lat");
            coordinates[1] = location.getDouble("lon");
            System.out.println("Latitude: " + coordinates[0] + ", Longitude: " + coordinates[1]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return coordinates;
    }
    

    /**
     * Get the weather for the date
     * @return The weather for the date in a string
     */
    public String getWeatherDate() {
        String url = buildWeatherUrl("hourly,minutely,alerts");
        JSONObject result = OnlineAPITools.fetchUrl(url);
        long targetTimestamp = Time.convertDateToUnixTimestamp(this.date);  // Timestamp jour cible (à 00:00 UTC)
        
        // Récupérer les prévisions horaires
        JSONArray hourlyWeather;
        try {
            hourlyWeather = result.getJSONArray("hourly");
            if (hourlyWeather == null) return "Erreur lors de la récupération des données météo (section hourly)";
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la récupération des données météo (section hourly) : " + e.getMessage() + url);
            return "Erreur lors de la récupération des données météo (section hourly) : " + e.getMessage() + url;
        }
        StringBuilder weatherResult = new StringBuilder("La météo pour le " + date + " est :\n");
        boolean dataFound = false; 
        // Parcourir les prévisions horaires
        for (int i = 0; i < hourlyWeather.length(); i++) {
            try {
                JSONObject hourWeather = hourlyWeather.getJSONObject(i);
                long weatherTimestamp = hourWeather.getLong("dt");  // Timestamp de la prévision horaire
    
                // Comparer le timestamp de l'heure avec la date cible (en nombre de jours)
                if (Time.isSameDay(targetTimestamp, weatherTimestamp)) {
                    dataFound = true; 
                    appendWeatherDetails(weatherResult, hourWeather, weatherTimestamp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "Erreur lors de la récupération des données météo (détails horaires)";
            }
        }
        if (!dataFound) {
            return "Aucune donnée météo disponible pour cette date.";
        }
        return weatherResult.toString();
    }    
    
    /**
     * Get the weather for the next five days
     * @return The weather for the next five days in a string
     */
    public String getWeatherForNextFiveDays() {
        String url = buildWeatherUrl("hourly,minutely,alerts");
        JSONObject result = OnlineAPITools.fetchUrl(url);
    
        // Récupérer les prévisions journalières
        JSONArray dailyWeather;
        try {
            dailyWeather = result.getJSONArray("daily");
            if (dailyWeather == null) return "Erreur lors de la récupération des données météo (section daily)";
        } catch (JSONException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des données météo (section daily)";
        }
    
        StringBuilder weatherResult = new StringBuilder("Prévisions météo pour les 5 prochains jours :\n");
        int daysToShow = Math.min(dailyWeather.length(), 5);
    
        // Parcourir les 5 jours de prévisions
        for (int i = 0; i < daysToShow; i++) {
            try {
                JSONObject dayWeather = dailyWeather.getJSONObject(i);
                appendDayForecast(weatherResult, dayWeather);
            } catch (JSONException e) {
                e.printStackTrace();
                return "Erreur lors de la récupération des données météo (détails journaliers)";
            }
        }
        return weatherResult.toString();
    }

    public String getCurrentWeather() {
        String url = buildWeatherUrl("hourly,daily,minutely,alerts");
        JSONObject result = OnlineAPITools.fetchUrl(url);
    
        // Récupérer les données actuelles
        JSONObject currentWeather;
        try {
            currentWeather = result.getJSONObject("current");
        } catch (JSONException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des données météo actuelles.";
        }
        StringBuilder weatherResult = new StringBuilder("Météo actuelle :\n");
        try {
            appendCurrentWeather(weatherResult, currentWeather);
        } catch (JSONException e) {
            e.printStackTrace();
            return "Erreur lors de la récupération des détails météorologiques actuels.";
        }
        return weatherResult.toString();
    }

    public String clean(String response){
        return response.split("&Weather,")[0];
    }


    /**
     * Append the forecast for the day
     * @param weather The weather String
     * @param apiKeyOpenAI 
     * @param question The question asked by the user
     */
    public String forecast(String weather, String apiKeyOpenAI, String question) {
        if (weather == null) return "Oups, une erreur est survenue lors de la récupération des données météo. Veuillez retenter plus tard.";
        OpenAI openAI = new OpenAI(apiKeyOpenAI);
        String prompt = "Ceci est la suite d'une requête spéciale que tu as déclenchée précédemment pour répondre à la question : %s. Voici la météo, analyse-la, tire-en les informations principales. Écris un bref bulletin météo pour la ville de %s. Données méteo :%s".formatted(question, city, weather);
        System.out.println(prompt);
        String forecast = openAI.sendCustomRequest(prompt);
        return openAI.cleanResponse(forecast).split("@")[0];
    }

    /**
     * Get the weather for the date
     * @param exclude The data to exclude from the API call
     * @return The weather for the date in a string
     */
    private String buildWeatherUrl(String exclude) {
        return String.format(
                "https://api.openweathermap.org/data/3.0/onecall?lat=%f&lon=%f&exclude=%s&appid=%s&units=metric&lang=fr",
                this.coordinates[0], this.coordinates[1], exclude, this.apiKey);
    }


    /**
     * Get the weather for the date
     * @return The weather for the date in a string
     * @throws JSONException If an error occurs while parsing the JSON
     */
    private void appendWeatherDetails(StringBuilder weatherResult, JSONObject hourWeather, long weatherTimestamp) throws JSONException {
        String description = hourWeather.getJSONArray("weather").getJSONObject(0).getString("description");
        double temp = hourWeather.getDouble("temp");
        double feelsLike = hourWeather.getDouble("feels_like");
        int pressure = hourWeather.getInt("pressure");
        int humidity = hourWeather.getInt("humidity");
        double dewPoint = hourWeather.getDouble("dew_point");
        double uvi = hourWeather.getDouble("uvi");
        int clouds = hourWeather.getInt("clouds");
        int visibility = hourWeather.getInt("visibility");
        double windSpeed = hourWeather.getDouble("wind_speed");
        int windDeg = hourWeather.getInt("wind_deg");

        double rain = hourWeather.has("rain") ? hourWeather.getJSONObject("rain").optDouble("1h", 0.0) : 0.0;
        double snow = hourWeather.has("snow") ? hourWeather.getJSONObject("snow").optDouble("1h", 0.0) : 0.0;

        String hour = Time.convertUnixToHour(weatherTimestamp);

        weatherResult.append(String.format("- %s : %s\n", hour, description))
                .append(String.format("  Température: %.1f°C, Ressenti: %.1f°C\n", temp, feelsLike))
                .append(String.format("  Pression: %d hPa, Humidité: %d%%, Point de rosée: %.1f°C\n", pressure, humidity, dewPoint))
                .append(String.format("  UV: %.1f, Nuages: %d%%, Visibilité: %d mètres\n", uvi, clouds, visibility))
                .append(String.format("  Vent: %.1f m/s, Direction: %d°\n", windSpeed, windDeg));

        if (rain > 0) weatherResult.append(String.format("  Pluie: %.1f mm/h\n", rain));
        if (snow > 0) weatherResult.append(String.format("  Neige: %.1f mm/h\n", snow));
    }

    private void appendDayForecast(StringBuilder weatherResult, JSONObject dayWeather) throws JSONException {
        long weatherTimestamp = dayWeather.getLong("dt");
        String date = Time.convertUnixToDate(weatherTimestamp);

        String description = dayWeather.getJSONArray("weather").getJSONObject(0).getString("description");
        double tempDay = dayWeather.getJSONObject("temp").getDouble("day");
        double tempNight = dayWeather.getJSONObject("temp").getDouble("night");
        double feelsLikeDay = dayWeather.getJSONObject("feels_like").getDouble("day");
        double feelsLikeNight = dayWeather.getJSONObject("feels_like").getDouble("night");
        int pressure = dayWeather.getInt("pressure");
        int humidity = dayWeather.getInt("humidity");
        double dewPoint = dayWeather.getDouble("dew_point");
        double uvi = dayWeather.getDouble("uvi");
        int clouds = dayWeather.getInt("clouds");
        int visibility = dayWeather.has("visibility") ? dayWeather.getInt("visibility") : -1;
        double windSpeed = dayWeather.getDouble("wind_speed");
        int windDeg = dayWeather.getInt("wind_deg");

        double rain = dayWeather.has("rain") ? dayWeather.optDouble("rain", 0.0) : 0.0;
        double snow = dayWeather.has("snow") ? dayWeather.optDouble("snow", 0.0) : 0.0;

        weatherResult.append(String.format("Prévisions pour le %s :\n", date))
                .append(String.format("  Description : %s\n", description))
                .append(String.format("  Température jour : %.1f°C, nuit : %.1f°C\n", tempDay, tempNight))
                .append(String.format("  Ressenti jour : %.1f°C, nuit : %.1f°C\n", feelsLikeDay, feelsLikeNight))
                .append(String.format("  Pression : %d hPa, Humidité : %d%%, Point de rosée : %.1f°C\n", pressure, humidity, dewPoint))
                .append(String.format("  UV : %.1f, Nuages : %d%%\n", uvi, clouds));

        if (visibility >= 0) weatherResult.append(String.format("  Visibilité : %d mètres\n", visibility));
        weatherResult.append(String.format("  Vent : %.1f m/s, Direction : %d°\n", windSpeed, windDeg));

        if (rain > 0) weatherResult.append(String.format("  Pluie : %.1f mm\n", rain));
        if (snow > 0) weatherResult.append(String.format("  Neige : %.1f mm\n", snow));

        weatherResult.append("\n");
    }
    
    private void appendCurrentWeather(StringBuilder weatherResult, JSONObject currentWeather) throws JSONException {
        long weatherTimestamp = currentWeather.getLong("dt");

        String description = currentWeather.getJSONArray("weather").getJSONObject(0).getString("description");
        double temp = currentWeather.getDouble("temp");
        double feelsLike = currentWeather.getDouble("feels_like");
        int pressure = currentWeather.getInt("pressure");
        int humidity = currentWeather.getInt("humidity");
        double dewPoint = currentWeather.getDouble("dew_point");
        double uvi = currentWeather.getDouble("uvi");
        int clouds = currentWeather.getInt("clouds");
        int visibility = currentWeather.getInt("visibility");
        double windSpeed = currentWeather.getDouble("wind_speed");
        int windDeg = currentWeather.getInt("wind_deg");

        double rain = currentWeather.has("rain") ? currentWeather.getJSONObject("rain").optDouble("1h", 0.0) : 0.0;
        double snow = currentWeather.has("snow") ? currentWeather.getJSONObject("snow").optDouble("1h", 0.0) : 0.0;

        String time = Time.convertUnixToHour(weatherTimestamp);

        weatherResult.append(String.format("Observation à %s :\n", time))
                .append(String.format("  Description : %s\n", description))
                .append(String.format("  Température : %.1f°C, Ressenti : %.1f°C\n", temp, feelsLike))
                .append(String.format("  Pression : %d hPa, Humidité : %d%%, Point de rosée : %.1f°C\n", pressure, humidity, dewPoint))
                .append(String.format("  UV : %.1f, Nuages : %d%%, Visibilité : %d mètres\n", uvi, clouds, visibility))
                .append(String.format("  Vent : %.1f m/s, Direction : %d°\n", windSpeed, windDeg));

        if (rain > 0) weatherResult.append(String.format("  Pluie : %.1f mm/h\n", rain));
        if (snow > 0) weatherResult.append(String.format("  Neige : %.1f mm/h\n", snow));
    }
}