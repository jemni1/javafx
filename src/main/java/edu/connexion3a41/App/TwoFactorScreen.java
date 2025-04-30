package edu.connexion3a41.App;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Scene;


public class TwoFactorScreen {
    private GridPane view;
    private Stage primaryStage;
    private User user;
    private String simulated2FACode = "123456";

    public TwoFactorScreen(Stage primaryStage, User user) {
        this.primaryStage = primaryStage;
        this.user = user;
        this.view = new GridPane();
        setupUI();
    }

    private void setupUI() {
        view.setAlignment(Pos.CENTER);
        view.setHgap(10);
        view.setVgap(10);
        view.setPadding(new Insets(25, 25, 25, 25));

        Label codeLabel = new Label("Enter 2FA Code:");
        TextField codeField = new TextField();
        view.add(codeLabel, 0, 0);
        view.add(codeField, 1, 0);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        view.add(errorLabel, 1, 1);

        Button verifyButton = new Button("Verify");
        verifyButton.setOnAction(e -> handle2FAVerification(codeField.getText(), errorLabel));
        view.add(verifyButton, 1, 2);
    }

    private void handle2FAVerification(String code, Label errorLabel) {
        if (code.equals(simulated2FACode)) {
            showHomeScreen();
        } else {
            errorLabel.setText("Invalid 2FA code");
        }
    }

    private void showHomeScreen() {
        primaryStage.setScene(new Scene(new Label("Welcome, " + user.getName() + "!"), 400, 300));
    }

    public GridPane getView() {
        return view;
    }
}