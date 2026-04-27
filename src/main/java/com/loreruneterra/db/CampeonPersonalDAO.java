package com.loreruneterra.db;

import com.loreruneterra.model.Campeon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampeonPersonalDAO {

    // ══════════════════════════════════════════
    //  READ
    // ══════════════════════════════════════════

    public List<Campeon> getAll() {
        List<Campeon> lista = new ArrayList<>();
        String sql = """
                SELECT key, nombre, titulo, clase, imagen_icono, imagen_splash,
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

    public Campeon getByKey(String key) {
        String sql = """
                SELECT key, nombre, titulo, clase, imagen_icono, imagen_splash,
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

    public List<Campeon> searchByNombre(String texto) {
        List<Campeon> lista = new ArrayList<>();
        String sql = """
                SELECT key, nombre, titulo, clase, imagen_icono, imagen_splash,
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

    public boolean create(Campeon campeon) {
        if (campeon.getKey() == null || campeon.getKey().isEmpty()) {
            String keyGenerada = "custom_"
                    + campeon.getNombre().toLowerCase().replaceAll("\\s+", "_")
                    + "_" + System.currentTimeMillis();
            campeon.setKey(keyGenerada);
        }

        String sql = """
                INSERT INTO campeones_personalizados
                    (key, nombre, titulo, clase, imagen_icono, imagen_splash,
                     bio_corta, bio_completa, bio_primera)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, campeon.getKey());
                ps.setString(2, campeon.getNombre());
                ps.setString(3, campeon.getTitulo());
                ps.setString(4, nullIfEmpty(campeon.getClase()));
                ps.setString(5, nullIfEmpty(campeon.getImagenIcono()));
                ps.setString(6, nullIfEmpty(campeon.getImagenSplash()));
                ps.setString(7, nullIfEmpty(campeon.getBioCorta()));
                ps.setString(8, nullIfEmpty(campeon.getBioCompleta()));
                ps.setString(9, nullIfEmpty(campeon.getBioPrimera()));
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

    public boolean update(Campeon campeon) {
        String sql = """
                UPDATE campeones_personalizados
                SET nombre        = ?,
                    titulo        = ?,
                    clase         = ?,
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
                ps.setString(3, nullIfEmpty(campeon.getClase()));
                ps.setString(4, nullIfEmpty(campeon.getImagenIcono()));
                ps.setString(5, nullIfEmpty(campeon.getImagenSplash()));
                ps.setString(6, nullIfEmpty(campeon.getBioCorta()));
                ps.setString(7, nullIfEmpty(campeon.getBioCompleta()));
                ps.setString(8, nullIfEmpty(campeon.getBioPrimera()));
                ps.setString(9, campeon.getKey());

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

    private Campeon mapRow(ResultSet rs) throws SQLException {
        Campeon c = new Campeon(
                rs.getString("key"),
                rs.getString("nombre"),
                rs.getString("titulo"),
                ""
        );
        c.setClase       (getString(rs, "clase"));
        c.setImagenIcono (getString(rs, "imagen_icono"));
        c.setImagenSplash(getString(rs, "imagen_splash"));
        c.setBioCorta    (getString(rs, "bio_corta"));
        c.setBioCompleta (getString(rs, "bio_completa"));
        c.setBioPrimera  (getString(rs, "bio_primera"));
        return c;
    }

    private String getString(ResultSet rs, String col) throws SQLException {
        String val = rs.getString(col);
        return val != null ? val : "";
    }

    private String nullIfEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}