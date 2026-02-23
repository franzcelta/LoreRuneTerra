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

    // Panel de detalles (inicialmente oculto)
    private final VBox detallesPanel = new VBox(10);
    private final Label lblNombreDetalles = new Label();
    private final Label lblTituloDetalles = new Label();
    private final ImageView imgPrincipalDetalles = new ImageView();
    private final ImageView imgSplashDetalles = new ImageView();
    private final TextArea txtBiografia = new TextArea("Biografía no disponible aún. Próximamente desde Universe.");
    private final Button btnCerrarDetalles = new Button("Cerrar detalles");

    @Override
    public void start(Stage primaryStage) {
        cargarCampeonesDesdeBD();

        BorderPane root = new BorderPane();

        // Panel superior: título + búsqueda
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));

        Label tituloApp = new Label("LoreRuneTerra - Campeones de Runeterra");
        tituloApp.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setMaxWidth(400);
        searchField.setStyle("-fx-font-size: 14px;");

        topBox.getChildren().addAll(tituloApp, searchField);
        root.setTop(topBox);

        // SplitPane para dividir tabla y detalles
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.6); // 60% tabla, 40% detalles

        // Tabla de campeones (izquierda)
        TableView<Campeon> table = new TableView<>();
        table.setItems(campeonesList);

        // Columnas (como antes)
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

        // Panel de detalles (derecha, inicialmente oculto)
        detallesPanel.setPadding(new Insets(20));
        detallesPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1;");

        lblNombreDetalles.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        lblTituloDetalles.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");

        imgPrincipalDetalles.setFitWidth(200);
        imgPrincipalDetalles.setPreserveRatio(true);

        imgSplashDetalles.setFitWidth(300);
        imgSplashDetalles.setPreserveRatio(true);

        txtBiografia.setWrapText(true);
        txtBiografia.setEditable(false);
        txtBiografia.setPrefHeight(200);

        btnCerrarDetalles.setOnAction(e -> ocultarDetalles());

        detallesPanel.getChildren().addAll(
                lblNombreDetalles,
                lblTituloDetalles,
                imgPrincipalDetalles,
                new Label("Splashart:"),
                imgSplashDetalles,
                new Label("Biografía:"),
                txtBiografia,
                btnCerrarDetalles
        );

        // Inicialmente ocultar panel de detalles
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);

        // Añadir al SplitPane
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

        // Evento al seleccionar fila: mostrar detalles
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarDetalles(newSelection);
            } else {
                ocultarDetalles();
            }
        });

        // Escena y stage
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("LoreRuneTerra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Mostrar panel de detalles con el campeón seleccionado
    private void mostrarDetalles(Campeon campeon) {
        lblNombreDetalles.setText(campeon.getNombre());
        lblTituloDetalles.setText(campeon.getTitulo());

        // Imagen principal (la pequeña/icon del campeón, más grande en detalles)
        String rutaImg = campeon.getImagen();
        if (rutaImg != null && !rutaImg.trim().isEmpty()) {
            try {
                String rutaLimpia = rutaImg.replace("file:///", "");
                File file = new File(rutaLimpia);
                if (file.exists()) {
                    imgPrincipalDetalles.setImage(new Image(file.toURI().toString()));
                }
            } catch (Exception e) {
                imgPrincipalDetalles.setImage(null);
            }
        }
        imgPrincipalDetalles.setFitWidth(250);
        imgPrincipalDetalles.setPreserveRatio(true);

        // Splashart grande (desde la carpeta splash)
        String key = campeon.getKey();
        String rutaSplash = "file:///C:/Users/franz/Documents/LoreRuneTerra ASSETS/img/champion/splash/" + key + "_0.jpg";

        try {
            File splashFile = new File(rutaSplash.replace("file:///", ""));
            if (splashFile.exists()) {
                imgSplashDetalles.setImage(new Image(rutaSplash));
                System.out.println("Splashart cargado: " + rutaSplash);
            } else {
                System.out.println("Splashart no encontrado: " + rutaSplash);
                imgSplashDetalles.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Error cargando splashart: " + rutaSplash);
            imgSplashDetalles.setImage(null);
        }

        imgSplashDetalles.setFitWidth(500);  // tamaño grande, ajustar mas tarde...
        imgSplashDetalles.setPreserveRatio(true);

        // Biografía (placeholder por ahora)
        txtBiografia.setText("Biografía completa de " + campeon.getNombre() + ".\n\nPróximamente desde la tabla biografias o Universe.");

        // Mostrar el panel
        detallesPanel.setVisible(true);
        detallesPanel.setManaged(true);
    }

    // Ocultar panel de detalles
    private void ocultarDetalles() {
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);
        // Podemos limpiar la selección luego...
        // table.getSelectionModel().clearSelection();
    }

    // Clase Campeon (sin cambios)
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

    // Cargar desde BD (sin cambios)
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

    public static void main(String[] args) {
        launch(args);
    }
}