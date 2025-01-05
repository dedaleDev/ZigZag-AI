package com.flavientech.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.flavientech.LoadConf;
import com.flavientech.PathChecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatabaseInitializer {
    private static final String URL = LoadConf.getDatabaseSource();
    private static final String USER = LoadConf.getDatabaseUsername();
    private static final String PASSWORD = LoadConf.getDatabasePassword();
    private static final String DB_NAME = LoadConf.getDatabaseDb();
    private static final String SQL_FILE = PathChecker.checkPath("zigzag.sql");

    public static boolean initialize() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
             
            String checkDbExists = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + DB_NAME + "'";
            var rs = stmt.executeQuery(checkDbExists);
            if (!rs.next()) {
                String createDb = "CREATE DATABASE IF NOT EXISTS" + DB_NAME;
                stmt.executeUpdate(createDb);
                executeSqlFile(conn, SQL_FILE);
            }
        } catch (SQLException | IOException e) {
            if (e instanceof SQLException) {
                System.err.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
            } else if (e instanceof IOException) {
                System.err.println("\u001B[31mFile error: " + e.getMessage() + "\u001B[0m");
            } else {
                System.err.println("\u001B[31mUnexpected error: " + e.getMessage() + "\u001B[0m");
            }
            return false;
        }
        return true;
    }

    private static void executeSqlFile(Connection conn, String filePath) throws IOException, SQLException {
        try (Statement stmt = conn.createStatement();
             BufferedReader br = new BufferedReader(new FileReader(filePath))) {
             
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                if (line.trim().endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0);
                }
            }
        }
    }
}