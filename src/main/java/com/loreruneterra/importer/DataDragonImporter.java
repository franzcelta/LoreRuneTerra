package com.loreruneterra.importer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Importa datos de campeones desde la API pública DataDragon de Riot Games.
 * Endpoint: https://ddragon.leagueoflegends.com/cdn/{version}/data/es_ES/champion.json
 *
 * Usa Gson para parsear el JSON.
 * Detecta duplicados mediante ChampionDAO.existsByKey() antes de insertar.
 * Reporta progreso mediante callbacks (Consumer) para actualizar la UI en tiempo real.
 */
public class DataDragonImporter {

    private static final String BASE_URL  = "https://ddragon.leagueoflegends.com";
    private static final String VERSIONS  = BASE_URL + "/api/versions.json";

    private final ChampionDAO       championDAO;
    private final Consumer<String>  logger;       // Callback para log de texto
    private final Consumer<Double>  progressCb;   // Callback para ProgressBar (0.0-1.0)

    public DataDragonImporter(ChampionDAO championDAO,
                              Consumer<String> logger,
                              Consumer<Double> progressCb) {
        this.championDAO = championDAO;
        this.logger      = logger;
        this.progressCb  = progressCb;
    }

    /**
     * Ejecuta la importación completa.
     * 1. Obtiene la versión más reciente de DataDragon
     * 2. Descarga el JSON de campeones en español
     * 3. Por cada campeón: si no existe (por key), lo inserta en la BD
     *
     * @return número de campeones nuevos insertados
     */
    public int importar() throws Exception {
        logger.accept("Conectando con DataDragon API...");

        // ── Paso 1: obtener versión más reciente ──
        String version = obtenerUltimaVersion();
        logger.accept("Versión DataDragon: " + version);

        // ── Paso 2: descargar JSON de campeones ──
        String championUrl = BASE_URL + "/cdn/" + version + "/data/es_ES/champion.json";
        logger.accept("Descargando: " + championUrl);
        String json = descargarJSON(championUrl);
        logger.accept("JSON descargado correctamente.");

        // ── Paso 3: parsear con Gson ──
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject data = root.getAsJsonObject("data");

        int total  = data.size();
        int nuevos = 0;
        int omitidos = 0;
        int i = 0;

        logger.accept("Procesando " + total + " campeones...\n");

        for (String champKey : data.keySet()) {
            JsonObject champ = data.getAsJsonObject(champKey);

            String key    = champ.get("id").getAsString();
            String nombre = champ.get("name").getAsString();
            String titulo = champ.get("title").getAsString();

            // URL de imagen (icono del catálogo)
            String imagen = BASE_URL + "/cdn/" + version
                    + "/img/champion/" + key + ".png";

            // Clases (tags) — tomamos la primera
            String clase = "";
            if (champ.has("tags") && champ.get("tags").getAsJsonArray().size() > 0) {
                clase = champ.get("tags").getAsJsonArray().get(0).getAsString();
                // Mapear inglés → español
                clase = mapearClase(clase);
            }

            // ── Si existe, actualizar clase e imagen. Si no, insertar ──
            Campeon existente = championDAO.getCampeonByKey(key);
            if (existente != null) {
                existente.setClase(clase);
                existente.setImagen(imagen);
                championDAO.updateCampeon(existente);
                omitidos++;
            } else {
                Campeon campeon = new Campeon(key, nombre, titulo, imagen);
                campeon.setClase(clase);
                boolean ok = championDAO.createCampeon(campeon);
                if (ok) {
                    nuevos++;
                    logger.accept("  ✓ " + nombre);
                } else {
                    logger.accept("  ✗ Error insertando: " + nombre);
                }
            }

            // Actualizar progreso
            i++;
            progressCb.accept((double) i / total);
        }

        logger.accept("\n──────────────────────────────");
        logger.accept("Importación completada.");
        logger.accept("  Nuevos:   " + nuevos);
        logger.accept("  Omitidos: " + omitidos + " (ya existían)");
        logger.accept("  Total:    " + total);

        return nuevos;
    }

    // ── Obtiene la versión más reciente de DataDragon ──
    private String obtenerUltimaVersion() throws Exception {
        String json = descargarJSON(VERSIONS);
        // El JSON es un array: ["14.8.1","14.7.1",...]
        // Tomamos el primer elemento
        String primera = json.trim()
                .replace("[", "")
                .replace("]", "")
                .split(",")[0]
                .replace("\"", "")
                .trim();
        return primera;
    }

    // ── Descarga el contenido de una URL como String ──
    private String descargarJSON(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(30_000);
        conn.setRequestProperty("User-Agent", "LoreRuneTerra/1.0");

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Error HTTP " + code + " al acceder a: " + urlStr);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
        }
        conn.disconnect();
        return sb.toString();
    }

    // ── Mapea las clases de DataDragon (inglés) a español ──
    private String mapearClase(String tag) {
        return switch (tag) {
            case "Assassin"  -> "Asesino";
            case "Fighter"   -> "Luchador";
            case "Mage"      -> "Mago";
            case "Marksman"  -> "Marksman";
            case "Support"   -> "Soporte";
            case "Tank"      -> "Tanque";
            default          -> tag;
        };
    }
}