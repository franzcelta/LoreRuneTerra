package com.loreruneterra.controller;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;

import com.loreruneterra.view.BiographyEditorDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.*;

import java.io.File;
import java.util.List;

public class MainController {

    private final ChampionDAO championDAO;
    private final ObservableList<Campeon> campeonesList;

    // Nodos principales (campos para que listeners puedan acceder)
    private final BorderPane root = new BorderPane();
    private final SplitPane splitPane = new SplitPane();
    private final TableView<Campeon> table = new TableView<>();
    private final VBox detallesPanel = new VBox(10);
    private final Label lblNombreDetalles = new Label();
    private final Label lblTituloDetalles = new Label();
    private final ImageView imgSplashDetalles = new ImageView();
    private final TextArea txtBiografia = new TextArea("Selecciona un campeón para ver su biografía.");
    private final Button btnEditarBio = new Button("Editar biografía");
    private final Button btnCerrarDetalles = new Button("Cerrar libro");
    private final ScrollPane scrollBio = new ScrollPane();
    private final ScrollPane scrollDetalles = new ScrollPane();
    private Campeon campeonSeleccionado = null;

    public MainController(ChampionDAO championDAO, ObservableList<Campeon> campeonesList) {
        this.championDAO = championDAO;
        this.campeonesList = campeonesList;

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        // Panel superior compacto
        VBox topBox = new VBox(5);
        topBox.setPadding(new Insets(8));
        topBox.setStyle("-fx-background-color: #0f0f0f;");

        Label tituloApp = new Label("LoreRuneTerra - Campeones de Runeterra");
        tituloApp.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setMaxWidth(350);
        searchField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: gray;");

        topBox.getChildren().addAll(tituloApp, searchField);
        root.setTop(topBox);

        // SplitPane
        splitPane.setDividerPositions(0.65);
        splitPane.setStyle("-fx-background-color: transparent;");

        // Tabla izquierda
        table.setItems(campeonesList);
        table.setStyle("-fx-background-color: #0d1624;");

        // Columnas
        TableColumn<Campeon, String> colImagen = new TableColumn<>("Imagen");
        colImagen.setPrefWidth(70);
        colImagen.setCellFactory(param -> new TableCell<Campeon, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);

                if (empty || url == null || url.trim().isEmpty()) {
                    setGraphic(null);
                    return;
                }

                try {
                    String rutaLimpia = url.replace("file:///", "");
                    File file = new File(rutaLimpia);
                    if (file.exists() && file.canRead()) {
                        imageView.setImage(new Image(file.toURI().toString()));
                    } else {
                        System.out.println("Imagen no encontrada: " + rutaLimpia);
                        imageView.setImage(null);
                    }
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                } catch (Exception e) {
                    System.err.println("Error al cargar imagen: " + url);
                    imageView.setImage(null);
                }
                setGraphic(imageView);
            }
        });
        colImagen.setCellValueFactory(new PropertyValueFactory<>("imagen"));

        TableColumn<Campeon, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Campeon, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        TableColumn<Campeon, String> colKey = new TableColumn<>("Key");
        colKey.setCellValueFactory(new PropertyValueFactory<>("key"));

        table.getColumns().addAll(colImagen, colNombre, colTitulo, colKey);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Cargando campeones..."));
        table.setFixedCellSize(60);

        // Panel de detalles
        detallesPanel.setPadding(new Insets(0, 15, 15, 15));
        detallesPanel.setStyle("-fx-background-color: #111111; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");

        lblNombreDetalles.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");
        lblTituloDetalles.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaa; -fx-padding: 0 0 10 0;");

        imgSplashDetalles.setFitWidth(500);
        imgSplashDetalles.setPreserveRatio(true);
        imgSplashDetalles.setSmooth(true);

        txtBiografia.setWrapText(true);
        txtBiografia.setEditable(false);
        txtBiografia.setPrefHeight(300);
        txtBiografia.setStyle("-fx-control-inner-background: #222222; -fx-text-fill: white; -fx-font-size: 14px;");

        scrollBio.setContent(txtBiografia);
        scrollBio.setFitToWidth(true);
        scrollBio.setStyle("-fx-background: transparent;");

        HBox botonesBox = new HBox(10);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.getChildren().addAll(btnEditarBio, btnCerrarDetalles);

        btnEditarBio.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        btnCerrarDetalles.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");

        Label lblSplash = new Label("Splash art");
        lblSplash.setStyle("-fx-font-size: 14px; -fx-text-fill: #bbb;");

        Label lblBio = new Label("Biografía");
        lblBio.setStyle("-fx-font-size: 14px; -fx-text-fill: #bbb;");

        detallesPanel.getChildren().addAll(
                lblNombreDetalles,
                lblTituloDetalles,
                lblSplash,
                imgSplashDetalles,
                lblBio,
                scrollBio,
                botonesBox
        );

        scrollDetalles.setContent(detallesPanel);
        scrollDetalles.setPadding(Insets.EMPTY);
        scrollDetalles.setFitToWidth(true);
        scrollDetalles.setFitToHeight(true);
        scrollDetalles.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        scrollDetalles.setPannable(true);
        scrollDetalles.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollDetalles.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        splitPane.getItems().addAll(table, scrollDetalles);
        root.setCenter(splitPane);
    }

    private void setupListeners() {
        // Filtro de búsqueda (cuando tengamos searchField, lo añadimos aquí o lo pasamos como parámetro)
        FilteredList<Campeon> filteredData = new FilteredList<>(campeonesList, p -> true);
        // searchField.textProperty().addListener(...) ← si lo tenemos, lo añadimos aquí

        SortedList<Campeon> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Selección de campeón → transforma panel derecho en "libro"
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newCampeon) -> {
            if (newCampeon != null) {
                campeonSeleccionado = newCampeon;

                // Actualizamos contenido dinámico (sin borrar botones)
                lblNombreDetalles.setText(newCampeon.getNombre());
                lblTituloDetalles.setText(newCampeon.getTitulo());

                String key = newCampeon.getKey();
                String rutaSplash = "file:///C:/Users/franz/Documents/LoreRuneTerra ASSETS/img/champion/splash/" + key + "_0.jpg";
                try {
                    String rutaLimpia = rutaSplash.replace("file:///", "");
                    File file = new File(rutaLimpia);
                    if (file.exists()) {
                        imgSplashDetalles.setImage(new Image(file.toURI().toString()));
                    } else {
                        imgSplashDetalles.setImage(null);
                    }
                } catch (Exception ignored) {
                    imgSplashDetalles.setImage(null);
                }

                imgSplashDetalles.setFitWidth(500);
                imgSplashDetalles.setPreserveRatio(true);
                imgSplashDetalles.setSmooth(true);

                String bio = championDAO.getBiografia(key);
                txtBiografia.setText(bio != null && !bio.trim().isEmpty() ? bio.replace("\n", "\n\n") : "No hay biografía guardada aún.");

                // Aseguramos botones visibles y con texto correcto
                btnEditarBio.setVisible(true);
                btnEditarBio.setText("Editar biografía");
                btnCerrarDetalles.setText("Cerrar libro");

                detallesPanel.setVisible(true);
                detallesPanel.setManaged(true);
            } else {
                detallesPanel.setVisible(false);
                detallesPanel.setManaged(false);
            }
        });

        // Botones (usamos BiographyEditorDialog)
        btnEditarBio.setOnAction(e -> {
            if (campeonSeleccionado != null) {
                BiographyEditorDialog editor = new BiographyEditorDialog(championDAO);
                editor.show(campeonSeleccionado, txtBiografia);
            }
        });

        btnCerrarDetalles.setOnAction(e -> ocultarDetalles());
    }

    public BorderPane getRoot() {
        return root;
    }

    private void ocultarDetalles() {
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);
        campeonSeleccionado = null;
    }
}