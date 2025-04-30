package edu.connexion3a41.App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class PasswordResetApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Debug resource paths
        URL resourceUrl = getClass().getResource("/request.fxml");
        System.out.println("getClass().getResource(/request.fxml): " + resourceUrl);
        URL classLoaderUrl = getClass().getClassLoader().getResource("request.fxml");
        System.out.println("getClassLoader().getResource(request.fxml): " + classLoaderUrl);

        if (resourceUrl == null && classLoaderUrl == null) {
            System.err.println("Error: request.fxml not found in src/main/resources or target/classes");
            return;
        }

        // Use the first non-null URL
        Parent root = resourceUrl != null
                ? FXMLLoader.load(resourceUrl)
                : FXMLLoader.load(classLoaderUrl);

        primaryStage.setTitle("Password Reset");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}