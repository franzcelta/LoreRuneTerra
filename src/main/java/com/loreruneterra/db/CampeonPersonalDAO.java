package com.loreruneterra.db;

import com.loreruneterra.model.Campeon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'campeones_personalizados'.
 * Gestiona los campeones creados por el usuario (no importados de DataDragon).
 * Usa 'key' como identificador único (no hay id autoincremental).
 * Todas las operaciones de escritura usan transacciones explícitas.
 */
public class CampeonPersonalDAO {

    // ══════════════════════════════════════════
    //  READ
    // ══════════════════════════════════════════

    /**
     * Devuelve todos los campeones personalizados ordenados por nombre.
     */
    public List<Campeon> getAll() {
        List<Campeon> lista = new ArrayList<>();
        String sql = """
                SELECT key, nombre, titulo, imagen_icono, imagen_splash,
                       bio_corta, bio_completa, bio_primera
                FROM campeones_personalizados
                ORDER BY nombre ASC
                """;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error cargando campeones personalizados: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Busca un campeón personalizado por su key única.
     * Devuelve null si no existe.
     */
    public Campeon getByKey(String key) {
        String sql = """
                SELECT key, nombre, titulo, imagen_icono, imagen_splash,
                       bio_corta, bio_completa, bio_primera
                FROM campeones_personalizados
                WHERE key = ?
                """;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error buscando campeón personal '" + key + "': " + e.getMessage());
        }
        return null;
    }

    /**
     * Búsqueda por nombre (insensible a mayúsculas).
     */
    public List<Campeon> searchByNombre(String texto) {
        List<Campeon> lista = new ArrayList<>();
        String sql = """
                SELECT key, nombre, titulo, imagen_icono, imagen_splash,
                       bio_corta, bio_completa, bio_primera
                FROM campeones_personalizados
                WHERE nombre ILIKE ?
                ORDER BY nombre ASC
                """;
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en búsqueda personal '" + texto + "': " + e.getMessage());
        }
        return lista;
    }

    /**
     * Comprueba si ya existe un campeón personalizado con esa key.
     */
    public boolean existsByKey(String key) {
        String sql = "SELECT 1 FROM campeones_personalizados WHERE key = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error comprobando existencia de key '" + key + "': " + e.getMessage());
        }
        return false;
    }

    // ══════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════

    /**
     * Inserta un nuevo campeón personalizado en la BD.
     * Genera una key única basada en el nombre + timestamp si está vacía.
     *
     * @param campeon Objeto Campeon con los datos a insertar
     * @return true si la inserción fue exitosa, false en caso de error
     */
    public boolean create(Campeon campeon) {
        // Generar key si no tiene
        if (campeon.getKey() == null || campeon.getKey().isEmpty()) {
            String keyGenerada = "custom_"
                    + campeon.getNombre().toLowerCase().replaceAll("\\s+", "_")
                    + "_" + System.currentTimeMillis();
            campeon.setKey(keyGenerada);
        }

        String sql = """
                INSERT INTO campeones_personalizados
                    (key, nombre, titulo, imagen_icono, imagen_splash,
                     bio_corta, bio_completa, bio_primera)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, campeon.getKey());
                ps.setString(2, campeon.getNombre());
                ps.setString(3, campeon.getTitulo());
                ps.setString(4, nullIfEmpty(campeon.getImagenIcono()));
                ps.setString(5, nullIfEmpty(campeon.getImagenSplash()));
                ps.setString(6, nullIfEmpty(campeon.getBioCorta()));
                ps.setString(7, nullIfEmpty(campeon.getBioCompleta()));
                ps.setString(8, nullIfEmpty(campeon.getBioPrimera()));
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("Campeón personal creado: " + campeon.getNombre());
            return true;

        } catch (SQLException e) {
            System.err.println("Error creando campeón personal '" + campeon.getNombre() + "': " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════

    /**
     * Actualiza todos los campos de un campeón personalizado existente.
     * Identifica el registro por su key (clave única de la tabla).
     *
     * @param campeon Objeto con los nuevos valores (la key no cambia)
     * @return true si se actualizó correctamente, false en caso de error
     */
    public boolean update(Campeon campeon) {
        String sql = """
                UPDATE campeones_personalizados
                SET nombre        = ?,
                    titulo        = ?,
                    imagen_icono  = ?,
                    imagen_splash = ?,
                    bio_corta     = ?,
                    bio_completa  = ?,
                    bio_primera   = ?
                WHERE key = ?
                """;
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, campeon.getNombre());
                ps.setString(2, campeon.getTitulo());
                ps.setString(3, nullIfEmpty(campeon.getImagenIcono()));
                ps.setString(4, nullIfEmpty(campeon.getImagenSplash()));
                ps.setString(5, nullIfEmpty(campeon.getBioCorta()));
                ps.setString(6, nullIfEmpty(campeon.getBioCompleta()));
                ps.setString(7, nullIfEmpty(campeon.getBioPrimera()));
                ps.setString(8, campeon.getKey());

                int filas = ps.executeUpdate();
                conn.commit();

                if (filas > 0) {
                    System.out.println("Campeón personal actualizado: " + campeon.getNombre());
                    return true;
                } else {
                    System.err.println("No se encontró campeón personal con key='" + campeon.getKey() + "'");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error actualizando campeón personal '" + campeon.getKey() + "': " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════

    /**
     * Elimina un campeón personalizado por su key única.
     * No hay CASCADE aquí porque esta tabla no tiene tablas dependientes.
     *
     * @param key Identificador único del campeón a eliminar
     * @return true si se eliminó correctamente, false en caso de error
     */
    public boolean delete(String key) {
        String sql = "DELETE FROM campeones_personalizados WHERE key = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                int filas = ps.executeUpdate();
                conn.commit();

                if (filas > 0) {
                    System.out.println("Campeón personal eliminado (key='" + key + "')");
                    return true;
                } else {
                    System.err.println("No se encontró campeón personal con key='" + key + "'");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error eliminando campeón personal key='" + key + "': " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignorar */ }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
            }
        }
    }

    // ══════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ══════════════════════════════════════════

    /** Convierte una fila del ResultSet en un objeto Campeon. */
    private Campeon mapRow(ResultSet rs) throws SQLException {
        Campeon c = new Campeon(
                rs.getString("key"),
                rs.getString("nombre"),
                rs.getString("titulo"),
                ""   // imagen principal vacía — esta tabla usa icono y splash
        );
        c.setImagenIcono (getString(rs, "imagen_icono"));
        c.setImagenSplash(getString(rs, "imagen_splash"));
        c.setBioCorta    (getString(rs, "bio_corta"));
        c.setBioCompleta (getString(rs, "bio_completa"));
        c.setBioPrimera  (getString(rs, "bio_primera"));
        return c;
    }

    /** Lee un String del ResultSet y devuelve "" si es null. */
    private String getString(ResultSet rs, String col) throws SQLException {
        String val = rs.getString(col);
        return val != null ? val : "";
    }

    /** Devuelve null si el String está vacío — para no guardar "" en BD. */
    private String nullIfEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}