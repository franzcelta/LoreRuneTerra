package com.loreruneterra.db;

import com.loreruneterra.model.Lugar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlacesDAO {

    // Cargar todos los lugares
    public List<Lugar> getAllLugares() {
        List<Lugar> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT id, nombre, descripcion, imagen_url FROM lugares ORDER BY nombre ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Lugar lugar = new Lugar(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("imagen_url")
                );
                lista.add(lugar);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar lugares: " + e.getMessage());
        }
        return lista;
    }

    // Añadir un lugar nuevo (Para edición posterior)
    public void addLugar(String nombre, String descripcion, String imagenUrl) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "INSERT INTO lugares (nombre, descripcion, imagen_url) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setString(3, imagenUrl);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error añadiendo lugar: " + e.getMessage());
        }
    }

    // ... Aquí mas tarde una vez avanzado el proyecto meteremos mas metodos para añadir o eliminar cosas.
}