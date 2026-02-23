package com.loreruneterra.importer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.loreruneterra.db.DatabaseConnector;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataDragonImporter {

    // ¡Cambia esta ruta a la tuya exacta!
    private static final String JSON_PATH = "C:\\Users\\franz\\Documents\\LoreRuneTerra ASSETS\\16.3.1\\data\\es_ES\\champion.json";
    // Si es championFull.json o está en otra subcarpeta, cámbialo aquí

    public static void main(String[] args) {
        importChampions();
    }

    public static void importChampions() {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(JSON_PATH);
             Connection conn = DatabaseConnector.getConnection()) {

            JsonObject root = gson.fromJson(reader, JsonObject.class);
            JsonObject data = root.getAsJsonObject("data");

            if (data == null) {
                System.err.println("No se encontró el objeto 'data' en el JSON.");
                return;
            }

            String sql = """
                INSERT INTO campeones (key, nombre, titulo, region, imagen)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (key) DO UPDATE SET
                    nombre = EXCLUDED.nombre,
                    titulo = EXCLUDED.titulo,
                    region = EXCLUDED.region,
                    imagen = EXCLUDED.imagen
                """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int count = 0;

                for (String key : data.keySet()) {
                    JsonObject champ = data.getAsJsonObject(key);

                    String nombre = champ.get("name").getAsString();
                    String titulo = champ.get("title").getAsString();

                    // Region: en champion.json no viene directamente, puedes dejar null o mapear manualmente
                    String region = null; // ← puedes mejorarlo después (ej. de tags o manual)

                    // Imagen: URL típica de DataDragon (ajusta versión si es necesario)
                    String imagen = "https://ddragon.leagueoflegends.com/cdn/15.3.1/img/champion/" + champ.get("image").getAsJsonObject().get("full").getAsString();

                    ps.setString(1, key);           // ej: "Aatrox"
                    ps.setString(2, nombre);        // ej: "Aatrox"
                    ps.setString(3, titulo);        // ej: "the Darkin Blade"
                    ps.setString(4, region);        // null por ahora
                    ps.setString(5, imagen);

                    ps.addBatch();
                    count++;
                }

                ps.executeBatch();
                System.out.println("¡Importación completada! Se importaron/actualizaron " + count + " campeones.");
            }

        } catch (IOException e) {
            System.err.println("Error al leer el JSON: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
        }
    }
}