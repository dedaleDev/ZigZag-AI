package com.flavientech.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.flavientech.LoadConf;

public class DatabaseController {

    private static final String DATABASE_URL = LoadConf.getDatabaseSource();
    private static final String DATABASE_USER = LoadConf.getDatabaseUsername();
    private static final String DATABASE_PASSWORD =  LoadConf.getDatabasePassword();

    private Connection connection;

    /**
     * Constructeur - initialise la connexion à la base de données.
     */
    public DatabaseController() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la connexion à la base de données.");
        }
    }

    /**
     * Méthode pour update une entrée dans la table `flashmemory`.
     *
     * @param request La requête à enregistrer.
     * @param answer  La réponse associée.
     */
    public void updateFlashMemory(String request, String answer) {
        String lengthSql = "SELECT SUM(LENGTH(Request) + LENGTH(Answer)) AS totalLength FROM flashmemory";
        try (PreparedStatement lengthStmt = connection.prepareStatement(lengthSql);
             ResultSet rs = lengthStmt.executeQuery()) {
            if (rs.next()) {
                int totalLength = rs.getInt("totalLength");
                if (totalLength + request.length() + answer.length() > 5000) {
                    String deleteSql = "DELETE FROM flashmemory";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                        deleteStmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Erreur lors du nettoyage de flashmemory.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération de la longueur totale de flashmemory.");
        }

        String sql = "INSERT INTO flashmemory (Request, Answer) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, request);
            pstmt.setString(2, answer);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion dans flashmemory.");
        }
    }
    /**
     * Méthode pour update une entrée dans la table `longmemory`.
     *
     * @param username Le nom de l'utilisateur associé.
     * @param summary  Le résumé à enregistrer.
     */
    public void updateLongMemory(String username, String summary) {
        String countSql = "SELECT COUNT(*) AS total FROM longmemory";
        try (PreparedStatement countStmt = connection.prepareStatement(countSql);
             ResultSet rs = countStmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("total");
                if (count >= 40) {
                    String deleteSql = "DELETE FROM longmemory";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                        deleteStmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Erreur lors du nettoyage de longmemory.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération de longmemory.");
        }

        String sql = "INSERT INTO longmemory (User, Date, Summary) SELECT user.Id, ?, ? FROM user WHERE user.Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setString(2, summary);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion dans longmemory.");
        }
    }

    /**
     * Méthode pour insérer un utilisateur dans la table `user`.
     *
     * @param username Le nom d'utilisateur à enregistrer.
     */
    public void newUser(String username) {
        String sql = "INSERT INTO user (Username) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'insertion dans user.");
        }
    }

    /**
    * Méthode pour supprimer un utilisateur de la base de données.
    *
    * @param username Le nom d'utilisateur à supprimer.
    */

    public void deleteUser(String username) {
        String sql = "DELETE FROM user WHERE Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur.");
        }
    }

    /**
     * Méthode pour rechercher les entrées de `longmemory` en fonction du nom d'utilisateur.
     *
     * @param username Le nom d'utilisateur.
     * @return Une liste des résumés trouvés.
     */
    public List<String> searchLongMemoryByUsername(String username) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT Summary FROM longmemory JOIN user ON longmemory.User = user.Id WHERE user.Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("Summary"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche dans longmemory.");
        }
        return results;
    }

    /**
     * Méthode pour fermer la connexion à la base de données.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la fermeture de la connexion à la base de données.");
        }
    }


    /*
     * Méthode pour récupérer les entrées de `flashmemory`.
     * @return Une liste des résumés trouvés. au format "Request : Answer"
     */
    public String getFlashMemory() {
        List<String> results = new ArrayList<>();
        String sql = "SELECT * FROM flashmemory";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                results.add(rs.getString("Request") + " : " + rs.getString("Answer"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération de flashmemory.");
        }
        //formate la réponse au format : "Question précédentes : " + Request + "\nTu avais répondu : " + Answer
        StringBuilder sb = new StringBuilder();
        for (String result : results) {
            sb.append("Question précédentes : ").append(result.split(" : ")[0]).append("\nTu avais répondu : ").append(result.split(" : ")[1]).append("\n");
        }
        return sb.toString();
    }


    /*
     * Méthode pour récupérer les entrées de `longmemory`.
     * @String username Le nom d'utilisateur, ne renvoie que les résumés associés à cet utilisateur.
     * @return Une liste des résumés trouvés. au format "User : Date : Summary"
     */
    public String getLongMemory(String username) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT * FROM longmemory JOIN user ON longmemory.User = user.Id WHERE user.Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString(rs.getString("Date") + " : " + rs.getString("Summary")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("\033[31mErreur lors de la récupération de longmemory.\033[0m");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String result : results) {
            sb.append(result).append("\n");
        }
        return sb.toString();
    }


    public String updateLongMemoryAndCleanAnswer(String fullData) {
        int startIndex = fullData.indexOf('@'); //first @
        int endIndex = fullData.indexOf('@', startIndex + 1) ; //second @

        if (startIndex != -1 && endIndex != -1) {  
            this.updateLongMemory(fullData.substring(startIndex + 1, endIndex).split(",")[1], fullData.substring(startIndex + 1, endIndex).split(",")[0]);
            fullData = fullData.substring(0, startIndex) + fullData.substring(endIndex + 1);//remove the summary
        }
        return fullData;
    }

}