package com.loreruneterra.controller;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import com.loreruneterra.view.ChampionBookView;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;

public class MainController {

    private final ChampionDAO championDAO;
    private final ObservableList<Campeon> campeonesList;
    private final BorderPane root = new BorderPane();

    private Stage primaryStage;

    public MainController(ChampionDAO championDAO, ObservableList<Campeon> campeonesList) {
        this.championDAO = championDAO;
        this.campeonesList = campeonesList;
        showWelcomeScreen();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void showWelcomeScreen() {
        VBox welcome = new VBox(60);
        welcome.setAlignment(Pos.CENTER);
        welcome.setStyle("-fx-background-color: #0a0f1c;");

        Label title = new Label("LoreRuneTerra");
        title.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        Label subtitle = new Label("Explora las historias y leyendas de Runeterra");
        subtitle.setStyle("-fx-font-size: 22px; -fx-text-fill: #a09b8c;");

        Button startButton = new Button("Entrar al Mundo");
        startButton.setStyle("-fx-background-color: #c8aa6e; -fx-text-fill: #0a0f1c; -fx-font-size: 28px; -fx-padding: 25 70; -fx-background-radius: 15;");
        startButton.setOnAction(e -> showChampionScreen());

        welcome.getChildren().addAll(title, subtitle, startButton);
        root.setCenter(welcome);
    }

    private void showChampionScreen() {
        System.out.println("=== Entrando a pantalla de campeones ===");

        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #0f0f0f;");

        Button btnVolver = new Button("← Volver al Menú");
        btnVolver.setOnAction(e -> showWelcomeScreen());

        Label titulo = new Label("Campeones de Runeterra");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e8d5a3;");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar campeón...");
        searchField.setMaxWidth(450);
        searchField.setPrefHeight(38);
        searchField.setStyle("""
            -fx-background-color: #2d2d2d; 
            -fx-text-fill: white; 
            -fx-prompt-text-fill: #aaaaaa;
            -fx-font-size: 15px;                         // ← letra más grande
            -fx-padding: 8 12;                           // ← más espacio interno
        """);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        topBar.getChildren().addAll(btnVolver, spacer1, titulo, spacer2, searchField);

        //CONTENEDOR DE CARTAS
        FlowPane flow = new FlowPane();
        flow.setHgap(30);
        flow.setVgap(30);
        flow.setPadding(new Insets(40));
        flow.setStyle("-fx-background-color: #0d1624;");

        for (Campeon c : campeonesList) {
            flow.getChildren().add(createChampionCard(c));
        }

        // Carta especial para "Nuevo Campeón Personalizado"
        VBox newCard = new VBox(12);
        newCard.setAlignment(Pos.CENTER);
        newCard.setPrefWidth(200);
        newCard.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 15; -fx-padding: 15; -fx-border-color: #c8aa6e; -fx-border-width: 2; -fx-border-style: dashed;");

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 60px; -fx-text-fill: #c8aa6e; -fx-font-weight: bold;");

        Label text = new Label("Nuevo Campeón");
        text.setStyle("-fx-font-size: 16px; -fx-text-fill: #c8aa6e;");

        newCard.getChildren().addAll(plus, text);

        newCard.setOnMouseClicked(e -> {
            System.out.println("Clic en Nueva Carta Personalizada");
            abrirFormularioNuevoCampeon(); // Esto abre el diálogo para crear el campeón


        });

        flow.getChildren().add(newCard);

        // Buscador funcional
        searchField.textProperty().addListener((obs, old, newValue) -> {
            String filter = (newValue == null ? "" : newValue).toLowerCase().trim();
            flow.getChildren().clear();

            for (Campeon c : campeonesList) {
                if (c.getNombre().toLowerCase().contains(filter)) {
                    flow.getChildren().add(createChampionCard(c));
                }
            }
        });

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);

        VBox main = new VBox(topBar, scroll);
        root.setCenter(main);

        System.out.println("=== Pantalla de campeones cargada con buscador ===");
    }

    private VBox createChampionCard(Campeon campeon) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 15; -fx-padding: 15;");

        ImageView img = new ImageView();

        // Elegir la ruta correcta del icono
        String ruta;
        if (campeon.getImagenIcono() != null && !campeon.getImagenIcono().isEmpty()) {
            ruta = campeon.getImagenIcono();
        } else {
            ruta = campeon.getImagen(); // Dataset de DataDragon
        }

        try {
            if (ruta != null && !ruta.isEmpty()) {
                File file = new File(ruta.replace("file:///", ""));
                if (file.exists() && file.canRead()) {
                    img.setImage(new Image(file.toURI().toString()));
                } else {
                    System.out.println("No se encontró la imagen de la carta: " + ruta);
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando imagen de la carta: " + e.getMessage());
        }

        img.setFitWidth(170);
        img.setFitHeight(170);
        img.setPreserveRatio(true);

        Label name = new Label(campeon.getNombre());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        card.getChildren().addAll(img, name);

        card.setOnMouseClicked(e -> {
            System.out.println("Clic en: " + campeon.getNombre());
            ChampionBookView view = new ChampionBookView();
            view.mostrarLibro(campeon, primaryStage);
        });

        return card;
    }

    public BorderPane getRoot() {
        return root;
    }

    private void abrirFormularioNuevoCampeon() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Campeón Personalizado");
        dialog.setHeaderText("Crea tu propio campeón");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Campos de texto para información básica
        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del campeón");

        TextField tituloField = new TextField();
        tituloField.setPromptText("Título (ej: el Destructor)");

        // Campos de texto para imágenes
        TextField iconoField = new TextField();
        iconoField.setPromptText("Ruta de imagen del icono");

        TextField splashField = new TextField();
        splashField.setPromptText("Ruta de imagen del splashart");

        // Campos de texto para biografías
        TextArea bioCortaField = new TextArea();
        bioCortaField.setPromptText("Biografía corta");
        bioCortaField.setPrefHeight(80);

        TextArea bioCompletaField = new TextArea();
        bioCompletaField.setPromptText("Biografía completa");
        bioCompletaField.setPrefHeight(120);

        TextArea bioPrimeraField = new TextArea();
        bioPrimeraField.setPromptText("Biografía en primera persona (opcional)");
        bioPrimeraField.setPrefHeight(100);

        // Agregar todos los campos al contenido en orden lógico
        content.getChildren().addAll(
                new Label("Nombre:"), nombreField,
                new Label("Título:"), tituloField,
                new Label("Ruta del icono:"), iconoField,
                new Label("Ruta del splashart:"), splashField,
                new Label("Biografía corta:"), bioCortaField,
                new Label("Biografía completa:"), bioCompletaField,
                new Label("Biografía en primera persona:"), bioPrimeraField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    System.out.println("Error: El nombre no puede estar vacío");
                    return null;
                }

                Campeon nuevo = new Campeon(
                        "custom_" + System.currentTimeMillis(),
                        nombre,
                        tituloField.getText().trim(),
                        "" // dejamos el campo 'imagen' vacío, usaremos icono y splash
                );

                ChampionDAO dao = new ChampionDAO();
                dao.insertarCampeonPersonalizado(nuevo);

                campeonesList.add(nuevo);
                showChampionScreen();

                // Guardar rutas de imágenes
                nuevo.setImagenIcono(iconoField.getText().trim());
                nuevo.setImagenSplash(splashField.getText().trim());

                // Guardar biografías
                nuevo.setBioCorta(bioCortaField.getText().trim());
                nuevo.setBioCompleta(bioCompletaField.getText().trim());
                nuevo.setBioPrimera(bioPrimeraField.getText().trim());

                // Añadir campeón a la lista
                campeonesList.add(nuevo);

                // Recargar la vista para mostrar el nuevo campeón
                showChampionScreen();
            }
            return null;
        });

        dialog.showAndWait();
    }


}