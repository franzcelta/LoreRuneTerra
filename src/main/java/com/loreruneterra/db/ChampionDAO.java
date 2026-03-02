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
        return getBiografiaCampo(keyCampeon, "biografia_corta");
    }

    // Carga la biografía COMPLETA más reciente
    public String getBiografiaCompleta(String keyCampeon) {
        return getBiografiaCampo(keyCampeon, "biografia_completa");
    }

    // Carga la biografía en PRIMERA PERSONA más reciente (solo para algunos)
    public String getBiografiaPrimeraPersona(String keyCampeon) {
        return getBiografiaCampo(keyCampeon, "biografia_primera_persona");
    }

    // Método privado para cargar cualquier campo
    private String getBiografiaCampo(String keyCampeon, String campo) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = String.format("""
                SELECT %s
                FROM biografias b
                JOIN campeones c ON b.campeon_id = c.id
                WHERE c.key = ?
                ORDER BY ultima_actualizacion DESC
                LIMIT 1
                """, campo);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, keyCampeon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(campo);
            }
        } catch (SQLException e) {
            System.err.println("Error cargando " + campo + ": " + e.getMessage());
        }
        return null;
    }

    // Guarda o actualiza las 3 biografías
    public void saveBiografia(String keyCampeon, String textoCorto, String textoCompleto, String textoPrimeraPersona) {
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
                INSERT INTO biografias (campeon_id, biografia_corta, biografia_completa, biografia_primera_persona, ultima_actualizacion)
                VALUES (?, ?, ?, ?, CURRENT_DATE)
                ON CONFLICT (campeon_id) DO UPDATE SET
                    biografia_corta = EXCLUDED.biografia_corta,
                    biografia_completa = EXCLUDED.biografia_completa,
                    biografia_primera_persona = EXCLUDED.biografia_primera_persona,
                    ultima_actualizacion = CURRENT_DATE
                """);
            ps.setInt(1, campeonId);
            ps.setString(2, textoCorto);
            ps.setString(3, textoCompleto);
            ps.setString(4, textoPrimeraPersona);
            ps.executeUpdate();

            System.out.println("Biografías guardadas para " + keyCampeon);
        } catch (SQLException e) {
            System.err.println("Error guardando biografías: " + e.getMessage());
        }
    }
}