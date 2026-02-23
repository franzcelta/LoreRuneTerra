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

    // Lista observable que uso para cargar los campeones desde la BD
    private final ObservableList<Campeon> campeonesList = FXCollections.observableArrayList();

    // Panel derecho donde muestro los detalles del campeón seleccionado
    private final VBox detallesPanel = new VBox(15);
    private final Label lblNombreDetalles = new Label();
    private final Label lblTituloDetalles = new Label();
    private final ImageView imgPrincipalDetalles = new ImageView();
    private final ImageView imgSplashDetalles = new ImageView();
    private final TextArea txtBiografia = new TextArea("Selecciona un campeón para ver su biografía.");
    private final Button btnEditarBio = new Button("Editar biografía");
    private final Button btnCerrarDetalles = new Button("Cerrar detalles");

    // Guardo referencia al campeón seleccionado actualmente
    private Campeon campeonSeleccionado = null;

    @Override
    public void start(Stage primaryStage) {

        // Cargo los campeones al iniciar la app
        cargarCampeonesDesdeBD();

        BorderPane root = new BorderPane();

        // ====== PANEL SUPERIOR (título + búsqueda) ======
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(10));

        Label tituloApp = new Label("LoreRuneTerra - Campeones de Runeterra");
        tituloApp.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setMaxWidth(400);

        topBox.getChildren().addAll(tituloApp, searchField);
        root.setTop(topBox);

        // ====== SPLITPANE (tabla izquierda, detalles derecha) ======
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.65);

        TableView<Campeon> table = new TableView<>();
        table.setItems(campeonesList);

        // Columna imagen (miniatura)
        TableColumn<Campeon, String> colImagen = new TableColumn<>("Imagen");
        colImagen.setPrefWidth(80);

        // Renderizo manualmente la imagen en la celda
        colImagen.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);

                if (empty || url == null || url.isBlank()) {
                    setGraphic(null);
                    return;
                }

                try {
                    String ruta = url.replace("file:///", "");
                    File file = new File(ruta);

                    if (file.exists()) {
                        imageView.setImage(new Image(file.toURI().toString()));
                    } else {
                        imageView.setImage(null);
                    }

                    imageView.setFitWidth(60);
                    imageView.setFitHeight(60);
                    imageView.setPreserveRatio(true);

                } catch (Exception e) {
                    imageView.setImage(null);
                }

                setGraphic(imageView);
            }
        });
        colImagen.setCellValueFactory(new PropertyValueFactory<>("imagen"));

        // Columnas normales
        TableColumn<Campeon, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Campeon, String> colTitulo = new TableColumn<>("Título");
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));

        TableColumn<Campeon, String> colKey = new TableColumn<>("Key");
        colKey.setCellValueFactory(new PropertyValueFactory<>("key"));

        table.getColumns().addAll(colImagen, colNombre, colTitulo, colKey);
        table.setFixedCellSize(70);

        // ====== PANEL DE DETALLES ======
        detallesPanel.setPadding(new Insets(20));

        lblNombreDetalles.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        lblTituloDetalles.setStyle("-fx-font-size: 18px;");

        imgPrincipalDetalles.setFitWidth(250);
        imgPrincipalDetalles.setPreserveRatio(true);

        imgSplashDetalles.setFitWidth(400);
        imgSplashDetalles.setPreserveRatio(true);

        txtBiografia.setWrapText(true);
        txtBiografia.setEditable(false);

        ScrollPane scrollBio = new ScrollPane(txtBiografia);
        scrollBio.setFitToWidth(true);

        HBox botones = new HBox(15, btnEditarBio, btnCerrarDetalles);
        botones.setAlignment(Pos.CENTER_RIGHT);

        detallesPanel.getChildren().addAll(
                lblNombreDetalles,
                lblTituloDetalles,
                imgPrincipalDetalles,
                imgSplashDetalles,
                scrollBio,
                botones
        );

        ScrollPane scrollDetalles = new ScrollPane(detallesPanel);
        scrollDetalles.setFitToWidth(true);

        splitPane.getItems().addAll(table, scrollDetalles);
        root.setCenter(splitPane);

        // ====== FILTRO DE BÚSQUEDA ======
        // Uso FilteredList para que la tabla se actualice dinámicamente
        FilteredList<Campeon> filteredData = new FilteredList<>(campeonesList, p -> true);

        searchField.textProperty().addListener((obs, old, val) -> {
            filteredData.setPredicate(c -> {
                if (val == null || val.isBlank()) return true;
                return c.getNombre().toLowerCase().contains(val.toLowerCase());
            });
        });

        SortedList<Campeon> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Cuando selecciono un campeón en la tabla, muestro sus detalles
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                campeonSeleccionado = nuevo;
                mostrarDetalles(nuevo);
            } else {
                ocultarDetalles();
            }
        });

        // Botón para editar biografía (abre diálogo)
        btnEditarBio.setOnAction(e -> {
            if (campeonSeleccionado != null) {
                abrirEditorLore(campeonSeleccionado);
            }
        });

        btnCerrarDetalles.setOnAction(e -> ocultarDetalles());

        Scene scene = new Scene(root, 1300, 800);
        primaryStage.setTitle("LoreRuneTerra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Muestro en el panel derecho los datos del campeón seleccionado
    private void mostrarDetalles(Campeon campeon) {

        lblNombreDetalles.setText(campeon.getNombre());
        lblTituloDetalles.setText(campeon.getTitulo());

        String key = campeon.getKey();

        // Cargo la biografía más reciente desde la BD
        String bio = cargarBiografia(key);
        if (bio != null && !bio.isBlank()) {
            txtBiografia.setText(bio);
        } else {
            txtBiografia.setText("No hay biografía guardada aún para "
                    + campeon.getNombre());
        }

        // Cargo imagen principal
        try {
            if (campeon.getImagen() != null) {
                File file = new File(campeon.getImagen().replace("file:///", ""));
                if (file.exists()) {
                    imgPrincipalDetalles.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception ignored) {}

        // Cargo splash art desde ruta local
        try {
            File splash = new File(
                    "C:/Users/franz/Documents/LoreRuneTerra ASSETS/img/champion/splash/"
                            + key + "_0.jpg");

            if (splash.exists()) {
                imgSplashDetalles.setImage(new Image(splash.toURI().toString()));
            } else {
                imgSplashDetalles.setImage(null);
            }

        } catch (Exception ignored) {}
    }

    // Obtengo la biografía más reciente del campeón desde la BD
    private String cargarBiografia(String keyCampeon) {
        try (Connection conn = DatabaseConnector.getConnection()) {

            String sql = """
                    SELECT biografia_completa
                    FROM biografias b
                    JOIN campeones c ON b.campeon_id = c.id
                    WHERE c.key = ?
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

    // Oculto el panel de detalles cuando no hay selección
    private void ocultarDetalles() {
        detallesPanel.setVisible(false);
        detallesPanel.setManaged(false);
        campeonSeleccionado = null;
    }

    // Abro un diálogo simple para pegar/editar la biografía manualmente
    private void abrirEditorLore(Campeon campeon) {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Editar Biografía - " + campeon.getNombre());

        TextArea textArea = new TextArea(txtBiografia.getText());
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> btn == ButtonType.OK ? textArea.getText() : null);

        dialog.showAndWait().ifPresent(texto -> {
            if (texto != null && !texto.isBlank()) {
                guardarBiografia(campeon.getKey(), texto);
                mostrarDetalles(campeon);
            }
        });
    }

    // Inserto o actualizo la biografía en la BD
    private void guardarBiografia(String keyCampeon, String texto) {
        try (Connection conn = DatabaseConnector.getConnection()) {

            PreparedStatement psId =
                    conn.prepareStatement("SELECT id FROM campeones WHERE key = ?");
            psId.setString(1, keyCampeon);
            ResultSet rs = psId.executeQuery();

            if (!rs.next()) return;

            int id = rs.getInt("id");

            PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO biografias (campeon_id, biografia_completa)
                    VALUES (?, ?)
                    ON CONFLICT (campeon_id) DO UPDATE
                    SET biografia_completa = EXCLUDED.biografia_completa
                    """);

            ps.setInt(1, id);
            ps.setString(2, texto);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Cargo todos los campeones ordenados alfabéticamente
    private void cargarCampeonesDesdeBD() {

        try (Connection conn = DatabaseConnector.getConnection()) {

            String sql = "SELECT key, nombre, titulo, imagen FROM campeones ORDER BY nombre ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                campeonesList.add(new Campeon(
                        rs.getString("key"),
                        rs.getString("nombre"),
                        rs.getString("titulo"),
                        rs.getString("imagen")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error cargando campeones: " + e.getMessage());
        }
    }

    // Clase modelo simple para representar un campeón
    public static class Campeon {

        private final StringProperty key;
        private final StringProperty nombre;
        private final StringProperty titulo;
        private final StringProperty imagen;

        public Campeon(String key, String nombre, String titulo, String imagen) {
            this.key = new SimpleStringProperty(key);
            this.nombre = new SimpleStringProperty(nombre);
            this.titulo = new SimpleStringProperty(titulo);
            this.imagen = new SimpleStringProperty(imagen);
        }

        public String getKey() { return key.get(); }
        public String getNombre() { return nombre.get(); }
        public String getTitulo() { return titulo.get(); }
        public String getImagen() { return imagen.get(); }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

//comentado