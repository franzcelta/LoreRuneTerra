package com.loreruneterra.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestión de la conexión a PostgreSQL.
 * Patrón Singleton: una sola instancia de Connection durante el ciclo de vida
 * de la aplicación. Las credenciales se leen de config.properties (no hardcoded).
 */
public class DatabaseConnector {

    private static Connection instance;

    private static final Properties config = new Properties();

    static {
        try (InputStream in = DatabaseConnector.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new RuntimeException(
                        "No se encontró config.properties en el classpath. " +
                                "Copia config.properties a src/main/resources/");
            }
            config.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo config.properties: " + e.getMessage(), e);
        }
    }

    // Constructor privado — impide instanciar desde fuera
    private DatabaseConnector() {}

    /**
     * Devuelve la conexión activa. Si no existe o está cerrada, la crea.
     * Patrón Singleton: siempre la misma instancia.
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            String url = String.format("jdbc:postgresql://%s:%s/%s",
                    config.getProperty("db.host"),
                    config.getProperty("db.port"),
                    config.getProperty("db.name"));
            instance = DriverManager.getConnection(
                    url,
                    config.getProperty("db.user"),
                    config.getProperty("db.password"));
        }
        return instance;
    }

    /**
     * Cierra la conexión activa de forma explícita.
     * Llamar al cerrar la aplicación (en el método stop() de MainApp).
     */
    public static void closeConnection() {
        if (instance != null) {
            try {
                if (!instance.isClosed()) {
                    instance.close();
                    System.out.println("Conexión a PostgreSQL cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }

    /** Método de prueba rápida — útil durante desarrollo. */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Conexión a PostgreSQL exitosa. BD: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Error al conectar a PostgreSQL: " + e.getMessage());
            System.err.println("Verifica config.properties (host, puerto, usuario, contraseña).");
        }
    }
}