package com.loreruneterra.controller;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import com.loreruneterra.view.ChampionBookView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controlador principal de la aplicación LoreRuneTerra.
 * Gestiona la pantalla de bienvenida, el catálogo de campeones
 * y las operaciones CRUD (Crear, Leer, Actualizar, Eliminar).
 *
 * Patrón MVC: este controlador no contiene SQL — delega toda
 * la persistencia en ChampionDAO.
 */
public class MainController {

    private final ChampionDAO championDAO;
    private final ObservableList<Campeon> campeonesList;
    private final BorderPane root = new BorderPane();
    private Stage primaryStage;

    public MainController(ChampionDAO championDAO, ObservableList<Campeon> campeonesList) {
        this.championDAO   = championDAO;
        this.campeonesList = campeonesList;
        showWelcomeScreen();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // ══════════════════════════════════════════
    //  PANTALLAS DE NAVEGACIÓN
    // ══════════════════════════════════════════

    private void showWelcomeScreen() {
        VBox welcome = new VBox(60);
        welcome.setAlignment(Pos.CENTER);
        welcome.setStyle("-fx-background-color: #0a0f1c;");

        Label title = new Label("LoreRuneTerra");
        title.setStyle("-fx-font-size: 60px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");

        Label subtitle = new Label("Explora las historias y leyendas de Runeterra");
        subtitle.setStyle("-fx-font-size: 22px; -fx-text-fill: #a09b8c;");

        Button startButton = new Button("Entrar al Mundo");
        startButton.setStyle("-fx-background-color: #c8aa6e; -fx-text-fill: #0a0f1c; " +
                "-fx-font-size: 28px; -fx-padding: 25 70; -fx-background-radius: 15;");
        startButton.setOnAction(e -> showChampionScreen());

        welcome.getChildren().addAll(title, subtitle, startButton);
        root.setCenter(welcome);
    }

    private void showChampionScreen() {
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
            -fx-font-size: 15px;
            -fx-padding: 8 12;
        """);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        topBar.getChildren().addAll(btnVolver, spacer1, titulo, spacer2, searchField);

        // ── Contenedor de cartas ──
        FlowPane flow = new FlowPane();
        flow.setHgap(30);
        flow.setVgap(30);
        flow.setPadding(new Insets(40));
        flow.setStyle("-fx-background-color: #0d1624;");

        for (Campeon c : campeonesList) {
            flow.getChildren().add(createChampionCard(c));
        }

        // Carta especial "Nuevo Campeón"
        flow.getChildren().add(createNewChampionCard());

        // Buscador funcional (filtro en tiempo real sobre campeonesList)
        searchField.textProperty().addListener((obs, old, newVal) -> {
            String filtro = newVal == null ? "" : newVal.toLowerCase().trim();
            flow.getChildren().clear();
            for (Campeon c : campeonesList) {
                if (c.getNombre().toLowerCase().contains(filtro)) {
                    flow.getChildren().add(createChampionCard(c));
                }
            }
            // Siempre mostrar carta de "Nuevo" al final
            if (filtro.isEmpty()) {
                flow.getChildren().add(createNewChampionCard());
            }
        });

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        root.setCenter(new VBox(topBar, scroll));
    }

    // ══════════════════════════════════════════
    //  TARJETAS DE CAMPEÓN
    // ══════════════════════════════════════════

    private VBox createChampionCard(Campeon campeon) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 15; -fx-padding: 15;");

        ImageView img = new ImageView();
        String ruta = (campeon.getImagenIcono() != null && !campeon.getImagenIcono().isEmpty())
                ? campeon.getImagenIcono()
                : campeon.getImagen();
        try {
            if (ruta != null && !ruta.isEmpty()) {
                File file = new File(ruta.replace("file:///", ""));
                if (file.exists() && file.canRead()) {
                    img.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando imagen: " + e.getMessage());
        }
        img.setFitWidth(170);
        img.setFitHeight(170);
        img.setPreserveRatio(true);

        Label name = new Label(campeon.getNombre());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Botones de editar y eliminar (aparecen en la tarjeta)
        HBox botones = new HBox(8);
        botones.setAlignment(Pos.CENTER);

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle("-fx-background-color: #c8aa6e; -fx-text-fill: #0a0f1c; " +
                "-fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6;");
        btnEditar.setOnAction(e -> {
            e.consume();
            abrirFormularioEditar(campeon);
        });

        Button btnEliminar = new Button("🗑 Eliminar");
        btnEliminar.setStyle("-fx-background-color: #8b1a1a; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 4 10; -fx-background-radius: 6;");
        btnEliminar.setOnAction(e -> {
            e.consume();
            confirmarEliminar(campeon);
        });

        botones.getChildren().addAll(btnEditar, btnEliminar);
        card.getChildren().addAll(img, name, botones);

        // Clic en la tarjeta → abrir libro del campeón
        card.setOnMouseClicked(e -> {
            ChampionBookView view = new ChampionBookView();
            view.mostrarLibro(campeon, primaryStage);
        });

        return card;
    }

    private VBox createNewChampionCard() {
        VBox newCard = new VBox(12);
        newCard.setAlignment(Pos.CENTER);
        newCard.setPrefWidth(200);
        newCard.setStyle("-fx-background-color: #1c2526; -fx-background-radius: 15; " +
                "-fx-padding: 15; -fx-border-color: #c8aa6e; " +
                "-fx-border-width: 2; -fx-border-style: dashed;");

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 60px; -fx-text-fill: #c8aa6e; -fx-font-weight: bold;");
        Label text = new Label("Nuevo Campeón");
        text.setStyle("-fx-font-size: 16px; -fx-text-fill: #c8aa6e;");
        newCard.getChildren().addAll(plus, text);
        newCard.setOnMouseClicked(e -> abrirFormularioNuevoCampeon());
        return newCard;
    }

    // ══════════════════════════════════════════
    //  CRUD: CREATE
    // ══════════════════════════════════════════

    private void abrirFormularioNuevoCampeon() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Campeón Personalizado");
        dialog.setHeaderText("Crea tu propio campeón");

        VBox content = buildFormulario(null);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(520);
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Recuperar los campos del formulario para leerlos al confirmar
        TextField nombreField     = (TextField) content.lookup("#nombreField");
        TextField tituloField     = (TextField) content.lookup("#tituloField");
        TextField claseField      = (TextField) content.lookup("#claseField");
        TextField iconoField      = (TextField) content.lookup("#iconoField");
        TextField splashField     = (TextField) content.lookup("#splashField");
        TextArea  bioCortaField   = (TextArea)  content.lookup("#bioCortaField");
        TextArea  bioCompletaField= (TextArea)  content.lookup("#bioCompletaField");
        TextArea  bioPrimeraField = (TextArea)  content.lookup("#bioPrimeraField");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "El nombre no puede estar vacío.");
                    return null;
                }

                Campeon nuevo = new Campeon(
                        "custom_" + System.currentTimeMillis(),
                        nombre,
                        tituloField.getText().trim(),
                        ""
                );
                nuevo.setClase      (claseField.getText().trim());
                nuevo.setImagenIcono(iconoField.getText().trim());
                nuevo.setImagenSplash(splashField.getText().trim());
                nuevo.setBioCorta   (bioCortaField.getText().trim());
                nuevo.setBioCompleta(bioCompletaField.getText().trim());
                nuevo.setBioPrimera (bioPrimeraField.getText().trim());

                // ── PERSISTIR EN BASE DE DATOS ──
                boolean ok = championDAO.createCampeon(nuevo);
                if (ok) {
                    // Guardar también las biografías
                    championDAO.saveBiografia(
                            nuevo.getKey(),
                            nuevo.getBioCorta(),
                            nuevo.getBioCompleta(),
                            nuevo.getBioPrimera()
                    );
                    campeonesList.add(nuevo);
                    showChampionScreen();  // Recargar vista
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Campeón creado",
                            "'" + nuevo.getNombre() + "' se ha añadido correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "No se pudo guardar el campeón en la base de datos.");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════
    //  CRUD: UPDATE
    // ══════════════════════════════════════════

    private void abrirFormularioEditar(Campeon campeon) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Editar Campeón");
        dialog.setHeaderText("Editando: " + campeon.getNombre());

        VBox content = buildFormulario(campeon);  // Pre-rellena con valores actuales
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefHeight(520);
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombreField      = (TextField) content.lookup("#nombreField");
        TextField tituloField      = (TextField) content.lookup("#tituloField");
        TextField claseField       = (TextField) content.lookup("#claseField");
        TextField iconoField       = (TextField) content.lookup("#iconoField");
        TextField splashField      = (TextField) content.lookup("#splashField");
        TextArea  bioCortaField    = (TextArea)  content.lookup("#bioCortaField");
        TextArea  bioCompletaField = (TextArea)  content.lookup("#bioCompletaField");
        TextArea  bioPrimeraField  = (TextArea)  content.lookup("#bioPrimeraField");

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String nombre = nombreField.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "El nombre no puede estar vacío.");
                    return null;
                }

                // Actualizar el objeto en memoria
                campeon.setNombre   (nombre);
                campeon.setTitulo   (tituloField.getText().trim());
                campeon.setClase    (claseField.getText().trim());
                campeon.setImagenIcono (iconoField.getText().trim());
                campeon.setImagenSplash(splashField.getText().trim());
                campeon.setBioCorta   (bioCortaField.getText().trim());
                campeon.setBioCompleta(bioCompletaField.getText().trim());
                campeon.setBioPrimera (bioPrimeraField.getText().trim());

                // ── PERSISTIR EN BASE DE DATOS ──
                boolean ok = championDAO.updateCampeon(campeon);
                if (ok) {
                    championDAO.saveBiografia(
                            campeon.getKey(),
                            campeon.getBioCorta(),
                            campeon.getBioCompleta(),
                            campeon.getBioPrimera()
                    );
                    showChampionScreen();
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Campeón actualizado",
                            "'" + campeon.getNombre() + "' se ha actualizado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "No se pudo actualizar el campeón en la base de datos.");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    // ══════════════════════════════════════════
    //  CRUD: DELETE
    // ══════════════════════════════════════════

    private void confirmarEliminar(Campeon campeon) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar campeón");
        confirm.setHeaderText("¿Eliminar a '" + campeon.getNombre() + "'?");
        confirm.setContentText(
                "Esta acción es irreversible.\n" +
                        "También se eliminarán todas sus biografías asociadas.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // ── ELIMINAR EN BASE DE DATOS ──
                boolean ok = championDAO.deleteCampeon(campeon.getId());
                if (ok) {
                    campeonesList.remove(campeon);
                    showChampionScreen();
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Campeón eliminado",
                            "'" + campeon.getNombre() + "' ha sido eliminado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "No se pudo eliminar el campeón de la base de datos.");
                }
            }
        });
    }

    // ══════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════

    /**
     * Construye el formulario de creación/edición.
     * Si se pasa un Campeon existente, los campos se pre-rellenan (modo edición).
     * Si se pasa null, los campos quedan vacíos (modo creación).
     */
    private VBox buildFormulario(Campeon campeon) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField nombreField = new TextField(campeon != null ? campeon.getNombre() : "");
        nombreField.setId("nombreField");
        nombreField.setPromptText("Nombre del campeón *");

        TextField tituloField = new TextField(campeon != null ? campeon.getTitulo() : "");
        tituloField.setId("tituloField");
        tituloField.setPromptText("Título (ej: el Destructor)");

        TextField claseField = new TextField(campeon != null ? campeon.getClase() : "");
        claseField.setId("claseField");
        claseField.setPromptText("Clase (Asesino, Mago, Tanque, Luchador, Soporte, Tirador)");

        TextField iconoField = new TextField(campeon != null ? campeon.getImagenIcono() : "");
        iconoField.setId("iconoField");
        iconoField.setPromptText("Ruta de imagen del icono");

        TextField splashField = new TextField(campeon != null ? campeon.getImagenSplash() : "");
        splashField.setId("splashField");
        splashField.setPromptText("Ruta del splashart");

        TextArea bioCortaField = new TextArea(campeon != null ? campeon.getBioCorta() : "");
        bioCortaField.setId("bioCortaField");
        bioCortaField.setPromptText("Biografía corta");
        bioCortaField.setPrefHeight(80);

        TextArea bioCompletaField = new TextArea(campeon != null ? campeon.getBioCompleta() : "");
        bioCompletaField.setId("bioCompletaField");
        bioCompletaField.setPromptText("Biografía completa");
        bioCompletaField.setPrefHeight(120);

        TextArea bioPrimeraField = new TextArea(campeon != null ? campeon.getBioPrimera() : "");
        bioPrimeraField.setId("bioPrimeraField");
        bioPrimeraField.setPromptText("Biografía en primera persona (opcional)");
        bioPrimeraField.setPrefHeight(100);

        content.getChildren().addAll(
                new Label("Nombre: *"),   nombreField,
                new Label("Título:"),     tituloField,
                new Label("Clase:"),      claseField,
                new Label("Icono:"),      iconoField,
                new Label("Splashart:"),  splashField,
                new Label("Bio corta:"),  bioCortaField,
                new Label("Bio completa:"), bioCompletaField,
                new Label("Bio primera persona:"), bioPrimeraField
        );
        return content;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}