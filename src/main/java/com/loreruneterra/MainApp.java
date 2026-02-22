package com.loreruneterra;  // ajusta si usas otro package

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.getChildren().add(new Label("¡LoreRuneTerra iniciado! Prueba JavaFX OK"));

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("LoreRuneTerra - Prueba Básica");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);  // ¡Esto es clave! Llama al launcher de JavaFX
    }
}