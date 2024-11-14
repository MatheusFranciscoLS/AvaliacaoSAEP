package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // URL do banco de dados PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/tododb"; // Altere para seu banco
    private static final String USER = "postgres"; // Altere para seu usuário
    private static final String PASSWORD = "postgres"; // Altere para sua senha

    // Método para obter a conexão com o banco
    public static Connection getConnection() throws SQLException {
        try {
            // Registra o driver do PostgreSQL (se necessário)
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado", e);
        }
    }
}