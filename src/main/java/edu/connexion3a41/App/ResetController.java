package edu.connexion3a41.App;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ResetController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    @FXML
    public void resetPassword() {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Reset message states
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        // Validate password
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Both password fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            showError("Password must be 8+ characters with uppercase, lowercase, number, and symbol");
            return;
        }

        // Update password in database
        try {
            String email = RequestController.getStoredEmail();
            if (email == null || email.isEmpty()) {
                showError("No email found for reset");
                return;
            }

            String url = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
            String user = "root";
            String dbPassword = "";

            try (Connection conn = DriverManager.getConnection(url, user, dbPassword)) {
                String sql = "UPDATE user SET password = ?, reset_code = NULL, reset_code_expires_at = NULL WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, password); // In production, hash the password
                stmt.setString(2, email);
                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    showError("Email not found in database");
                    return;
                }
            }

            // Show success message
            showSuccess("✓ Password reset successfully!");

            // Navigate to login page after delay
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
                    Stage stage = (Stage) passwordField.getScene().getWindow();
                    stage.setScene(new Scene(root, 500, 400));
                    stage.setTitle("Login");
                } catch (Exception e) {
                    showError("Error navigating to login: " + e.getMessage());
                }
            });
            delay.play();

        } catch (Exception e) {
            showError("Failed to reset password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    @FXML
    public void backToRequest() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/request.fxml"));
        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Password Reset");
    }
}