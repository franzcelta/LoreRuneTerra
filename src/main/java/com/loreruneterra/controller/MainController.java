package com.loreruneterra.controller;

import com.loreruneterra.db.CampeonPersonalDAO;
import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.db.DatabaseConnector;
import com.loreruneterra.model.Campeon;
import com.loreruneterra.view.ChampionBookView;
import com.loreruneterra.view.DashboardView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal de LoreRuneTerra.
 * Gestiona navegación, catálogo, filtros, CRUD, importación DataDragon y vista de lugares.
 *
 * Funcionalidades implementadas:
 *  - Catálogo con FlowPane de tarjetas
 *  - Filtro por texto (nombre), clase y lugar — combinables
 *  - Animaciones FadeTransition entre vistas
 *  - CRUD completo (Create, Read, Update, Delete) con persistencia PostgreSQL
 *  - Vista de Lugares/Regiones con imagen y descripción
 *  - Importación DataDragon (ver DataDragonImporter)
 */
public class MainController {

    // ── Constantes de clase disponibles ──
    private static final String[] CLASES = {
            "Todas", "Asesino", "Luchador", "Mago", "Marksman", "Soporte", "Tanque"
    };

    private final ChampionDAO        championDAO;
    private final CampeonPersonalDAO personalDAO;
    private final ObservableList<Campeon> campeonesList;
    private final BorderPane root = new BorderPane();
    private Stage primaryStage;

    // ── Estado de filtros activos ──
    private String filtroTexto  = "";
    private String filtroClase  = "Todas";
    private int    filtroLugar  = 0;   // 0 = todos

    // ── Lista de lugares cargada una vez al iniciar ──
    private final List<String[]> lugares = new ArrayList<>(); // [id, nombre, desc, imagen_url]

    public MainController(ChampionDAO championDAO,
                          ObservableList<Campeon> campeonesList) {
        this.championDAO   = championDAO;
        this.personalDAO   = new CampeonPersonalDAO();
        this.campeonesList = campeonesList;
        cargarLugares();
        showWelcomeScreen();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // ══════════════════════════════════════════
    //  CARGA DE LUGARES desde BD
    // ══════════════════════════════════════════

    private void cargarLugares() {
        lugares.clear();
        String sql = "SELECT id, nombre, descripcion, imagen_url FROM lugares ORDER BY nombre ASC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lugares.add(new String[]{
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("imagen_url")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error cargando lugares: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════
    //  ANIMACIÓN
    // ══════════════════════════════════════════

    /** Aplica FadeIn a cualquier nodo (transición de entrada suave). */
    private void fadeIn(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void setCenter(Node node) {
        Node actual = root.getCenter();
        if (actual == null) {
            root.setCenter(node);
            fadeIn(node);
            return;
        }

        // Preparar nueva pantalla fuera de vista (a la derecha)
        node.setTranslateX(root.getWidth());
        node.setOpacity(0);
        root.setCenter(node);

        // Nueva pantalla entra desde la derecha
        TranslateTransition ttIn = new TranslateTransition(Duration.millis(320), node);
        ttIn.setFromX(root.getWidth() > 0 ? root.getWidth() : 1300);
        ttIn.setToX(0);

        FadeTransition ftIn = new FadeTransition(Duration.millis(320), node);
        ftIn.setFromValue(0.0);
        ftIn.setToValue(1.0);

        ParallelTransition entrada = new ParallelTransition(ttIn, ftIn);
        entrada.play();
    }

    // ══════════════════════════════════════════
    //  PANTALLA DE BIENVENIDA
    // ══════════════════════════════════════════

    private void showWelcomeScreen() {
        VBox welcome = new VBox(40);
        welcome.setAlignment(Pos.CENTER);
        welcome.setStyle("-fx-background-color: #0a0f1c;");

        Label title = new Label("LoreRuneTerra");
        title.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        Label subtitle = new Label("Explora las historias y leyendas de Runeterra");
        subtitle.setStyle("-fx-font-size: 22px; -fx-text-fill: #a09b8c;");

        // Botones del menú principal
        Button btnCampeones = createMenuButton("⚔  Campeones",   "#c8aa6e", "#0a0f1c");
        Button btnLugares   = createMenuButton("🌍  Regiones",    "#4a7fa5", "white");
        Button btnDashboard = createMenuButton("📊  Dashboard",   "#5a2d82", "white");
        Button btnImportar  = createMenuButton("⬇  Importar DataDragon", "#2d6a4f", "white");

        btnCampeones.setOnAction(e -> showChampionScreen());
        btnLugares.setOnAction(e -> showLugaresScreen());
        btnDashboard.setOnAction(e -> showDashboardScreen());
        btnImportar.setOnAction(e -> showImportScreen());

        VBox botones = new VBox(16, btnCampeones, btnLugares, btnDashboard, btnImportar);
        botones.setAlignment(Pos.CENTER);

        welcome.getChildren().addAll(title, subtitle, botones);
        setCenter(welcome);
    }

    private Button createMenuButton(String texto, String bgColor, String textColor) {
        Button btn = new Button(texto);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                        "-fx-font-size: 20px; -fx-padding: 18 60; -fx-background-radius: 12; " +
                        "-fx-min-width: 340px;", bgColor, textColor));
        return btn;
    }

    // ══════════════════════════════════════════
    //  BARRA SUPERIOR (reutilizable)
    // ══════════════════════════════════════════

    private HBox buildTopBar(String titulo) {
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(14, 20, 14, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #0f0f0f;");

        Button btnVolver = new Button("← Menú");
        btnVolver.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #c8aa6e; " +
                "-fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 8;");
        btnVolver.setOnAction(e -> showWelcomeScreen());

        Label lbl = new Label(titulo);
        lbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e8d5a3;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(btnVolver, spacer, lbl);
        return topBar;
    }

    // ══════════════════════════════════════════
    //  PANTALLA DE CAMPEONES
    // ══════════════════════════════════════════

    private void showChampionScreen() {
        // Resetear filtros al entrar
        filtroTexto = "";
        filtroClase = "Todas";
        filtroLugar = 0;

        VBox page = new VBox();
        page.setStyle("-fx-background-color: #0d1624;");

        // ── Top bar ──
        HBox topBar = buildTopBar("Campeones de Runeterra");

        // ── Barra de filtros ──
        HBox filtros = buildFiltrosBar();

        // ── FlowPane de tarjetas ──
        FlowPane flow = new FlowPane();
        flow.setHgap(28);
        flow.setVgap(28);
        flow.setPadding(new Insets(30));
        flow.setStyle("-fx-background-color: #0d1624;");

        renderCards(flow, campeonesList);
        flow.getChildren().add(createNewChampionCard(flow));

        // ── Conectar filtros al FlowPane ──
        conectarFiltros(filtros, flow);

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #0d1624; -fx-background: #0d1624;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        page.getChildren().addAll(topBar, filtros, scroll);
        setCenter(page);
    }

    // ── Barra de filtros (texto + clase + lugar) ──
    private HBox buildFiltrosBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #111827;");

        // Texto libre
        TextField searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("🔍  Buscar campeón...");
        searchField.setPrefWidth(280);
        searchField.setPrefHeight(36);
        searchField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #888; -fx-font-size: 14px; -fx-padding: 6 12;");

        // ComboBox clase
        ComboBox<String> cbClase = new ComboBox<>();
        cbClase.setId("cbClase");
        cbClase.getItems().addAll(CLASES);
        cbClase.setValue("Todas");
        cbClase.setPrefHeight(36);
        cbClase.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-font-size: 13px;");
        cbClase.setPromptText("Clase");

        // ComboBox lugar
        ComboBox<String> cbLugar = new ComboBox<>();
        cbLugar.setId("cbLugar");
        cbLugar.getItems().add("Todos los lugares");
        for (String[] l : lugares) cbLugar.getItems().add(l[1]);
        cbLugar.setValue("Todos los lugares");
        cbLugar.setPrefHeight(36);
        cbLugar.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-font-size: 13px;");

        // Botón limpiar filtros
        Button btnLimpiar = new Button("✕ Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #3d2d2d; -fx-text-fill: #c8aa6e; " +
                "-fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 6;");

        // Label contador
        Label lblContador = new Label();
        lblContador.setId("lblContador");
        lblContador.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        btnLimpiar.setOnAction(e -> {
            searchField.clear();
            cbClase.setValue("Todas");
            cbLugar.setValue("Todos los lugares");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(searchField, cbClase, cbLugar, btnLimpiar, spacer, lblContador);
        return bar;
    }

    // ── Conecta los listeners de filtros al FlowPane ──
    private void conectarFiltros(HBox filtros, FlowPane flow) {
        TextField  search   = (TextField)  filtros.lookup("#searchField");
        ComboBox<String> cbClase = (ComboBox<String>) filtros.lookup("#cbClase");
        ComboBox<String> cbLugar = (ComboBox<String>) filtros.lookup("#cbLugar");
        Label      counter  = (Label)      filtros.lookup("#lblContador");

        Runnable aplicarFiltros = () -> {
            filtroTexto = search.getText() == null ? "" : search.getText().toLowerCase().trim();
            filtroClase = cbClase.getValue()  == null ? "Todas" : cbClase.getValue();
            String lugarSel = cbLugar.getValue();
            filtroLugar = 0;
            if (lugarSel != null && !lugarSel.equals("Todos los lugares")) {
                for (String[] l : lugares) {
                    if (l[1].equals(lugarSel)) {
                        filtroLugar = Integer.parseInt(l[0]);
                        break;
                    }
                }
            }

            flow.getChildren().clear();
            List<Campeon> filtrados = filtrar(campeonesList);
            renderCards(flow, filtrados);
            flow.getChildren().add(createNewChampionCard(flow));

            int total = filtrados.size();
            counter.setText(total + " campeón" + (total != 1 ? "es" : ""));
        };

        search.textProperty().addListener((o, v, n) -> aplicarFiltros.run());
        cbClase.valueProperty().addListener((o, v, n) -> aplicarFiltros.run());
        cbLugar.valueProperty().addListener((o, v, n) -> aplicarFiltros.run());

        // Mostrar contador inicial
        counter.setText(campeonesList.size() + " campeones");
    }

    // ── Lógica de filtrado combinada ──
    private List<Campeon> filtrar(List<Campeon> todos) {
        List<Campeon> resultado = new ArrayList<>();
        for (Campeon c : todos) {
            // Filtro texto
            if (!filtroTexto.isEmpty() &&
                    !c.getNombre().toLowerCase().contains(filtroTexto)) continue;
            // Filtro clase
            if (!"Todas".equals(filtroClase) &&
                    !filtroClase.equalsIgnoreCase(c.getClase())) continue;
            // Filtro lugar
            if (filtroLugar > 0 && c.getLugarId() != filtroLugar) continue;
            resultado.add(c);
        }
        return resultado;
    }

    // ── Renderiza las tarjetas en el FlowPane ──
    private void renderCards(FlowPane flow, List<Campeon> lista) {
        for (Campeon c : lista) {
            flow.getChildren().add(createChampionCard(c, flow));
        }
    }

    // ══════════════════════════════════════════
    //  TARJETAS DE CAMPEÓN
    // ══════════════════════════════════════════

    private VBox createChampionCard(Campeon campeon, FlowPane flow) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(190);
        card.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 14; -fx-padding: 14;");

        // ── Efecto hover estilo LoL ──
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.06);
            st.setToY(1.06);
            st.play();
            card.setStyle(card.getStyle() +
                    "-fx-effect: dropshadow(gaussian, #c8aa6e, 18, 0.4, 0, 0);");
        });

        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            card.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 14; -fx-padding: 14;");
        });

        // Imagen
        ImageView img = loadImage(
                campeon.getImagenIcono() != null && !campeon.getImagenIcono().isEmpty()
                        ? campeon.getImagenIcono() : campeon.getImagen(),
                160, 160);

        // Nombre
        Label name = new Label(campeon.getNombre());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-wrap-text: true;");
        name.setMaxWidth(170);

        // Clase y lugar
        String claseStr  = (campeon.getClase() != null && !campeon.getClase().isEmpty())
                ? campeon.getClase() : "–";
        String lugarStr  = nombreLugar(campeon.getLugarId());
        Label  meta = new Label(claseStr + "  ·  " + lugarStr);
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #c8aa6e; -fx-wrap-text: true;");
        meta.setMaxWidth(170);

        // Badge personalizado
        if (esPersonalizado(campeon)) {
            Label badge = new Label("✦ Personalizado");
            badge.setStyle("-fx-font-size: 10px; -fx-text-fill: #a09b8c;");
            card.getChildren().addAll(img, name, meta, badge);
        } else {
            card.getChildren().addAll(img, name, meta);
        }

        // Botones CRUD
        HBox botones = new HBox(6);
        botones.setAlignment(Pos.CENTER);

        Button btnEditar   = smallButton("✏", "#c8aa6e", "#0a0f1c");
        Button btnEliminar = smallButton("🗑", "#8b1a1a", "white");

        btnEditar.setOnAction(e -> { e.consume(); abrirFormularioEditar(campeon, flow); });
        btnEliminar.setOnAction(e -> { e.consume(); confirmarEliminar(campeon, flow); });

        botones.getChildren().addAll(btnEditar, btnEliminar);
        card.getChildren().add(botones);

        // Clic en la tarjeta → detalle
        card.setOnMouseClicked(e -> {
            if (e.getTarget() == card || e.getTarget() instanceof ImageView
                    || e.getTarget() instanceof Label) {
                ChampionBookView view = new ChampionBookView();
                view.mostrarLibro(campeon, primaryStage);
            }
        });

        return card;
    }

    private VBox createNewChampionCard(FlowPane flow) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(190);
        card.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 14; " +
                "-fx-padding: 14; -fx-border-color: #c8aa6e; " +
                "-fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 14;");
        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 52px; -fx-text-fill: #c8aa6e; -fx-font-weight: bold;");
        Label txt = new Label("Nuevo Campeón");
        txt.setStyle("-fx-font-size: 14px; -fx-text-fill: #c8aa6e;");
        card.getChildren().addAll(plus, txt);
        card.setOnMouseClicked(e -> abrirFormularioNuevoCampeon(flow));
        return card;
    }

    // ══════════════════════════════════════════
    //  PANTALLA DE LUGARES / REGIONES
    // ══════════════════════════════════════════

    private void showLugaresScreen() {
        VBox page = new VBox();
        page.setStyle("-fx-background-color: #0d1624;");

        HBox topBar = buildTopBar("Regiones de Runeterra");

        FlowPane flow = new FlowPane();
        flow.setHgap(28);
        flow.setVgap(28);
        flow.setPadding(new Insets(30));
        flow.setStyle("-fx-background-color: #0d1624;");

        for (String[] lugar : lugares) {
            flow.getChildren().add(createLugarCard(lugar));
        }

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #0d1624; -fx-background: #0d1624;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        page.getChildren().addAll(topBar, scroll);
        setCenter(page);
    }

    private VBox createLugarCard(String[] lugar) {
        // lugar = [id, nombre, descripcion, imagen_url]
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 14; -fx-padding: 18;");

        ImageView img = loadImage(lugar[3], 220, 130);
        img.setStyle("-fx-background-radius: 8;");

        Label nombre = new Label(lugar[1]);
        nombre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        Label desc = new Label(lugar[2] != null ? lugar[2] : "");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #a09b8c; -fx-wrap-text: true;");
        desc.setMaxWidth(230);
        desc.setWrapText(true);

        // Contar campeones de este lugar
        int idLugar = Integer.parseInt(lugar[0]);
        long count = campeonesList.stream()
                .filter(c -> c.getLugarId() == idLugar)
                .count();
        Label countLabel = new Label(count + " campeón" + (count != 1 ? "es" : ""));
        countLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555e6e; -fx-font-style: italic;");

        card.getChildren().addAll(img, nombre, desc, countLabel);
        return card;
    }

    // ══════════════════════════════════════════
    //  PANTALLA DE IMPORTACIÓN DATADRAGON
    // ══════════════════════════════════════════

    private void showDashboardScreen() {
        VBox page = new VBox();
        page.setStyle("-fx-background-color: #0d1624;");
        HBox topBar = buildTopBar("Dashboard de Estadísticas");
        DashboardView dashboard = new DashboardView(campeonesList);
        ScrollPane scroll = dashboard.buildView();
        VBox.setVgrow(scroll, Priority.ALWAYS);
        page.getChildren().addAll(topBar, scroll);
        setCenter(page);
    }

    private void showImportScreen() {
        VBox page = new VBox(20);
        page.setStyle("-fx-background-color: #0d1624;");
        page.setPadding(new Insets(0, 0, 30, 0));

        HBox topBar = buildTopBar("Importar desde DataDragon");

        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.TOP_CENTER);

        Label info = new Label(
                "DataDragon es la API oficial de Riot Games que proporciona\n" +
                        "datos actualizados de todos los campeones de League of Legends.\n\n" +
                        "Al importar, el sistema descargará los datos de la versión más\n" +
                        "reciente y añadirá los campeones nuevos a la base de datos.\n" +
                        "Los campeones ya existentes no se duplicarán.");
        info.setStyle("-fx-font-size: 14px; -fx-text-fill: #a09b8c; -fx-text-alignment: center;");
        info.setWrapText(true);
        info.setMaxWidth(500);

        Label lblVersion = new Label("Versión actual: cargando...");
        lblVersion.setStyle("-fx-font-size: 13px; -fx-text-fill: #c8aa6e;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(480);
        progressBar.setVisible(false);

        Label lblEstado = new Label("");
        lblEstado.setStyle("-fx-font-size: 13px; -fx-text-fill: #a09b8c;");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setPrefWidth(480);
        logArea.setStyle("-fx-background-color: #111827; -fx-text-fill: #a0c4a0; -fx-font-size: 12px;");
        logArea.setVisible(false);

        Button btnImportar = new Button("⬇  Iniciar Importación");
        btnImportar.setStyle("-fx-background-color: #2d6a4f; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 14 40; -fx-background-radius: 10;");

        btnImportar.setOnAction(e -> {
            btnImportar.setDisable(true);
            progressBar.setVisible(true);
            logArea.setVisible(true);
            logArea.clear();
            lblEstado.setText("Iniciando importación...");

            // Lanzar importación en hilo separado para no bloquear la UI
            Thread hilo = new Thread(() -> {
                try {
                    com.loreruneterra.importer.DataDragonImporter importer =
                            new com.loreruneterra.importer.DataDragonImporter(
                                    championDAO,
                                    (msg) -> javafx.application.Platform.runLater(() -> {
                                        logArea.appendText(msg + "\n");
                                        logArea.setScrollTop(Double.MAX_VALUE);
                                    }),
                                    (progress) -> javafx.application.Platform.runLater(() ->
                                            progressBar.setProgress(progress)
                                    )
                            );
                    int nuevos = importer.importar();
                    javafx.application.Platform.runLater(() -> {
                        DashboardView.registrarActividad("IMPORT",
                                "DataDragon v" + "16.8.1" + " — " + nuevos + " nuevos");
                        lblEstado.setText("✓ Completado: " + nuevos + " campeón"
                                + (nuevos != 1 ? "es" : "") + " nuevos importados.");
                        lblEstado.setStyle("-fx-font-size: 13px; -fx-text-fill: #4caf50;");
                        btnImportar.setDisable(false);
                        progressBar.setProgress(1.0);
                        // Recargar lista
                        campeonesList.setAll(championDAO.getAllCampeones());
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        lblEstado.setText("✗ Error: " + ex.getMessage());
                        lblEstado.setStyle("-fx-font-size: 13px; -fx-text-fill: #f44336;");
                        btnImportar.setDisable(false);
                    });
                }
            });
            hilo.setDaemon(true);
            hilo.start();
        });

        content.getChildren().addAll(info, lblVersion, btnImportar,
                progressBar, lblEstado, logArea);
        VBox.setVgrow(content, Priority.ALWAYS);
        page.getChildren().addAll(topBar, content);
        setCenter(page);
    }

    // ══════════════════════════════════════════
    //  CRUD: CREATE
    // ══════════════════════════════════════════

    private void abrirFormularioNuevoCampeon(FlowPane flow) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Campeón Personalizado");
        dialog.setHeaderText("Crea tu propio campeón");

        VBox content = buildFormulario(null);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(520);
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                TextField nombreField      = (TextField) content.lookup("#nombreField");
                TextField tituloField      = (TextField) content.lookup("#tituloField");
                TextField claseField       = (TextField) content.lookup("#claseField");
                TextField iconoField       = (TextField) content.lookup("#iconoField");
                TextField splashField      = (TextField) content.lookup("#splashField");
                TextArea  bioCortaField    = (TextArea)  content.lookup("#bioCortaField");
                TextArea  bioCompletaField = (TextArea)  content.lookup("#bioCompletaField");
                TextArea  bioPrimeraField  = (TextArea)  content.lookup("#bioPrimeraField");

                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "El nombre no puede estar vacío.");
                    return null;
                }

                Campeon nuevo = new Campeon(
                        "custom_" + nombre.toLowerCase().replaceAll("\\s+", "_")
                                + "_" + System.currentTimeMillis(),
                        nombre, tituloField.getText().trim(), "");
                nuevo.setClase        (claseField.getText().trim());
                nuevo.setImagenIcono  (iconoField.getText().trim());
                nuevo.setImagenSplash (splashField.getText().trim());
                nuevo.setBioCorta     (bioCortaField.getText().trim());
                nuevo.setBioCompleta  (bioCompletaField.getText().trim());
                nuevo.setBioPrimera   (bioPrimeraField.getText().trim());

                boolean ok = personalDAO.create(nuevo);
                if (ok) {
                    campeonesList.add(nuevo);
                    DashboardView.registrarActividad("CREATE", "Añadido: " + nuevo.getNombre());
                    flow.getChildren().clear();
                    renderCards(flow, filtrar(campeonesList));
                    flow.getChildren().add(createNewChampionCard(flow));
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Campeón creado",
                            "'" + nuevo.getNombre() + "' añadido correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "No se pudo guardar en la base de datos.");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════
    //  CRUD: UPDATE
    // ══════════════════════════════════════════

    private void abrirFormularioEditar(Campeon campeon, FlowPane flow) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Editar Campeón");
        dialog.setHeaderText("Editando: " + campeon.getNombre());

        VBox content = buildFormulario(campeon);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(520);
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                TextField nombreField      = (TextField) content.lookup("#nombreField");
                TextField tituloField      = (TextField) content.lookup("#tituloField");
                TextField claseField       = (TextField) content.lookup("#claseField");
                TextField iconoField       = (TextField) content.lookup("#iconoField");
                TextField splashField      = (TextField) content.lookup("#splashField");
                TextArea  bioCortaField    = (TextArea)  content.lookup("#bioCortaField");
                TextArea  bioCompletaField = (TextArea)  content.lookup("#bioCompletaField");
                TextArea  bioPrimeraField  = (TextArea)  content.lookup("#bioPrimeraField");

                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "El nombre no puede estar vacío.");
                    return null;
                }

                campeon.setNombre      (nombre);
                campeon.setTitulo      (tituloField.getText().trim());
                campeon.setClase       (claseField.getText().trim());
                campeon.setImagenIcono (iconoField.getText().trim());
                campeon.setImagenSplash(splashField.getText().trim());
                campeon.setBioCorta    (bioCortaField.getText().trim());
                campeon.setBioCompleta (bioCompletaField.getText().trim());
                campeon.setBioPrimera  (bioPrimeraField.getText().trim());

                boolean ok;
                if (esPersonalizado(campeon)) {
                    ok = personalDAO.update(campeon);
                } else {
                    ok = championDAO.updateCampeon(campeon);
                    if (ok) championDAO.saveBiografia(campeon.getKey(),
                            campeon.getBioCorta(), campeon.getBioCompleta(), campeon.getBioPrimera());
                }

                if (ok) {
                    DashboardView.registrarActividad("UPDATE", "Editado: " + campeon.getNombre());
                    flow.getChildren().clear();
                    renderCards(flow, filtrar(campeonesList));
                    flow.getChildren().add(createNewChampionCard(flow));
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado",
                            "'" + campeon.getNombre() + "' actualizado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "No se pudo actualizar en la base de datos.");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════
    //  CRUD: DELETE
    // ══════════════════════════════════════════

    private void confirmarEliminar(Campeon campeon, FlowPane flow) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar campeón");
        confirm.setHeaderText("¿Eliminar a '" + campeon.getNombre() + "'?");
        confirm.setContentText("Esta acción es irreversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok;
                if (esPersonalizado(campeon)) {
                    // Intentar primero en campeones_personalizados
                    ok = personalDAO.delete(campeon.getKey());
                    // Si no estaba ahí, buscar en campeones (datos legacy)
                    if (!ok) {
                        ok = championDAO.deleteCampeon(campeon.getId());
                    }
                } else {
                    ok = championDAO.deleteCampeon(campeon.getId());
                }
                if (ok) {
                    DashboardView.registrarActividad("DELETE", "Eliminado: " + campeon.getNombre());
                    campeonesList.remove(campeon);
                    flow.getChildren().clear();
                    renderCards(flow, filtrar(campeonesList));
                    flow.getChildren().add(createNewChampionCard(flow));
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado",
                            "'" + campeon.getNombre() + "' eliminado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "No se pudo eliminar de la base de datos.");
                }
            }
        });
    }

    // ══════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════

    private boolean esPersonalizado(Campeon c) {
        return c.getKey() != null && c.getKey().startsWith("custom_");
    }

    private String nombreLugar(int lugarId) {
        if (lugarId <= 0) return "–";
        for (String[] l : lugares) {
            if (Integer.parseInt(l[0]) == lugarId) return l[1];
        }
        return "–";
    }

    private ImageView loadImage(String ruta, double w, double h) {
        ImageView img = new ImageView();
        img.setFitWidth(w);
        img.setFitHeight(h);
        img.setPreserveRatio(true);
        if (ruta == null || ruta.isEmpty()) return img;
        try {
            if (ruta.startsWith("http://") || ruta.startsWith("https://")) {
                img.setImage(new Image(ruta, true));
            } else if (ruta.startsWith("file:///")) {
                // Usar URI directamente para manejar espacios en la ruta
                img.setImage(new Image(ruta.replace(" ", "%20")));
            } else {
                String clean = ruta.replace("file:///", "").replace("file://", "");
                File file = new File(clean);
                if (file.exists() && file.canRead()) {
                    img.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando imagen: " + ruta + " → " + e.getMessage());
        }
        return img;
    }

    private Button smallButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; " +
                        "-fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6;", bg, fg));
        return btn;
    }

    private VBox buildFormulario(Campeon c) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nombreField = new TextField(c != null ? c.getNombre() : "");
        nombreField.setId("nombreField"); nombreField.setPromptText("Nombre *");

        TextField tituloField = new TextField(c != null ? c.getTitulo() : "");
        tituloField.setId("tituloField"); tituloField.setPromptText("Título");

        TextField claseField = new TextField(c != null ? c.getClase() : "");
        claseField.setId("claseField"); claseField.setPromptText("Clase");

        TextField iconoField = new TextField(c != null ? c.getImagenIcono() : "");
        iconoField.setId("iconoField"); iconoField.setPromptText("Ruta icono");

        TextField splashField = new TextField(c != null ? c.getImagenSplash() : "");
        splashField.setId("splashField"); splashField.setPromptText("Ruta splashart");

        TextArea bioCortaField = new TextArea(c != null ? c.getBioCorta() : "");
        bioCortaField.setId("bioCortaField"); bioCortaField.setPromptText("Biografía corta");
        bioCortaField.setPrefHeight(80);

        TextArea bioCompletaField = new TextArea(c != null ? c.getBioCompleta() : "");
        bioCompletaField.setId("bioCompletaField"); bioCompletaField.setPromptText("Biografía completa");
        bioCompletaField.setPrefHeight(120);

        TextArea bioPrimeraField = new TextArea(c != null ? c.getBioPrimera() : "");
        bioPrimeraField.setId("bioPrimeraField"); bioPrimeraField.setPromptText("Primera persona (opcional)");
        bioPrimeraField.setPrefHeight(100);

        content.getChildren().addAll(
                new Label("Nombre: *"), nombreField,
                new Label("Título:"), tituloField,
                new Label("Clase:"), claseField,
                new Label("Icono:"), iconoField,
                new Label("Splashart:"), splashField,
                new Label("Bio corta:"), bioCortaField,
                new Label("Bio completa:"), bioCompletaField,
                new Label("Bio primera persona:"), bioPrimeraField
        );
        return content;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public BorderPane getRoot() { return root; }
}