package edu.connexion3a41.App;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class VerifyController {
    @FXML
    private TextField codeField;
    @FXML
    private Label errorLabel;

    @FXML
    public void verifyCode() throws Exception {
        String enteredCode = codeField.getText().trim();

        // Reset error state
        errorLabel.setVisible(false);

        if (!enteredCode.matches("\\d{6}")) {
            showError("Please enter a 6-digit code");
            return;
        }

        String email = RequestController.getStoredEmail();
        String[] codeData = getVerificationCodeFromDatabase(email);
        String storedCode = codeData[0];
        String expiresAt = codeData[1];

        System.out.println("Entered code: " + enteredCode + ", Stored code: " + storedCode + ", Expires at: " + expiresAt);

        if (storedCode == null) {
            showError("No verification code found for email");
            return;
        }

        if (expiresAt != null) {
            Timestamp expires = Timestamp.valueOf(expiresAt);
            if (System.currentTimeMillis() > expires.getTime()) {
                showError("Verification code has expired");
                return;
            }
        }

        if (enteredCode.equals(storedCode)) {
            Parent root = FXMLLoader.load(getClass().getResource("/reset.fxml"));
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 400));
            stage.setTitle("Reset Password");
        } else {
            showError("Invalid verification code");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private String[] getVerificationCodeFromDatabase(String email) throws Exception {
        String url = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT reset_code, reset_code_expires_at FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String code = rs.getString("reset_code");
                Timestamp expires = rs.getTimestamp("reset_code_expires_at");
                return new String[]{code, expires != null ? expires.toString() : null};
            }
        }
        return new String[]{null, null};
    }

    @FXML
    public void backToRequest() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/request.fxml"));
        Stage stage = (Stage) codeField.getScene().getWindow();
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Password Reset");
    }
}