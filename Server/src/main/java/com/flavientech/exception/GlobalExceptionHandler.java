package com.flavientech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.PersistenceException;
import org.hibernate.exception.JDBCConnectionException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JDBCConnectionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDatabaseConnectionException(JDBCConnectionException ex, Model model) {
        // Log the exception if necessary
        System.err.println("Erreur de connexion à la base de données : " + ex.getMessage());

        // Ajoutez un message personnalisé pour l'utilisateur
        model.addAttribute("errorMessage", "Impossible de se connecter à la base de données. Vérifiez la configuration.");
        return "error"; // Nom de la vue HTML d'erreur
    }

    @ExceptionHandler(PersistenceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handlePersistenceException(PersistenceException ex, Model model) {
        // Log the exception
        System.err.println("Erreur de persistance : " + ex.getMessage());

        // Ajoutez un message personnalisé pour l'utilisateur
        model.addAttribute("errorMessage", "Une erreur est survenue lors de l'accès à la base de données. Veuillez vérifier la configuration.");
        return "error"; // Nom de la vue HTML d'erreur
    }
}