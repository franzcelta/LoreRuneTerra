package com.loreruneterra;

import com.loreruneterra.db.DatabaseConnector;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private final ObservableList<Campeon> campeonesList = FXCollections.observableArrayList();

    // Panel de detalles
    private final VBox detallesPanel = new VBox(15);
    private final Label lblNombreDetalles = new Label();
    private final Label lblTituloDetalles = new Label();
    private final ImageView imgPrincipalDetalles = new ImageView();
    private final ImageView imgSplashDetalles = new ImageView();
    private final TextArea txtBiografia = new TextArea("Selecciona un campeón para ver su biografía.");
    private final Button btnEditarBio = new Button("Editar biografía");
    private final Button btnGuardarBio = new Button("Guardar cambios");
    private final Button btnCerrarDetalles = new Button("Cerrar detalles");

    private Campeon campeonSeleccionado = null;

    @Override
    public void start(Stage primaryStage) {
        cargarCampeonesDesdeBD();

        BorderPane root = new BorderPane();

        // Panel superior
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));
        topBox.setStyle("-fx-background-color: #0f0f0f;");

        Label tituloApp = new Label("LoreRuneTerra - Campeones de Runeterra");
        tituloApp.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setMaxWidth(400);
        searchField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: gray;");

        topBox.getChildren().addAll(tituloApp, searchField);
        root.setTop(topBox);

        // SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.65);

        // Tabla izquierda
        TableView<Campeon> table = new TableView<>();
        table.setItems(campeonesList);
        table.setStyle("-fx-background-color: #1e1e1e;");

        // Columnas
        TableColumn<Campeon, String> colImagen = new TableColumn<>("Imagen");
        colImagen.setPrefWidth(80);
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

                    imageView.setFitWidth(60);
                    imageView.setFitHeight(60);
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

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Cargando campeones..."));
        table.setFixedCellSize(70);

        // Panel de detalles
        detallesPanel.setPadding(new Insets(20));
        detallesPanel.setStyle("-fx-background-color: #111111; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);

        lblNombreDetalles.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblTituloDetalles.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa; -fx-padding: 0 0 15 0;");

        imgPrincipalDetalles.setFitWidth(250);
        imgPrincipalDetalles.setPreserveRatio(true);

        imgSplashDetalles.setFitWidth(550);
        imgSplashDetalles.setPreserveRatio(true);

        txtBiografia.setWrapText(true);
        txtBiografia.setEditable(false);
        txtBiografia.setPrefHeight(300);
        txtBiografia.setStyle("-fx-control-inner-background: #222222; -fx-text-fill: white; -fx-font-size: 14px;");

        ScrollPane scrollBio = new ScrollPane(txtBiografia);
        scrollBio.setFitToWidth(true);
        scrollBio.setStyle("-fx-background: transparent;");

        HBox botonesBox = new HBox(15);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.getChildren().addAll(btnEditarBio, btnGuardarBio, btnCerrarDetalles);

        btnEditarBio.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        btnGuardarBio.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        btnCerrarDetalles.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");

        btnGuardarBio.setVisible(false);

        Label lblImagen = new Label("Imagen principal");
        lblImagen.setStyle("-fx-font-size: 16px; -fx-text-fill: #bbb;");

        Label lblSplash = new Label("Splash art");
        lblSplash.setStyle("-fx-font-size: 16px; -fx-text-fill: #bbb;");

        Label lblBio = new Label("Biografía");
        lblBio.setStyle("-fx-font-size: 16px; -fx-text-fill: #bbb;");

        detallesPanel.getChildren().addAll(
                lblNombreDetalles,
                lblTituloDetalles,
                lblImagen,
                imgPrincipalDetalles,
                lblSplash,
                imgSplashDetalles,
                lblBio,
                scrollBio,
                botonesBox
        );

        splitPane.getItems().addAll(table, detallesPanel);

        root.setCenter(splitPane);

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

        // Selección de campeón
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newCampeon) -> {
            if (newCampeon != null) {
                campeonSeleccionado = newCampeon;
                mostrarDetalles(newCampeon);
                txtBiografia.setEditable(false);
                btnGuardarBio.setVisible(false);
                btnEditarBio.setVisible(true);
            } else {
                ocultarDetalles();
            }
        });

        // Botones
        btnEditarBio.setOnAction(e -> {
            txtBiografia.setEditable(true);
            btnEditarBio.setVisible(false);
            btnGuardarBio.setVisible(true);
        });

        btnGuardarBio.setOnAction(e -> {
            if (campeonSeleccionado != null) {
                guardarBiografia(campeonSeleccionado.getKey(), txtBiografia.getText());
                txtBiografia.setEditable(false);
                btnGuardarBio.setVisible(false);
                btnEditarBio.setVisible(true);
            }
        });

        btnCerrarDetalles.setOnAction(e -> ocultarDetalles());

        Scene scene = new Scene(root, 1300, 800);
        scene.setFill(javafx.scene.paint.Color.BLACK);
        primaryStage.setTitle("LoreRuneTerra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void mostrarDetalles(Campeon campeon) {
        lblNombreDetalles.setText(campeon.getNombre());
        lblTituloDetalles.setText(campeon.getTitulo());

        // Imagen principal
        String rutaImg = campeon.getImagen();
        if (rutaImg != null && !rutaImg.trim().isEmpty()) {
            try {
                String rutaLimpia = rutaImg.replace("file:///", "");
                File file = new File(rutaLimpia);
                if (file.exists()) {
                    imgPrincipalDetalles.setImage(new Image(file.toURI().toString()));
                }
            } catch (Exception ignored) {
                imgPrincipalDetalles.setImage(null);
            }
        }

        // Splash art
        String key = campeon.getKey();
        String rutaSplash = "file:///C:/Users/franz/Documents/LoreRuneTerra ASSETS/img/champion/splash/" + key + "_0.jpg";
        try {
            String rutaLimpia = rutaSplash.replace("file:///", "");
            File splashFile = new File(rutaLimpia);
            if (splashFile.exists()) {
                imgSplashDetalles.setImage(new Image(splashFile.toURI().toString()));
            } else {
                imgSplashDetalles.setImage(null);
            }
        } catch (Exception ignored) {
            imgSplashDetalles.setImage(null);
        }

        // Biografía placeholder
        txtBiografia.setText("Biografía completa de " + campeon.getNombre() + ".\n\nPróximamente desde Universe o tabla biografias.");

        detallesPanel.setVisible(true);
        detallesPanel.setManaged(true);
    }

    private void ocultarDetalles() {
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);
        campeonSeleccionado = null;
    }

    // Métodos de biografía (placeholders por ahora)
    private void cargarBiografia(String keyCampeon) {
        // Implementar cuando agreguemos la carga real desde BD
    }

    private void guardarBiografia(String keyCampeon, String textoBio) {
        // Implementar cuando agreguemos la BD real
    }

    private void cargarCampeonesDesdeBD() {
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

            campeonesList.setAll(lista);
            System.out.println("Campeones cargados desde BD: " + lista.size());

        } catch (SQLException e) {
            System.err.println("Error al cargar campeones: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public static class Campeon {
        private final StringProperty key = new SimpleStringProperty();
        private final StringProperty nombre = new SimpleStringProperty();
        private final StringProperty titulo = new SimpleStringProperty();
        private final StringProperty imagen = new SimpleStringProperty();

        public Campeon(String key, String nombre, String titulo, String imagen) {
            this.key.set(key);
            this.nombre.set(nombre);
            this.titulo.set(titulo);
            this.imagen.set(imagen);
        }

        public String getKey() { return key.get(); }
        public StringProperty keyProperty() { return key; }

        public String getNombre() { return nombre.get(); }
        public StringProperty nombreProperty() { return nombre; }

        public String getTitulo() { return titulo.get(); }
        public StringProperty tituloProperty() { return titulo; }

        public String getImagen() { return imagen.get(); }
        public StringProperty imagenProperty() { return imagen; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}