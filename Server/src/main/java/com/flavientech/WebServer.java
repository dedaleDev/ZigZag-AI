package com.flavientech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.flavientech.exception.DatabaseConnectionException;

import jakarta.activation.DataSource;

@SpringBootApplication
public class WebServer {
    public static void main(String[] args) {
        SpringApplication.run(WebServer.class, args);
    }

    @Bean
    public DataSource dataSource() {
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://localhost:3306/mydb");
            dataSource.setUsername("user");
            dataSource.setPassword("password");
            return (DataSource) dataSource;
        } catch (Exception ex) {
            throw new DatabaseConnectionException("Erreur de connexion à la base de données : " + ex.getMessage());
        }
    }
}