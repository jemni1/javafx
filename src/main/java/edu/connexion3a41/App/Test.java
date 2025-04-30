package edu.connexion3a41.App;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test extends Application {

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/data"; // Replace with your DB name
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = ""; // Replace with your MySQL password

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Testing database connection...");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);

        // Test the connection
        String connectionStatus = testConnection() ? "Connection successful!" : "Connection failed!";
        label.setText(connectionStatus);

        primaryStage.setTitle("Database Connection Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean testConnection() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Successfully connected to MySQL database!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
        return false;
    }
}