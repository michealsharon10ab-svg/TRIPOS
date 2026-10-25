package com.tripos.pos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Entry point — launches the Checkout (POS Sale) screen directly.
 * Login screen is handled by a separate team member.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL fxml = getClass().getResource("/com/tripos/pos/view/CheckoutView.fxml");
        if (fxml == null) throw new IllegalStateException("CheckoutView.fxml not found.");

        FXMLLoader loader = new FXMLLoader(fxml);
        Scene scene = new Scene(loader.load(), 1440, 900);

        URL css = getClass().getResource("/css/pos-theme.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("TriPOS — POS Sale  [PROTOTYPE]");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
