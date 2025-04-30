package edu.connexion3a41.App;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen {
    private final Stage stage;
    private final VBox root;

    // Database credentials (same as RegisterScreen)
    private static final String URL = "jdbc:mysql://localhost:3306/data";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public LoginScreen(Stage stage) {
        this.stage = stage;
        this.root = createLoginUI();
    }

    public Pane getRoot() {
        return root;
    }

    private VBox createLoginUI() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        container.setBackground(new Background(new BackgroundFill(Color.web("#e7f9e7"), null, null)));

        Text title = new Text("Hello! Let's get started");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setFill(Color.web("#66bb6a"));

        Text subtitle = new Text("Sign in to continue.");
        subtitle.setFont(Font.font("System", FontWeight.LIGHT, 14));
        subtitle.setFill(Color.GRAY);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);
        emailField.setPrefHeight(40);
        emailField.setStyle("-fx-font-size: 16px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 16px;");

        Button signInButton = new Button("SIGN IN");
        signInButton.setMaxWidth(300);
        signInButton.setPrefHeight(50);
        signInButton.setStyle(
                "-fx-background-color: #66bb6a; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );
        signInButton.setOnMouseEntered(e -> signInButton.setStyle(
                "-fx-background-color: #81c784; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
        ));
        signInButton.setOnMouseExited(e -> signInButton.setStyle(
                "-fx-background-color: #66bb6a; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;"
        ));
        signInButton.setOnAction(e -> {
            try {
                if (verifyCredentials(emailField.getText(), passwordField.getText())) {
                    showHomeScreen();
                } else {
                    errorLabel.setText("Invalid email or password");
                    errorLabel.setVisible(true);
                }
            } catch (SQLException ex) {
                errorLabel.setText("Database error: " + ex.getMessage());
                errorLabel.setVisible(true);
            }
        });

        CheckBox rememberMe = new CheckBox("Keep me signed in");
        rememberMe.setTextFill(Color.GRAY);

        Hyperlink forgotPassword = new Hyperlink("Forgot password?");
        forgotPassword.setOnAction(e -> showForgotPasswordDialog());

        HBox optionsBox = new HBox(20, rememberMe, forgotPassword);
        optionsBox.setAlignment(Pos.CENTER);

        Hyperlink registerLink = new Hyperlink("Register");
        registerLink.setOnAction(e -> showRegisterScreen());
        Text registerText = new Text("Don't have an account? ");
        registerText.setFill(Color.GRAY);
        HBox registerBox = new HBox(registerText, registerLink);
        registerBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(10, title, subtitle, errorLabel, emailField, passwordField, signInButton,
                optionsBox, registerBox);
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(400);
        formBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
        formBox.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,00,0.1), 10, 0, 0, 4);");

        container.getChildren().add(formBox);
        return container;
    }

    private boolean verifyCredentials(String email, String password) throws SQLException {
        String sql = "SELECT password FROM user WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // For now, compare plain text (since RegisterScreen doesn't hash passwords yet)
                return storedPassword.equals(password);
            }
            return false; // No user found with this email
        }
    }

    private void showHomeScreen() {
        stage.setScene(new Scene(new Label("Welcome to Home!"), 800, 600));
    }

    private void showForgotPasswordDialog() {
        new Alert(Alert.AlertType.INFORMATION, "Forgot Password functionality to be implemented.").showAndWait();
    }

    private void showRegisterScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterScreen.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            RegisterScreenController controller = loader.getController();
            controller.setStage(stage);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}