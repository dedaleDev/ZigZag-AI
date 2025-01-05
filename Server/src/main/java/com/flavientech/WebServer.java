package com.flavientech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebServer {
    public static void main(String[] args) {
        try {
            SpringApplication.run(WebServer.class, args);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }

            String message = cause.getMessage();
            if (message.contains("Access denied for user")) {
                System.err.println("\u001B[31mLe mot de passe est incorrect.\u001B[0m");
            } else if (message.contains("Unable to determine Dialect")) {
                System.err.println("\u001B[31mURL de la base de données incorrecte ou dialecte non défini.\u001B[0m");
            } else if (message.contains("Unknown database")) {
                System.err.println("\u001B[31mLa base de données spécifiée n'existe pas.\u001B[0m");
            } else {
                System.err.println("\u001B[31mUne erreur est survenue : " + message + "\u001B[0m");
            }
        }
    }
}