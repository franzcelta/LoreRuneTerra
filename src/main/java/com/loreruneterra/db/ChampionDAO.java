package com.loreruneterra.db;

import com.loreruneterra.model.Campeon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChampionDAO {

    // Carga TODOS los campeones
    public List<Campeon> getAllCampeones() {
        List<Campeon> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT key, nombre, titulo, imagen FROM campeones ORDER BY nombre ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Campeon c = new Campeon(
                        rs.getString("key"),
                        rs.getString("nombre"),
                        rs.getString("titulo"),
                        rs.getString("imagen")
                );
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar campeones: " + e.getMessage());
        }

        return lista;
    }

    // Carga la biografía CORTA más reciente
    public String getBiografiaCorta(String keyCampeon) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = """
                SELECT biografia_corta
                FROM biografias b
                JOIN campeones c ON b.campeon_id = c.id
                WHERE c.key = ?
                ORDER BY ultima_actualizacion DESC
                LIMIT 1
                """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, keyCampeon);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("biografia_corta");
            }
        } catch (SQLException e) {
            System.err.println("Error cargando biografía corta: " + e.getMessage());
        }
        return null;
    }

    // Carga la biografía COMPLETA más reciente
    public String getBiografiaCompleta(String keyCampeon) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = """
                SELECT biografia_completa
                FROM biografias b
                JOIN campeones c ON b.campeon_id = c.id
                WHERE c.key = ?
                ORDER BY ultima_actualizacion DESC
                LIMIT 1
                """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, keyCampeon);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("biografia_completa");
            }
        } catch (SQLException e) {
            System.err.println("Error cargando biografía completa: " + e.getMessage());
        }
        return null;
    }

    // Guarda o actualiza ambas biografías (corta y completa)
    public void saveBiografia(String keyCampeon, String textoCorto, String textoCompleto) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement psId = conn.prepareStatement("SELECT id FROM campeones WHERE key = ?");
            psId.setString(1, keyCampeon);
            ResultSet rs = psId.executeQuery();

            if (!rs.next()) {
                System.err.println("Campeón no encontrado: " + keyCampeon);
                return;
            }

            int campeonId = rs.getInt("id");

            PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO biografias (campeon_id, biografia_corta, biografia_completa, ultima_actualizacion)
                VALUES (?, ?, ?, CURRENT_DATE)
                ON CONFLICT (campeon_id) DO UPDATE SET
                    biografia_corta = EXCLUDED.biografia_corta,
                    biografia_completa = EXCLUDED.biografia_completa,
                    ultima_actualizacion = CURRENT_DATE
                """);
            ps.setInt(1, campeonId);
            ps.setString(2, textoCorto);
            ps.setString(3, textoCompleto);
            ps.executeUpdate();

            System.out.println("Biografías guardadas para " + keyCampeon);
        } catch (SQLException e) {
            System.err.println("Error guardando biografías: " + e.getMessage());
        }
    }

    // Versión compatible con el código antiguo (usamos la misma como corta por ahora)
    public void saveBiografia(String keyCampeon, String textoCompleto) {
        saveBiografia(keyCampeon, textoCompleto, textoCompleto);
    }
}