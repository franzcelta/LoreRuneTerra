package com.loreruneterra.view;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class ChampionBookView {

    private final ChampionDAO championDAO = new ChampionDAO();  // ← Instancia propia del DAO

    public void mostrarLibro(Campeon campeon, Stage ownerStage) {
        Stage libroStage = new Stage();
        libroStage.setTitle(campeon.getNombre() + " - Libro de Runeterra");
        libroStage.initOwner(ownerStage);
        libroStage.initModality(Modality.WINDOW_MODAL);

        BorderPane libroRoot = new BorderPane();
        libroRoot.setStyle("-fx-background-color: #0f0f0f; -fx-padding: 20;");

        Label libroTitulo = new Label(campeon.getNombre() + " - " + campeon.getTitulo());
        libroTitulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #c8aa6e;");
        libroRoot.setTop(libroTitulo);
        BorderPane.setAlignment(libroTitulo, Pos.CENTER);

        VBox contenido = new VBox(20);
        contenido.setAlignment(Pos.CENTER);

        ImageView splashGrande = new ImageView();
        String rutaSplash = "file:///C:/Users/franz/Documents/LoreRuneTerra ASSETS/img/champion/splash/" + campeon.getKey() + "_0.jpg";
        try {
            String rutaLimpia = rutaSplash.replace("file:///", "");
            File file = new File(rutaLimpia);
            if (file.exists() && file.canRead()) {
                splashGrande.setImage(new Image(file.toURI().toString()));
            } else {
                System.out.println("Splashart no encontrado: " + rutaLimpia);
            }
        } catch (Exception e) {
            System.err.println("Error cargando splashart: " + e.getMessage());
        }
        splashGrande.setFitWidth(600);
        splashGrande.setPreserveRatio(true);
        splashGrande.setSmooth(true);

        TextArea bioArea = new TextArea();
        bioArea.setWrapText(true);
        bioArea.setEditable(false);
        bioArea.setPrefHeight(400);
        bioArea.setStyle("-fx-control-inner-background: #1c1c2e; -fx-text-fill: #e6e6e6; -fx-font-size: 15px;");

        // Cargar bio usando el DAO (no MainApp)
        String bio = championDAO.getBiografiaCompleta(campeon.getKey());
        bioArea.setText(bio != null && !bio.trim().isEmpty() ? bio : "No hay biografía guardada aún.");

        contenido.getChildren().addAll(splashGrande, bioArea);
        libroRoot.setCenter(contenido);

        Button cerrarBtn = new Button("Cerrar libro");
        cerrarBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        cerrarBtn.setOnAction(e -> libroStage.close());
        libroRoot.setBottom(cerrarBtn);
        BorderPane.setAlignment(cerrarBtn, Pos.CENTER);

        Scene libroScene = new Scene(libroRoot, 1000, 800);
        libroStage.setScene(libroScene);
        libroStage.show();
    }
}