package com.flavientech;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseConnectionChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Connexion à la base de données réussie !");
        } catch (Exception ex) {
            System.err.println("Erreur : Impossible de se connecter à la base de données. Vérifiez la configuration.");
            System.err.println("Détails : " + ex.getMessage());
        }
    }
}