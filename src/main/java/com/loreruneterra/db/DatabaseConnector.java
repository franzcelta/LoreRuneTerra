package com.loreruneterra.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:postgresql://localhost:5432/loreruneterra";
    private static final String USER = "postgres";      // usuario habitual
    private static final String PASSWORD = "0031";  // contraña habitual

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Método de prueba rápido
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("¡Conexión a PostgreSQL exitosa! DB: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Error al conectar a PostgreSQL:");
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Código de error: " + e.getErrorCode());
            // e.printStackTrace();  // Comentado temporalmente - warning de IntelliJ
        }
    }

    public static void main(String[] args) {
        testConnection();
    }
}
