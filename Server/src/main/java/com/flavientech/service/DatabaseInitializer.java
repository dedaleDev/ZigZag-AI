package com.flavientech.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.flavientech.LoadConf;
import com.flavientech.PathChecker;

import java.io.IOException;

public class DatabaseInitializer {

    // Configuration de la base de données
    private static final String URL = LoadConf.getDatabaseSource();
    private static final String USER = LoadConf.getDatabaseUsername();
    private static final String PASSWORD = LoadConf.getDatabasePassword();
    private static final String DB_NAME = LoadConf.getDatabaseName();
    private static final String SQL_FILE = PathChecker.checkPath("zigzag.sql");

    public static boolean initialize() {
        Connection connection = null;
        try {
            // Connexion au serveur MySQL sans spécifier de base de données
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie avec MySQL.");

            // Vérifie si la base de données zigzag existe
            if (!databaseExists(connection, DB_NAME)) {
                System.out.println("La base de données " + SQL_FILE + " n'existe pas. Création en cours...");
                createDatabaseAndLoadSQL();
                System.out.println("Base de données " + DB_NAME + " créée et chargée avec succès.");

                // Vérification de la base de données
                if (!verifyDatabase()) {
                    throw new RuntimeException("La vérification de la base de données a échoué.");
                }
                System.out.println("Vérification de la base de données réussie.");
            } else {
                if (!verifyDatabase()) {
                    throw new RuntimeException("La vérification de la base de données a échoué.");
                }
                System.out.println("La base de données " + DB_NAME + " existe déjà.");
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // Vérifie si une base de données existe
    private static boolean databaseExists(Connection connection, String dbName) {
        boolean exists = false;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + dbName + "';")) {
            exists = rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    // Crée la base de données et charge le fichier SQL
    private static void createDatabaseAndLoadSQL() throws IOException, InterruptedException {
        // Étape 1 : Création de la base de données
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
            System.out.println("Base de données " + DB_NAME + " créée.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la base de données.");
        }

        // Étape 2 : Charger le fichier SQL avec la commande imposée
        ProcessBuilder processBuilder = new ProcessBuilder(
                "mysql",
                "-u", USER,
                "-p" + PASSWORD,
                DB_NAME,
                "-e", "source " + SQL_FILE 
        );

        // Redirige les flux d'entrée et sortie pour afficher les messages d'erreur si nécessaire
        processBuilder.redirectErrorStream(true);

        // Exécute la commande
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Fichier SQL chargé avec succès dans la base de données " + DB_NAME + ".");
        } else {
            throw new RuntimeException("Erreur lors du chargement du fichier SQL. Code de sortie : " + exitCode);
        }
    }

    private static boolean verifyDatabase() {
        try (Connection conn = DriverManager.getConnection(URL + "/" + DB_NAME, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Vérifier l'existence des tables
            String[] tables = {"flashmemory", "longmemory", "user"};
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + table + "';");
                if (!rs.next()) {
                    System.out.println("La table " + table + " n'existe pas.");
                    return false;
                }
            }

            // Vérifier le nombre de lignes dans chaque table (exemple)
            String[][] tableChecks = {
                {"flashmemory", "SELECT COUNT(*) FROM flashmemory"},
                {"longmemory", "SELECT COUNT(*) FROM longmemory"},
                {"user", "SELECT COUNT(*) FROM user"}
            };

            for (String[] check : tableChecks) {
                ResultSet rs = stmt.executeQuery(check[1]);
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        System.out.println("La table " + check[0] + " est vide.");
                        return false;
                    }
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}