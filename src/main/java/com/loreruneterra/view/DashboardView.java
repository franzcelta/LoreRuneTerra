package com.loreruneterra.view;

import com.loreruneterra.db.DatabaseConnector;
import com.loreruneterra.model.Campeon;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Vista del Dashboard de Estadísticas de LoreRuneTerra.
 * Implementada con CSS y JavaFX básico (sin javafx-charts).
 * Muestra:
 *  - KPIs: total campeones, regiones, biografías
 *  - Gráfico de barras por clase (CSS)
 *  - Gráfico circular por región (Arc JavaFX)
 *  - Tabla de actividad reciente
 */
public class DashboardView {

    // Colores por clase
    private static final Map<String, String> COLORES_CLASE = new LinkedHashMap<>();
    static {
        COLORES_CLASE.put("Asesino",   "#e74c3c");
        COLORES_CLASE.put("Luchador",  "#e67e22");
        COLORES_CLASE.put("Mago",      "#9b59b6");
        COLORES_CLASE.put("Marksman",  "#3498db");
        COLORES_CLASE.put("Soporte",   "#2ecc71");
        COLORES_CLASE.put("Tanque",    "#1abc9c");
        COLORES_CLASE.put("Otro",      "#7f8c8d");
    }

    // Colores por región
    private static final String[] COLORES_REGION = {
            "#c8aa6e","#4a7fa5","#2d6a4f","#8b1a1a","#6a4fa5",
            "#4f8b6a","#a5764f","#4f6a8b","#8b6a1a","#6a8b4f"
    };

    private final ObservableList<Campeon> campeonesList;

    // Registro de actividad reciente (máx 10 entradas)
    private static final List<String[]> actividadReciente = new ArrayList<>();

    public DashboardView(ObservableList<Campeon> campeonesList) {
        this.campeonesList = campeonesList;
    }

    /**
     * Registra una operación CRUD en el historial del dashboard.
     * Llamar desde MainController tras cada CREATE/UPDATE/DELETE.
     */
    public static void registrarActividad(String tipo, String descripcion) {
        String hora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
        actividadReciente.add(0, new String[]{tipo, descripcion, hora});
        if (actividadReciente.size() > 10) {
            actividadReciente.remove(actividadReciente.size() - 1);
        }
    }

    // ══════════════════════════════════════════
    //  CONSTRUCCIÓN DE LA VISTA
    // ══════════════════════════════════════════

    public ScrollPane buildView() {
        VBox page = new VBox(24);
        page.setPadding(new Insets(30));
        page.setStyle("-fx-background-color: #0d1624;");

        // ── Título ──
        Label titulo = new Label("📊  Dashboard de Estadísticas");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        // ── KPIs ──
        HBox kpis = buildKPIs();

        // ── Gráficos ──
        HBox graficos = new HBox(24);
        graficos.setAlignment(Pos.TOP_LEFT);
        VBox barChart  = buildBarChart();
        VBox pieChart  = buildPieChart();
        HBox.setHgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        graficos.getChildren().addAll(barChart, pieChart);

        // ── Actividad reciente ──
        VBox actividad = buildActividadReciente();

        page.getChildren().addAll(titulo, kpis, graficos, actividad);

        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #0d1624; -fx-background: #0d1624;");
        return scroll;
    }

    // ══════════════════════════════════════════
    //  KPIs
    // ══════════════════════════════════════════

    private HBox buildKPIs() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        int totalCampeones  = campeonesList.size();
        int totalRegiones   = contarRegiones();
        int totalBiografias = contarBiografias();
        String version      = "16.8.1"; // última versión DataDragon importada

        row.getChildren().addAll(
                kpiCard("⚔", String.valueOf(totalCampeones), "Campeones", "#1a3a5c"),
                kpiCard("🌍", String.valueOf(totalRegiones),  "Regiones",  "#1a4a2e"),
                kpiCard("📖", String.valueOf(totalBiografias),"Biografías","#3a1a5c"),
                kpiCard("🔄", version,                        "DataDragon","#4a3a1a")
        );
        return row;
    }

    private VBox kpiCard(String icono, String valor, String label, String bgColor) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(160);
        card.setPrefHeight(100);
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-background-radius: 12; -fx-padding: 16;");

        Label ico = new Label(icono + "  " + valor);
        ico.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #a09b8c;");

        card.getChildren().addAll(ico, lbl);
        return card;
    }

    // ══════════════════════════════════════════
    //  GRÁFICO DE BARRAS — Campeones por clase
    // ══════════════════════════════════════════

    private VBox buildBarChart() {
        VBox container = new VBox(12);
        container.setStyle("-fx-background-color: #1a2234; -fx-background-radius: 12; -fx-padding: 20;");

        Label titulo = new Label("Campeones por Clase");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        // Contar por clase
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (String clase : COLORES_CLASE.keySet()) conteo.put(clase, 0);

        for (Campeon c : campeonesList) {
            String clase = c.getClase();
            if (clase == null || clase.isEmpty()) clase = "Otro";
            conteo.merge(clase, 1, Integer::sum);
        }

        int maxVal = conteo.values().stream().mapToInt(Integer::intValue).max().orElse(1);

        VBox barras = new VBox(8);
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            if (entry.getValue() == 0) continue;
            String color = COLORES_CLASE.getOrDefault(entry.getKey(), "#7f8c8d");
            barras.getChildren().add(buildBarra(entry.getKey(), entry.getValue(), maxVal, color));
        }

        container.getChildren().addAll(titulo, barras);
        return container;
    }

    private HBox buildBarra(String label, int valor, int maxVal, String color) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // Etiqueta
        Label lbl = new Label(label);
        lbl.setMinWidth(80);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #a09b8c;");

        // Barra
        double porcentaje = (double) valor / maxVal;
        double maxAncho   = 200;
        Region barra = new Region();
        barra.setPrefHeight(18);
        barra.setPrefWidth(porcentaje * maxAncho);
        barra.setMinWidth(4);
        barra.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        // Valor
        Label val = new Label(String.valueOf(valor));
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");

        row.getChildren().addAll(lbl, barra, val);
        return row;
    }

    // ══════════════════════════════════════════
    //  GRÁFICO CIRCULAR — Campeones por región
    // ══════════════════════════════════════════

    private VBox buildPieChart() {
        VBox container = new VBox(12);
        container.setStyle("-fx-background-color: #1a2234; -fx-background-radius: 12; -fx-padding: 20;");

        Label titulo = new Label("Campeones por Región");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        // Contar por lugar_id
        Map<Integer, Integer> conteoPorId = new LinkedHashMap<>();
        Map<Integer, String>  nombrePorId = new LinkedHashMap<>();
        cargarNombresLugares(nombrePorId);

        for (Campeon c : campeonesList) {
            if (c.getLugarId() > 0) {
                conteoPorId.merge(c.getLugarId(), 1, Integer::sum);
            }
        }

        int total = conteoPorId.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            Label vacio = new Label("Sin datos de región asignados");
            vacio.setStyle("-fx-text-fill: #555e6e; -fx-font-size: 12px;");
            container.getChildren().addAll(titulo, vacio);
            return container;
        }

        // Pie chart con Arc
        Pane piePane = new Pane();
        piePane.setPrefSize(160, 160);
        double cx = 80, cy = 80, radio = 70;
        double anguloActual = -90; // empezar arriba

        int colorIdx = 0;
        List<String[]> leyenda = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : conteoPorId.entrySet()) {
            double grados = 360.0 * entry.getValue() / total;
            String color  = COLORES_REGION[colorIdx % COLORES_REGION.length];
            String nombre = nombrePorId.getOrDefault(entry.getKey(), "Región " + entry.getKey());

            Arc arco = new Arc(cx, cy, radio, radio, anguloActual, -grados);
            arco.setType(ArcType.ROUND);
            arco.setStyle("-fx-fill: " + color + "; -fx-stroke: #0d1624; -fx-stroke-width: 2;");

            piePane.getChildren().add(arco);
            leyenda.add(new String[]{color, nombre, String.valueOf(entry.getValue())});

            anguloActual -= grados;
            colorIdx++;
        }

        // Círculo central (donut effect)
        Circle centro = new Circle(cx, cy, radio * 0.45);
        centro.setStyle("-fx-fill: #1a2234;");
        piePane.getChildren().add(centro);

        // Leyenda
        VBox leyendaBox = new VBox(5);
        for (String[] item : leyenda) {
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            Region cuadro = new Region();
            cuadro.setPrefSize(12, 12);
            cuadro.setStyle("-fx-background-color: " + item[0] + "; -fx-background-radius: 2;");
            Label lbl = new Label(item[1] + " (" + item[2] + ")");
            lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #a09b8c;");
            fila.getChildren().addAll(cuadro, lbl);
            leyendaBox.getChildren().add(fila);
        }

        HBox chartRow = new HBox(20);
        chartRow.setAlignment(Pos.CENTER_LEFT);
        chartRow.getChildren().addAll(piePane, leyendaBox);

        container.getChildren().addAll(titulo, chartRow);
        return container;
    }

    // ══════════════════════════════════════════
    //  TABLA DE ACTIVIDAD RECIENTE
    // ══════════════════════════════════════════

    private VBox buildActividadReciente() {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: #1a2234; -fx-background-radius: 12; -fx-padding: 20;");

        Label titulo = new Label("Actividad Reciente");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        container.getChildren().add(titulo);

        if (actividadReciente.isEmpty()) {
            Label vacio = new Label("No hay actividad registrada en esta sesión.");
            vacio.setStyle("-fx-text-fill: #555e6e; -fx-font-size: 12px; -fx-font-style: italic;");
            container.getChildren().add(vacio);
            return container;
        }

        for (String[] item : actividadReciente) {
            HBox fila = new HBox(12);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(6, 10, 6, 10));
            fila.setStyle("-fx-background-color: #111827; -fx-background-radius: 6;");

            // Badge tipo
            String color = switch (item[0]) {
                case "CREATE" -> "#2ecc71";
                case "UPDATE" -> "#f39c12";
                case "DELETE" -> "#e74c3c";
                case "IMPORT" -> "#3498db";
                default       -> "#7f8c8d";
            };
            Label badge = new Label(item[0]);
            badge.setMinWidth(60);
            badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-size: 10px; -fx-font-weight: bold; " +
                    "-fx-padding: 2 6; -fx-background-radius: 4;");

            Label desc = new Label(item[1]);
            desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #a09b8c;");
            HBox.setHgrow(desc, Priority.ALWAYS);

            Label hora = new Label(item[2]);
            hora.setStyle("-fx-font-size: 11px; -fx-text-fill: #555e6e;");

            fila.getChildren().addAll(badge, desc, hora);
            container.getChildren().add(fila);
        }

        return container;
    }

    // ══════════════════════════════════════════
    //  HELPERS BD
    // ══════════════════════════════════════════

    private int contarRegiones() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM lugares");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error contando regiones: " + e.getMessage());
        }
        return 0;
    }

    private int contarBiografias() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM biografias");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error contando biografías: " + e.getMessage());
        }
        return 0;
    }

    private void cargarNombresLugares(Map<Integer, String> mapa) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, nombre FROM lugares ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getInt("id"), rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.err.println("Error cargando nombres de lugares: " + e.getMessage());
        }
    }
}