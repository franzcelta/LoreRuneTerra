package com.loreruneterra;

import com.loreruneterra.controller.MainController;
import com.loreruneterra.db.ChampionDAO;
import com.loreruneterra.model.Campeon;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private final ObservableList<Campeon> campeonesList = FXCollections.observableArrayList();
    private final ChampionDAO championDAO = new ChampionDAO();

    private Stage primaryStage;  // ← campo para guardar la ventana principal

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;  // ← asignamos directamente aquí (opcional, pero recomendado)

        // Cargar datos
        List<Campeon> lista = championDAO.getAllCampeones();
        campeonesList.setAll(lista);

        // Crear el controlador
        MainController controller = new MainController(championDAO, campeonesList);

        // Pasar la stage principal al controlador (para abrir ventanas modales)
        controller.setPrimaryStage(primaryStage);

        // Crear escena
        Scene scene = new Scene(controller.getRoot(), 1300, 800);
        scene.setFill(javafx.scene.paint.Color.BLACK);

        // Cargar CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("LoreRuneTerra");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Setter (fuera del método start)
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}