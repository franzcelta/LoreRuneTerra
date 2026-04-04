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
            // Aquí más adelante abriremos el formulario para crear
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
        try {
            String ruta = campeon.getImagen().replace("file:///", "");
            File file = new File(ruta);
            if (file.exists()) {
                img.setImage(new Image(file.toURI().toString()));
            }
        } catch (Exception ignored) {}

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
}