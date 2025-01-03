package com.flavientech;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone; 

public class Time {
    public static String getTime() {
        // Get the current time
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        String time = sdf.format(date);
        return time;
    }

    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        String dateStr = sdf.format(date);
        return dateStr;
    }

    public static String getDateTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        String dateTime = sdf.format(date);
        return dateTime;
    }

    public static long convertDateToUnixTimestamp(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");  // Format de la date d'entrée
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Utiliser UTC pour correspondre à l'heure Unix
        try {
            Date parsedDate = dateFormat.parse(date);
            return parsedDate.getTime() / 1000; // Conversion en secondes
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }


    public static boolean isSameDay(long timestamp1, long timestamp2) {
        // Diviser par 86400 (secondes par jour) pour obtenir les jours complets
        long day1 = timestamp1 / 86400;
        long day2 = timestamp2 / 86400;
        return day1 == day2;
    }

    public static String convertUnixToHour(long unixTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(unixTimestamp * 1000); // Convertir en millisecondes
        return sdf.format(date);
    }

    public static String convertUnixToDate(long unixTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(unixTimestamp * 1000); // Convertir en millisecondes
        return sdf.format(date);
    }
}
