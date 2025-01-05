package com.flavientech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebServer {
    public static void main(String[] args) {
        try{
            SpringApplication.run(WebServer.class, args);
        }catch(Exception e){
            //afficher l'erreur en rouge :
            System.err.println("Erreur : " + e);
            System.err.println("Indice de correction : veuillez vérifier que l'url, le mot de passe et le nom d'utilisateur pour la base de donnée indiqué dans le fichier application.properties est correcte. Et que le serveur de base de donnée est bien démarré.");
        }
   
    }
    
}