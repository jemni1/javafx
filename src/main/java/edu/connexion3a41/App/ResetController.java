package edu.connexion3a41.App;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.Random;

public class ResetController {
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;


    @FXML
    public void resetPassword() {
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters");
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
            return;
        }

        try {
            updatePassword(RequestController.getStoredEmail(), password);
            errorLabel.setVisible(false);
            successLabel.setText("Password reset successfully");
            successLabel.setVisible(true);
        } catch (Exception e) {
            errorLabel.setText("Failed to reset password: " + e.getMessage());
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
            e.printStackTrace();
        }
    }

    private void updatePassword(String email, String password) throws Exception {
        String url = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String passwordDb = "";

        try (Connection conn = DriverManager.getConnection(url, user, passwordDb)) {
            String sql = "UPDATE user SET password = ?, reset_code = NULL, reset_code_expires_at = NULL WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, password); // TODO: Hash password in production
            stmt.setString(2, email);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new Exception("User not found");
            }
            System.out.println("Password updated for " + email);
        }
    }
    private String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = new byte[16];
        new Random().nextBytes(salt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash) + ":" + Base64.getEncoder().encodeToString(salt);
    }
}