package com.loreruneterra.controller;

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
import javafx.scene.shape.Rectangle;
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
    private final StackPane bookContainer = new StackPane();     // ← Añadido
    private final VBox leftPage = new VBox(10);                   // ← Añadido
    private final VBox rightPage = new VBox(20);                  // ← Añadido
    private final Rectangle bookSpine = new Rectangle(30, 600);   // ← Añadido (lomo)
    private final VBox detallesPanel = new VBox(10);              // ← en caso de usarse
    private final Label lblNombreDetalles = new Label();
    private final Label lblTituloDetalles = new Label();
    private final ImageView imgSplashDetalles = new ImageView();
    private final TextArea txtBiografia = new TextArea("Selecciona un campeón para ver su biografía.");
    private final Button btnEditarBio = new Button("Editar biografía");
    private final Button btnCerrarDetalles = new Button("Cerrar libro");
    private final ScrollPane scrollBio = new ScrollPane();
    private final ScrollPane scrollDetalles = new ScrollPane();   // ← Añadido
    private final HBox botonesBox = new HBox(15);
    private final Label lblSplash = new Label("Ilustración");
    private final Label lblBio = new Label("Texto del Tomo");
    private final TextField searchField = new TextField();

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

        TableColumn<Campeon, String> colKey = new TableColumn<>("Key");
        colKey.setCellValueFactory(new PropertyValueFactory<>("key"));

        table.getColumns().addAll(colImagen, colNombre, colTitulo, colKey);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Cargando campeones..."));
        table.setFixedCellSize(60);

        // === LIBRO LITERAL ABIERTO CON ANIMACIÓN ===
        bookContainer.setStyle("-fx-background-color: #0a0a0a;"); // mesa oscura

        // Página izquierda (decorativa)
        leftPage.setStyle("""
            -fx-background-color: #1c1810;
            -fx-background-radius: 8 0 0 8;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 30, 0.6, -10, 0);
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
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 30, 0.6, 10, 0);
        """);
        rightPage.setPadding(new Insets(40, 50, 50, 50));

        lblNombreDetalles.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #e8d5a3; -fx-font-family: 'Cinzel' or serif;");
        lblTituloDetalles.setStyle("-fx-font-size: 18px; -fx-text-fill: #b89e6e; -fx-font-style: italic;");

        imgSplashDetalles.setFitWidth(480);
        imgSplashDetalles.setPreserveRatio(true);

        txtBiografia.setStyle("""
            -fx-control-inner-background: #1c1810;
            -fx-text-fill: #d4c3a2;
            -fx-font-size: 15px;
            -fx-font-family: serif;
            -fx-padding: 25;
        """);

        scrollBio.setContent(txtBiografia);
        scrollBio.setFitToWidth(true);
        scrollBio.setStyle("-fx-background: transparent;");

        HBox botonesBox = new HBox(15);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.getChildren().addAll(btnEditarBio, btnCerrarDetalles);

        btnEditarBio.setStyle("-fx-background-color: #4a3c2a; -fx-text-fill: #e8d5a3;");
        btnCerrarDetalles.setStyle("-fx-background-color: #8b2a2a; -fx-text-fill: #e8d5a3;");

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
        });

        SortedList<Campeon> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Selección de campeón → transforma panel derecho en "libro" + animación PROTOTIPO
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newCampeon) -> {
            if (newCampeon != null) {
                campeonSeleccionado = newCampeon;

                // Animación de pasar página (giro desde el centro)
                RotateTransition pageFlip = new RotateTransition(Duration.millis(700), rightPage);
                pageFlip.setAxis(Rotate.Y_AXIS);
                pageFlip.setFromAngle(0);
                pageFlip.setToAngle(90); // Giro completo como pasar hoja
                pageFlip.setAutoReverse(true);
                pageFlip.setCycleCount(2);
                pageFlip.setOnFinished(event -> {
                    // Actualizamos contenido DESPUÉS de la animación
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
                    } catch (Exception ignored) {}

                    String bio = championDAO.getBiografia(key);
                    txtBiografia.setText(bio != null && !bio.trim().isEmpty() ? bio.replace("\n", "\n\n") : "No hay biografía guardada aún.");

                    btnEditarBio.setVisible(true);
                    btnCerrarDetalles.setText("Cerrar libro");

                    // Volvemos la página a 0 grados
                    rightPage.setRotate(0);
                });
                pageFlip.play();

                bookContainer.setVisible(true);
            } else {
                bookContainer.setVisible(false);
            }
        });

        // Botones
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
        bookContainer.setVisible(false);
        campeonSeleccionado = null;
    }
}