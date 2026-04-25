package com.loreruneterra.view;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.db.DatabaseConnector;
import com.loreruneterra.model.Campeon;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vista de detalle de un campeón — "Libro de Runeterra".
 * Muestra: splashart con pasador de skins, nombre, título,
 * clase, región y las 3 modalidades de biografía.
 */
public class ChampionBookView {

    private static final String SPLASH_DIR =
            "C:\\Users\\franz\\Documents\\LoreRuneTerraASSETS\\img\\champion\\splash";

    private final ChampionDAO championDAO = new ChampionDAO();

    public void mostrarLibro(Campeon campeon, Stage ownerStage) {
        Stage libroStage = new Stage();
        libroStage.setTitle(campeon.getNombre() + " — Libro de Runeterra");
        libroStage.initOwner(ownerStage);
        libroStage.initModality(Modality.WINDOW_MODAL);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0f1c;");
        root.setOpacity(0);

        // ── CABECERA ──
        root.setTop(buildHeader(campeon));

        // ── CONTENIDO CENTRAL ──
        HBox contenido = new HBox(30);
        contenido.setPadding(new Insets(20, 30, 10, 30));
        contenido.setAlignment(Pos.TOP_LEFT);

        // Panel izquierdo: splashart + pasador de skins
        VBox splashPanel = buildSplashPanel(campeon);

        // Panel derecho: meta + biografía
        VBox panelDerecho = new VBox(16);
        panelDerecho.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);

        // Meta: clase y región
        HBox meta = buildMeta(campeon);

        // Botones de biografía
        Button btnCorta    = bioButton("📄 Versión Corta");
        Button btnCompleta = bioButton("📖 Versión Completa");
        Button btnPrimera  = bioButton("💬 Primera Persona");

        HBox btnsBio = new HBox(10, btnCorta, btnCompleta, btnPrimera);
        btnsBio.setAlignment(Pos.CENTER_LEFT);

        // Área de texto
        TextArea bioArea = new TextArea();
        bioArea.setWrapText(true);
        bioArea.setEditable(false);
        bioArea.setPrefHeight(420);
        bioArea.setStyle("""
            -fx-control-inner-background: #111827;
            -fx-text-fill: #e6e6e6;
            -fx-font-size: 14px;
            -fx-padding: 15;
        """);
        VBox.setVgrow(bioArea, Priority.ALWAYS);

        // Acciones de botones de biografía
        btnCorta.setOnAction(e -> {
            String bio = getBio(campeon, "corta");
            bioArea.setText(bio != null && !bio.isEmpty() ? bio : "Sin biografía corta disponible.");
            resaltarBoton(btnCorta, btnCompleta, btnPrimera);
        });
        btnCompleta.setOnAction(e -> {
            String bio = getBio(campeon, "completa");
            bioArea.setText(bio != null && !bio.isEmpty() ? bio : "Sin biografía completa disponible.");
            resaltarBoton(btnCompleta, btnCorta, btnPrimera);
        });
        btnPrimera.setOnAction(e -> {
            String bio = getBio(campeon, "primera");
            bioArea.setText(bio != null && !bio.isEmpty() ? bio : "Sin biografía en primera persona disponible.");
            resaltarBoton(btnPrimera, btnCorta, btnCompleta);
        });

        panelDerecho.getChildren().addAll(meta, btnsBio, bioArea);
        contenido.getChildren().addAll(splashPanel, panelDerecho);
        root.setCenter(contenido);

        // ── BOTÓN CERRAR ──
        Button cerrarBtn = new Button("✕  Cerrar");
        cerrarBtn.setStyle("-fx-background-color: #8b1a1a; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10 30; -fx-background-radius: 8;");
        cerrarBtn.setOnAction(e -> libroStage.close());

        HBox footer = new HBox(cerrarBtn);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 30, 20, 30));
        root.setBottom(footer);

        // ── ESCENA ──
        Scene scene = new Scene(root, 1100, 780);
        libroStage.setScene(scene);
        libroStage.show();

        // ── FADE IN ──
        FadeTransition ft = new FadeTransition(Duration.millis(350), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        // ── Cargar biografía corta por defecto ──
        String bioDefault = getBio(campeon, "corta");
        bioArea.setText(bioDefault != null && !bioDefault.isEmpty()
                ? bioDefault : "Sin biografía disponible.");
        resaltarBoton(btnCorta, btnCompleta, btnPrimera);
    }

    // ══════════════════════════════════════════
    //  CABECERA
    // ══════════════════════════════════════════

    private VBox buildHeader(Campeon campeon) {
        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 30, 12, 30));
        header.setStyle("-fx-background-color: #0f1624; " +
                "-fx-border-color: #c8aa6e; -fx-border-width: 0 0 1 0;");

        Label nombre = new Label(campeon.getNombre());
        nombre.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        String tituloStr = campeon.getTitulo() != null && !campeon.getTitulo().isEmpty()
                ? campeon.getTitulo() : "";
        Label titulo = new Label(tituloStr);
        titulo.setStyle("-fx-font-size: 16px; -fx-text-fill: #a09b8c; -fx-font-style: italic;");

        header.getChildren().addAll(nombre, titulo);
        return header;
    }

    // ══════════════════════════════════════════
    //  META: CLASE Y REGIÓN
    // ══════════════════════════════════════════

    private HBox buildMeta(Campeon campeon) {
        HBox meta = new HBox(16);
        meta.setAlignment(Pos.CENTER_LEFT);

        String clase = (campeon.getClase() != null && !campeon.getClase().isEmpty())
                ? campeon.getClase() : "Desconocida";
        meta.getChildren().add(metaBadge("⚔  " + clase, "#1a3a5c", "#c8aa6e"));

        String region = obtenerNombreLugar(campeon.getLugarId());
        meta.getChildren().add(metaBadge("🌍  " + region, "#1a2e1a", "#4caf50"));

        return meta;
    }

    private VBox metaBadge(String texto, String bg, String color) {
        VBox badge = new VBox();
        badge.setAlignment(Pos.CENTER);
        badge.setPadding(new Insets(8, 16, 8, 16));
        badge.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;");
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        badge.getChildren().add(lbl);
        return badge;
    }

    // ══════════════════════════════════════════
    //  SPLASHART CON PASADOR DE SKINS
    // ══════════════════════════════════════════

    private VBox buildSplashPanel(Campeon campeon) {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setMinWidth(440);

        // Cargar lista de skins disponibles
        List<File> skins = cargarSkins(campeon);

        ImageView img = new ImageView();
        img.setFitWidth(420);
        img.setFitHeight(480);
        img.setPreserveRatio(true);
        img.setSmooth(true);

        // Índice actual de skin
        int[] idx = {0};

        // Función para cargar imagen con fade
        Runnable cargarImagen = () -> {
            if (skins.isEmpty()) {
                // Fallback: icono web de DataDragon
                String iconUrl = campeon.getImagen();
                if (iconUrl != null && iconUrl.startsWith("http")) {
                    img.setImage(new Image(iconUrl, true));
                }
                return;
            }
            FadeTransition ftOut = new FadeTransition(Duration.millis(200), img);
            ftOut.setFromValue(1.0);
            ftOut.setToValue(0.0);
            ftOut.setOnFinished(e -> {
                try {
                    img.setImage(new Image(skins.get(idx[0]).toURI().toString()));
                } catch (Exception ex) {
                    System.err.println("Error cargando skin: " + ex.getMessage());
                }
                FadeTransition ftIn = new FadeTransition(Duration.millis(200), img);
                ftIn.setFromValue(0.0);
                ftIn.setToValue(1.0);
                ftIn.play();
            });
            ftOut.play();
        };

        // Cargar imagen inicial sin animación
        if (!skins.isEmpty()) {
            try {
                img.setImage(new Image(skins.get(0).toURI().toString()));
            } catch (Exception e) {
                System.err.println("Error cargando skin inicial: " + e.getMessage());
            }
        } else {
            String iconUrl = campeon.getImagen();
            if (iconUrl != null && iconUrl.startsWith("http")) {
                img.setImage(new Image(iconUrl, true));
            }
        }

        // Label contador de skin
        Label lblContador = new Label();
        lblContador.setStyle("-fx-font-size: 11px; -fx-text-fill: #555e6e;");

        Runnable actualizarContador = () -> {
            if (!skins.isEmpty()) {
                String nombre = skins.get(idx[0]).getName()
                        .replace(".jpg", "").replace(".png", "");
                lblContador.setText("Skin " + (idx[0] + 1) + " / "
                        + skins.size() + "  —  " + nombre);
            } else {
                lblContador.setText("Sin skins locales");
            }
        };
        actualizarContador.run();

        // Botones ◀ ▶
        String estiloBtn = "-fx-background-color: #1a2234; -fx-text-fill: #c8aa6e; " +
                "-fx-font-size: 18px; -fx-padding: 8 20; -fx-background-radius: 6;";

        Button btnAnterior = new Button("◀");
        Button btnSiguiente = new Button("▶");
        btnAnterior.setStyle(estiloBtn);
        btnSiguiente.setStyle(estiloBtn);

        boolean soloUna = skins.size() <= 1;
        btnAnterior.setDisable(soloUna);
        btnSiguiente.setDisable(soloUna);

        btnAnterior.setOnAction(e -> {
            idx[0] = (idx[0] - 1 + skins.size()) % skins.size();
            cargarImagen.run();
            actualizarContador.run();
        });

        btnSiguiente.setOnAction(e -> {
            idx[0] = (idx[0] + 1) % skins.size();
            cargarImagen.run();
            actualizarContador.run();
        });

        HBox controles = new HBox(16, btnAnterior, lblContador, btnSiguiente);
        controles.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(img, controles);
        return panel;
    }

    /**
     * Escanea la carpeta de splasharts y devuelve todos los ficheros
     * que corresponden al campeón (Key_N.jpg), ordenados por nombre.
     */
    private List<File> cargarSkins(Campeon campeon) {
        List<File> skins = new ArrayList<>();

        // Campeón personalizado con splashart propio
        if (campeon.getImagenSplash() != null && !campeon.getImagenSplash().isEmpty()) {
            String clean = campeon.getImagenSplash()
                    .replace("file:///", "").replace("file://", "");
            File f = new File(clean);
            if (f.exists() && f.canRead()) {
                skins.add(f);
                return skins;
            }
        }

        // Escanear carpeta local de skins
        File carpeta = new File(SPLASH_DIR);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] archivos = carpeta.listFiles((dir, name) ->
                    name.startsWith(campeon.getKey() + "_") &&
                            (name.endsWith(".jpg") || name.endsWith(".png")));
            if (archivos != null && archivos.length > 0) {
                Arrays.sort(archivos);
                skins.addAll(Arrays.asList(archivos));
            }
        }

        return skins;
    }

    // ══════════════════════════════════════════
    //  BIOGRAFÍAS
    // ══════════════════════════════════════════

    private String getBio(Campeon campeon, String tipo) {
        // Memoria primero
        String bio = switch (tipo) {
            case "corta"    -> campeon.getBioCorta();
            case "completa" -> campeon.getBioCompleta();
            case "primera"  -> campeon.getBioPrimera();
            default         -> "";
        };
        if (bio != null && !bio.isEmpty()) return bio;

        // BD como fallback
        return switch (tipo) {
            case "corta"    -> championDAO.getBiografiaCorta(campeon.getKey());
            case "completa" -> championDAO.getBiografiaCompleta(campeon.getKey());
            case "primera"  -> championDAO.getBiografiaPrimeraPersona(campeon.getKey());
            default         -> "";
        };
    }

    // ══════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════

    private Button bioButton(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: #1a2234; -fx-text-fill: #a09b8c; " +
                "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6;");
        return btn;
    }

    private void resaltarBoton(Button activo, Button... otros) {
        activo.setStyle("-fx-background-color: #c8aa6e; -fx-text-fill: #0a0f1c; " +
                "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                "-fx-font-weight: bold;");
        for (Button btn : otros) {
            btn.setStyle("-fx-background-color: #1a2234; -fx-text-fill: #a09b8c; " +
                    "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6;");
        }
    }

    private String obtenerNombreLugar(int lugarId) {
        if (lugarId <= 0) return "Desconocida";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nombre FROM lugares WHERE id = ?")) {
            ps.setInt(1, lugarId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nombre");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo nombre de lugar: " + e.getMessage());
        }
        return "Desconocida";
    }
}