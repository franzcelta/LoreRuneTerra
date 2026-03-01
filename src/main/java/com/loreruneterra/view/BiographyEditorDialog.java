package com.loreruneterra.view;

import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BiographyEditorDialog {

    private final ChampionDAO championDAO;

    public BiographyEditorDialog(ChampionDAO championDAO) {
        this.championDAO = championDAO;
    }

    public void show(Campeon campeon, TextArea txtBiografia) {
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
                championDAO.saveBiografia(campeon.getKey(), nuevoTexto);
                String bio = championDAO.getBiografiaCompleta(campeon.getKey());
                txtBiografia.setText(bio != null && !bio.trim().isEmpty() ? bio.replace("\n", "\n\n") : "No hay biografía guardada aún.");
            }
        });
    }
}
