package com.loreruneterra.view;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BiographyEditorDialog {

    private final ChampionDAO championDAO;

    public BiographyEditorDialog(ChampionDAO championDAO) {
        this.championDAO = championDAO;
    }

    public void show(Campeon campeon) {
        Stage editorStage = new Stage();
        editorStage.setTitle("Editar biografías de " + campeon.getNombre());
        editorStage.initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        Label lblCorta = new Label("Biografía corta (intro narrativa atractiva):");
        TextArea areaCorta = new TextArea();
        areaCorta.setWrapText(true);
        areaCorta.setPrefHeight(150);

        Label lblCompleta = new Label("Biografía completa (historia detallada):");
        TextArea areaCompleta = new TextArea();
        areaCompleta.setWrapText(true);
        areaCompleta.setPrefHeight(300);

        Label lblPrimera = new Label("Biografía en primera persona (solo para algunos campeones):");
        TextArea areaPrimera = new TextArea();
        areaPrimera.setWrapText(true);
        areaPrimera.setPrefHeight(300);

        // Cargar versiones actuales
        areaCorta.setText(championDAO.getBiografiaCorta(campeon.getKey()) != null ? championDAO.getBiografiaCorta(campeon.getKey()) : "");
        areaCompleta.setText(championDAO.getBiografiaCompleta(campeon.getKey()) != null ? championDAO.getBiografiaCompleta(campeon.getKey()) : "");
        areaPrimera.setText(championDAO.getBiografiaPrimeraPersona(campeon.getKey()) != null ? championDAO.getBiografiaPrimeraPersona(campeon.getKey()) : "");

        Button btnGuardar = new Button("Guardar todas las versiones");
        btnGuardar.setOnAction(e -> {
            championDAO.saveBiografia(campeon.getKey(), areaCorta.getText(), areaCompleta.getText(), areaPrimera.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Guardado");
            alert.setHeaderText(null);
            alert.setContentText("Las 3 biografías guardadas correctamente.");
            alert.showAndWait();
            editorStage.close();
        });

        layout.getChildren().addAll(lblCorta, areaCorta, lblCompleta, areaCompleta, lblPrimera, areaPrimera, btnGuardar);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 900, 900);
        editorStage.setScene(scene);
        editorStage.show();
    }
}