package com.loreruneterra.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;

import javafx.util.Duration;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import com.loreruneterra.view.BiographyEditorDialog;

import javafx.animation.RotateTransition;
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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class MainController {

    private final ChampionDAO championDAO;
    private final ObservableList<Campeon> campeonesList;

    // Nodos principales
    private final BorderPane root = new BorderPane();
    private final SplitPane splitPane = new SplitPane();
    private final TableView<Campeon> table = new TableView<>();
    private final StackPane bookContainer = new StackPane();
    private final VBox leftPage = new VBox(10);
    private final VBox rightPage = new VBox(20);
    private final Label lblNombreDetalles = new Label();
    private final Label lblTituloDetalles = new Label();
    private final ImageView imgSplashDetalles = new ImageView();
    private final TextArea txtBiografia = new TextArea("Selecciona un campeón para ver su biografía.");
    private final Button btnEditarBio = new Button("Editar biografía");
    private final Button btnCerrarDetalles = new Button("Cerrar libro");
    private final ScrollPane scrollBio = new ScrollPane();
    private final HBox botonesBox = new HBox(15);
    private final Label lblSplash = new Label("Ilustración");
    private final Label lblBio = new Label("Texto del Tomo");
    private final TextField searchField = new TextField();
    private final ScrollPane scrollDetalles = new ScrollPane();
    private boolean bioCompletaVisible = false;
    private final Button btnLeerCompleta = new Button("Leer Biografía completa");

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

        table.getColumns().addAll(colImagen, colNombre, colTitulo);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Cargando campeones..."));
        table.setFixedCellSize(60);

        // === LIBRO ABIERTO CON EFECTO 3D ===
        bookContainer.setStyle("-fx-background-color: #0a0a0a;");

        // Página izquierda (decorativa)
        leftPage.setStyle("""
        -fx-background-color: #1c1810;
        -fx-background-radius: 8 0 0 8;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 30, 0.6, -15, 0);
    """);
        leftPage.setPrefWidth(320);
        leftPage.setAlignment(Pos.CENTER);
        Label leftText = new Label("Crónicas de Runeterra");
        leftText.setStyle("-fx-font-size: 20px; -fx-text-fill: #8b7355; -fx-font-style: italic;");
        leftPage.getChildren().add(leftText);

        // Página derecha (contenido principal)
        rightPage.setStyle("""
        -fx-background-color: #1c1810;
        -fx-background-radius: 0 8 8 0;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 30, 0.6, 15, 0);
    """);
        rightPage.setPadding(new Insets(40, 50, 50, 50));

        lblNombreDetalles.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e8d5a3; -fx-font-family: 'Cinzel' or serif;");
        lblTituloDetalles.setStyle("-fx-font-size: 18px; -fx-text-fill: #b89e6e; -fx-font-style: italic;");

        imgSplashDetalles.setFitWidth(480);
        imgSplashDetalles.setPreserveRatio(true);

        txtBiografia.setWrapText(true);
        txtBiografia.setEditable(false);
        txtBiografia.setPrefHeight(600);
        txtBiografia.setPrefWidth(620);
        txtBiografia.setStyle("""
        -fx-control-inner-background: #1c1810;
        -fx-background-color: #1c1810;
        -fx-text-fill: #e8d5a3;
        -fx-font-size: 18px;
        -fx-font-family: serif;
        -fx-padding: 35;
        -fx-line-spacing: 8;
    """);

        scrollBio.setContent(txtBiografia);
        scrollBio.setFitToWidth(true);
        scrollBio.setFitToHeight(true);
        scrollBio.setStyle("-fx-background: #1c1810;");
        scrollBio.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollBio.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Botones abajo (solo una vez)
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.getChildren().addAll(btnEditarBio, btnLeerCompleta, btnCerrarDetalles);

        btnEditarBio.setStyle("-fx-background-color: #4a3c2a; -fx-text-fill: #e8d5a3; -fx-font-size: 14px; -fx-font-family: serif;");
        btnCerrarDetalles.setStyle("-fx-background-color: #8b2a2a; -fx-text-fill: #e8d5a3; -fx-font-size: 14px; -fx-font-family: serif;");
        btnLeerCompleta.setStyle("-fx-background-color: #3a5f8d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: serif;");
        btnLeerCompleta.setVisible(false); // se muestra solo si hay bio larga

        // Añade TODO al panel derecho (incluyendo botones al final)
        rightPage.getChildren().addAll(
                lblNombreDetalles,
                lblTituloDetalles,
                imgSplashDetalles,
                new Label("Biografía:"),
                scrollBio,
                botonesBox
        );

        bookContainer.getChildren().addAll(leftPage, rightPage);
        StackPane.setAlignment(leftPage, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightPage, Pos.CENTER_RIGHT);

        scrollDetalles.setContent(bookContainer);
        scrollDetalles.setStyle("-fx-background: transparent;");

        splitPane.getItems().addAll(table, scrollDetalles);
        root.setCenter(splitPane);
    }

    private void setupListeners() {
        // Filtro de búsqueda
        FilteredList<Campeon> filteredData = new FilteredList<>(campeonesList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(campeon -> {
                if (newValue == null || newValue.trim().isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return campeon.getNombre().toLowerCase().contains(lowerCaseFilter);
            });

            // Forzar actualización del libro si hay selección después del filtro
            Campeon selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Simulamos un "cambio" para refrescar
                table.getSelectionModel().clearSelection();
                table.getSelectionModel().select(selected);
            }
        });

        SortedList<Campeon> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);


        // Selección de campeón → transición suave de página (fade + slide recuperada)
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newCampeon) -> {
            if (newCampeon != null) {
                campeonSeleccionado = newCampeon;

                // Fade-out + slide-out de la página actual
                FadeTransition fadeOut = new FadeTransition(Duration.millis(250), rightPage);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), rightPage);
                slideOut.setFromX(0);
                slideOut.setToX(50);

                ParallelTransition out = new ParallelTransition(fadeOut, slideOut);
                out.setOnFinished(event -> {
                    // Actualizamos contenido DESPUÉS del fade-out
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

                    // ← AQUÍ VA EL NUEVO CÓDIGO DE BIO CORTA/COMPLETA
                    String bioCorta = championDAO.getBiografiaCorta(key);
                    String bioCompleta = championDAO.getBiografiaCompleta(key);

                    txtBiografia.setText(bioCorta != null ? bioCorta : "No hay biografía corta guardada.");

                    btnLeerCompleta.setVisible(bioCompleta != null && !bioCompleta.equals(bioCorta));
                    btnLeerCompleta.setText("Leer Biografía completa");
                    bioCompletaVisible = false;

                    // Toggle del botón
                    btnLeerCompleta.setOnAction(e -> {
                        if (bioCompletaVisible) {
                            txtBiografia.setText(bioCorta);
                            btnLeerCompleta.setText("Leer Biografía completa");
                            bioCompletaVisible = false;
                        } else {
                            txtBiografia.setText(bioCompleta);
                            btnLeerCompleta.setText("Ver resumen");
                            bioCompletaVisible = true;
                        }
                    });

                    btnEditarBio.setVisible(true);
                    btnEditarBio.setText("Editar biografía");
                    btnCerrarDetalles.setText("Cerrar libro");

                    // Fade-in + slide-in de la nueva página
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(350), rightPage);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), rightPage);
                    slideIn.setFromX(-50);
                    slideIn.setToX(0);

                    ParallelTransition in = new ParallelTransition(fadeIn, slideIn);
                    in.play();
                });

                out.play();

                bookContainer.setVisible(true);
            } else {
                bookContainer.setVisible(false);
            }
        });

        btnEditarBio.setOnAction(e -> {
            if (campeonSeleccionado != null) {
                BiographyEditorDialog editor = new BiographyEditorDialog(championDAO);
                editor.show(campeonSeleccionado, txtBiografia);
            } else {
                // Opcional: mostrar alerta si no hay campeón seleccionado
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Atención");
                alert.setHeaderText("No hay campeón seleccionado");
                alert.setContentText("Selecciona un campeón para editar su biografía.");
                alert.showAndWait();
            }
        });

        btnCerrarDetalles.setOnAction(e -> ocultarDetalles());


    }

    public BorderPane getRoot() {
        return root;
    }

    private void ocultarDetalles() {
        bookContainer.setVisible(false);
        campeonSeleccionado = null;
    }
}