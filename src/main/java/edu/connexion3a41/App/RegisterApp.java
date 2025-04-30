package edu.connexion3a41.App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class RegisterApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load directly from the root of resources folder
            URL fxmlUrl = getClass().getClassLoader().getResource("RegisterScreen.fxml");

            if (fxmlUrl == null) {
                throw new RuntimeException("Cannot find RegisterScreen.fxml");
            }

            System.out.println("Loading FXML from: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 800, 600);
            primaryStage.setTitle("Register");
            RegisterScreenController controller = loader.getController();
            controller.setStage(primaryStage);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}