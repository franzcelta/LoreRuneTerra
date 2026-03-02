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

    public void show(Campeon campeon, TextArea txtBiografia) {
        Stage editorStage = new Stage();
        editorStage.setTitle("Editar biografías de " + campeon.getNombre());
        editorStage.initModality(Modality.APPLICATION_MODAL);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));

        // TextArea para biografía corta
        Label lblCorta = new Label("Biografía corta (resumen narrativo):");
        TextArea areaCorta = new TextArea();
        areaCorta.setWrapText(true);
        areaCorta.setPrefHeight(200);
        areaCorta.setStyle("-fx-control-inner-background: #1c1c2e; -fx-text-fill: #e6e6e6;");

        // TextArea para biografía completa
        Label lblCompleta = new Label("Biografía completa (historia detallada):");
        TextArea areaCompleta = new TextArea();
        areaCompleta.setWrapText(true);
        areaCompleta.setPrefHeight(400);
        areaCompleta.setStyle("-fx-control-inner-background: #1c1c2e; -fx-text-fill: #e6e6e6;");

        // Cargar versiones actuales
        String bioCorta = championDAO.getBiografiaCorta(campeon.getKey());
        areaCorta.setText(bioCorta != null ? bioCorta : "");

        String bioCompleta = championDAO.getBiografiaCompleta(campeon.getKey());
        areaCompleta.setText(bioCompleta != null ? bioCompleta : "");

        Button btnGuardar = new Button("Guardar cambios");
        btnGuardar.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        btnGuardar.setOnAction(e -> {
            championDAO.saveBiografia(campeon.getKey(), areaCorta.getText(), areaCompleta.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Guardado");
            alert.setHeaderText(null);
            alert.setContentText("Biografías guardadas correctamente.");
            alert.showAndWait();
            editorStage.close();
        });

        layout.getChildren().addAll(lblCorta, areaCorta, lblCompleta, areaCompleta, btnGuardar);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 800, 700);
        editorStage.setScene(scene);
        editorStage.show();
    }
}