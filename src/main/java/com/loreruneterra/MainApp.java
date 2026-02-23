package com.loreruneterra;

import com.loreruneterra.db.DatabaseConnector;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private ObservableList<Campeon> campeonesList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        // Cargar campeones desde BD
        cargarCampeonesDesdeBD();

        // Layout principal
        BorderPane root = new BorderPane();

        // Tabla de campeones
        TableView<Campeon> table = new TableView<>();
        table.setItems(campeonesList);

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
                    // Limpia prefijo file:/// si existe
                    String rutaLimpia = url.replace("file:///", "");
                    File file = new File(rutaLimpia);

                    if (file.exists() && file.canRead()) {
                        // Carga desde URI (la forma más fiable para locales en Windows)
                        imageView.setImage(new Image(file.toURI().toString()));
                        System.out.println("Imagen cargada OK: " + rutaLimpia);
                    } else {
                        System.out.println("Archivo NO encontrado o no legible: " + rutaLimpia);
                        imageView.setImage(null);
                    }

                    imageView.setFitWidth(60);
                    imageView.setFitHeight(60);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    // e.printStackTrace();  // comentado - warning de IntelliJ
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

        // Agregar columnas (imagen primero para que destaque)
        table.getColumns().addAll(colImagen, colNombre, colTitulo, colKey);

        // Ajustes de la tabla
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Cargando campeones... o no hay datos aún"));
        table.setFixedCellSize(70);  // Altura fija de fila para que quepan bien las imágenes

        root.setCenter(table);

        // Barra superior
        Label tituloApp = new Label("LoreRuneTerra - Campeones de Runeterra");
        tituloApp.setStyle("-fx-font-size: 20px; -fx-padding: 10;");
        root.setTop(tituloApp);

        // Escena y stage
        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setTitle("LoreRuneTerra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Clase interna Campeon (modelo)
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

    // Cargar desde BD
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
            System.err.println("Error: " + e.getMessage());
            // e.printStackTrace();  // comentado - warning de IntelliJ
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}