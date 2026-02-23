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
    private final VBox detallesPanel = new VBox(10);
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

        // Panel superior compacto
        VBox topBox = new VBox(5);
        topBox.setPadding(new Insets(8));
        topBox.setStyle("-fx-background-color: #0f0f0f;");

        Label tituloApp = new Label("LoreRuneTerra - Campeones de Runeterra");
        tituloApp.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;"); // Dorado

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setMaxWidth(350);
        searchField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: gray;");

        topBox.getChildren().addAll(tituloApp, searchField);
        root.setTop(topBox);

        // SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.65);
        splitPane.setStyle("-fx-background-color: transparent;");

        // Tabla izquierda
        TableView<Campeon> table = new TableView<>();
        table.setItems(campeonesList);
        table.setStyle("-fx-background-color: #0d1624;"); // Azul marino

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
        detallesPanel.setPadding(new Insets(15));
        detallesPanel.setStyle("-fx-background-color: #111111; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");

        lblNombreDetalles.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");
        lblTituloDetalles.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaa; -fx-padding: 0 0 10 0;");

        imgPrincipalDetalles.setFitWidth(200);
        imgPrincipalDetalles.setPreserveRatio(true);

        imgSplashDetalles.setFitWidth(400);
        imgSplashDetalles.setPreserveRatio(true);
        imgSplashDetalles.setSmooth(true);

        txtBiografia.setWrapText(true);
        txtBiografia.setEditable(false);
        txtBiografia.setPrefHeight(200);
        txtBiografia.setStyle("-fx-control-inner-background: #222222; -fx-text-fill: white; -fx-font-size: 14px;");

        ScrollPane scrollBio = new ScrollPane(txtBiografia);
        scrollBio.setFitToWidth(true);
        scrollBio.setStyle("-fx-background: transparent;");

        HBox botonesBox = new HBox(10);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);
        botonesBox.getChildren().addAll(btnEditarBio, btnGuardarBio, btnCerrarDetalles);

        btnEditarBio.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        btnGuardarBio.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        btnCerrarDetalles.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");

        btnGuardarBio.setVisible(false);

        Label lblImagen = new Label("Imagen principal");
        lblImagen.setStyle("-fx-font-size: 14px; -fx-text-fill: #bbb;");

        Label lblSplash = new Label("Splash art");
        lblSplash.setStyle("-fx-font-size: 14px; -fx-text-fill: #bbb;");

        Label lblBio = new Label("Biografía");
        lblBio.setStyle("-fx-font-size: 14px; -fx-text-fill: #bbb;");

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

        ScrollPane scrollDetalles = new ScrollPane(detallesPanel);
        scrollDetalles.setFitToWidth(true);
        scrollDetalles.setStyle("-fx-background: transparent;");

        splitPane.getItems().addAll(table, scrollDetalles);

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
            if (campeonSeleccionado != null) {
                abrirEditorLore(campeonSeleccionado);
            }
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

        // Cargar CSS (asegúrate de que la ruta sea correcta)
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Aplicar clases CSS (para que se vean los estilos)
        tituloApp.getStyleClass().add("title-label");
        lblNombreDetalles.getStyleClass().add("title-label");
        lblTituloDetalles.getStyleClass().add("subtitle-label");
        txtBiografia.getStyleClass().add("biography-text-area");
        btnEditarBio.getStyleClass().addAll("button", "edit-button");
        btnGuardarBio.getStyleClass().addAll("button", "save-button");
        btnCerrarDetalles.getStyleClass().addAll("button", "close-button");
        imgPrincipalDetalles.getStyleClass().add("image-view");
        imgSplashDetalles.getStyleClass().add("image-view");
        scrollBio.getStyleClass().add("scroll-pane");
        scrollDetalles.getStyleClass().add("scroll-pane");
        table.getStyleClass().add("table-view");
        detallesPanel.getStyleClass().add("detalles-panel");

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

        imgSplashDetalles.setFitWidth(400);
        imgSplashDetalles.setPreserveRatio(true);
        imgSplashDetalles.setSmooth(true);

        // Biografía REAL desde BD
        String bio = cargarBiografia(key);
        if (bio != null && !bio.trim().isEmpty()) {
            txtBiografia.setText(bio.replace("\n", "\n\n"));
        } else {
            txtBiografia.setText("No hay biografía guardada aún para " + campeon.getNombre() + ".\nUsa 'Editar biografía' para agregar desde Universe.");
        }

        detallesPanel.setVisible(true);
        detallesPanel.setManaged(true);
    }

    private String cargarBiografia(String keyCampeon) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = """
                SELECT biografia_completa
                FROM biografias b
                JOIN campeones c ON b.campeon_id = c.id
                WHERE c.key = ?
                ORDER BY ultima_actualizacion DESC
                LIMIT 1
                """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, keyCampeon);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("biografia_completa");
            }
        } catch (SQLException e) {
            System.err.println("Error cargando biografía: " + e.getMessage());
        }
        return null;
    }

    private void ocultarDetalles() {
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);
        campeonSeleccionado = null;
    }

    private void abrirEditorLore(Campeon campeon) {
        if (campeon == null) return;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Editar Biografía - " + campeon.getNombre());
        dialog.setHeaderText("Pega la biografía completa y la historia corta desde Universe");

        TextArea textArea = new TextArea(txtBiografia.getText());
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(600);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return textArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevoTexto -> {
            if (nuevoTexto != null && !nuevoTexto.trim().isEmpty()) {
                guardarBiografia(campeon.getKey(), nuevoTexto);
                mostrarDetalles(campeon);
            }
        });
    }

    private void guardarBiografia(String keyCampeon, String texto) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement psId = conn.prepareStatement("SELECT id FROM campeones WHERE key = ?");
            psId.setString(1, keyCampeon);
            ResultSet rs = psId.executeQuery();
            if (!rs.next()) {
                System.err.println("Campeón no encontrado: " + keyCampeon);
                return;
            }
            int campeonId = rs.getInt("id");

            PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO biografias (campeon_id, biografia_completa, ultima_actualizacion)
                VALUES (?, ?, CURRENT_DATE)
                ON CONFLICT (campeon_id) DO UPDATE SET
                    biografia_completa = EXCLUDED.biografia_completa,
                    ultima_actualizacion = CURRENT_DATE
                """);
            ps.setInt(1, campeonId);
            ps.setString(2, texto);
            ps.executeUpdate();

            System.out.println("Biografía guardada para " + keyCampeon);
            txtBiografia.setText(texto);
        } catch (SQLException e) {
            System.err.println("Error guardando biografía: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo guardar la biografía");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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