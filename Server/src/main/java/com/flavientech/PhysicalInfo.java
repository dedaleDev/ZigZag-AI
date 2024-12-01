package com.flavientech;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhysicalInfo {
    public String getDayAndTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Formater la date et l'heure
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);
        // Afficher la date et l'heure actuelles
        return "Date et heure actuelles : " + formattedDateTime;
    }
}
