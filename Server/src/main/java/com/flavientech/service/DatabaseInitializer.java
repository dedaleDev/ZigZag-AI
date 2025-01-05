package com.flavientech.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.flavientech.LoadConf;
import com.flavientech.PathChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseInitializer {

    // Configuration de la base de données
    private static final String URL = LoadConf.getDatabaseSource();
    private static final String USER = LoadConf.getDatabaseUsername();
    private static final String PASSWORD = LoadConf.getDatabasePassword();
    private static final String DB_NAME = URL.substring(URL.lastIndexOf("/") + 1);
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

    private static void createDatabaseAndLoadSQL() throws IOException {
        // Étape 1 : Création de la base de données
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
            System.out.println("Base de données " + DB_NAME + " créée.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de la base de données.");
        }

        // Étape 2 : Charger le fichier SQL via JDBC
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            String sql = new String(Files.readAllBytes(Paths.get(SQL_FILE)));
            for (String query : sql.split(";")) {
                if (!query.trim().isEmpty()) {
                    stmt.executeUpdate(query);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du chargement du fichier SQL.");
        }
    }


    private static boolean verifyDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Vérifier l'existence des tables
            String[] tables = {"flashmemory", "longmemory", "user"};
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + table + "';");
                if (!rs.next()) {
                    //affiche en ROUGE que la table n'existe pas
                    System.out.println("\033[31m" + "La table " + table + " n'existe pas." + "\033[0m");
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}