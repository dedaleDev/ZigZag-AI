package com.flavientech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@SpringBootApplication
public class WebServer {
    public static void main(String[] args) {
        SpringApplication.run(WebServer.class, args);
    }
}

// Écouteur pour capter les erreurs au démarrage
@Component
class DatabaseErrorHandler implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        Throwable exception = event.getException();
        while (exception != null) {
            if (exception instanceof SQLException) {
                // Message coloré pour l'erreur de la base de données
                System.out.println("\u001B[31mErreur : Mot de passe ou URL incorrect pour la base de données.\u001B[0m");
                break;
            }
            exception = exception.getCause();
        }
    }
}