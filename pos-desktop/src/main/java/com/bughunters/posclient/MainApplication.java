package com.bughunters.posclient;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bug Hunters POS Desktop");
        // Structure: Load primary FXML scene
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
