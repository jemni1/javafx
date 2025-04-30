package edu.connexion3a41.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class MfaController {

    @FXML
    private TextField codeField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private Button verifyButton;

    @FXML
    private Button resendButton;

    private String email;
    private String correctCode;
    private Stage stage;

    private long lastResendTime = 0;
    private static final long RESEND_COOLDOWN = 60 * 1000; // 1 minute cooldown

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/data";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void initialize(String email, String verificationCode) {
        this.email = email;
        this.correctCode = verificationCode;
        // Ensure labels are reset
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleVerifyCode(ActionEvent event) {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            errorLabel.setText("Please enter the verification code");
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
            return;
        }

        try {
            if (verifyCode(email, enteredCode)) {
                // Clear the verification code from the database
                clearVerificationCode(email);
                showHomeScreen();
            } else {
                errorLabel.setText("Invalid or expired verification code");
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
            }
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
        }
    }

    @FXML
    private void handleResendCode(ActionEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResendTime < RESEND_COOLDOWN) {
            errorLabel.setText("Please wait before resending the code");
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
            return;
        }

        try {
            // Generate new code
            String newCode = generateRandomCode();
            // Store new code
            storeVerificationCode(email, newCode);
            // Fetch username from database
            String username = getUsername(email);
            // Send email
            Mail mailSender = new Mail();
            boolean emailSent = mailSender.sendVerificationEmail(email, username, newCode);

            if (emailSent) {
                this.correctCode = newCode;
                lastResendTime = currentTime;
                successLabel.setText("New verification code sent to your email");
                successLabel.setVisible(true);
                errorLabel.setVisible(false);
            } else {
                errorLabel.setText("Failed to send verification code");
                errorLabel.setVisible(true);
                successLabel.setVisible(false);
            }
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
        }
    }

    @FXML
    private void restrictCodeInput(KeyEvent event) {
        String text = codeField.getText();
        if (text.length() > 6) {
            codeField.setText(text.substring(0, 6));
            codeField.positionCaret(6);
        }
        if (!text.matches("[0-9]*")) {
            codeField.setText(text.replaceAll("[^0-9]", ""));
            codeField.positionCaret(codeField.getText().length());
        }
    }

    private boolean verifyCode(String email, String enteredCode) throws SQLException {
        String sql = "SELECT reset_code, reset_code_expires_at FROM user WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedCode = rs.getString("reset_code");
                java.sql.Timestamp expiresAt = rs.getTimestamp("reset_code_expires_at");

                // Check if code is expired (valid for 5 minutes)
                if (expiresAt != null) {
                    long currentTime = System.currentTimeMillis();
                    long expireTime = expiresAt.getTime();
                    if ((currentTime - expireTime) > 5 * 60 * 1000) {
                        return false; // Code expired
                    }
                }

                return enteredCode.equals(storedCode);
            }
            return false;
        }
    }

    private void clearVerificationCode(String email) throws SQLException {
        String sql = "UPDATE user SET reset_code = NULL, reset_code_expires_at = NULL WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.format("%06d", code);
    }

    private void storeVerificationCode(String email, String code) throws SQLException {
        String sql = "UPDATE user SET reset_code = ?, reset_code_expires_at = NOW() WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, email);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No user found with email: " + email);
            }
        }
    }

    private String getUsername(String email) throws SQLException {
        String sql = "SELECT username FROM user WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username != null && !username.isEmpty() ? username : "User";
            }
            return "User";
        }
    }

    private void showHomeScreen() {
        try {
            Parent root;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeScreen.fxml"));
                root = loader.load();
            } catch (Exception e) {
                root = new javafx.scene.layout.BorderPane(new Label("Welcome to Home!"));
            }

            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Home");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading home screen: " + e.getMessage());
            errorLabel.setVisible(true);
            successLabel.setVisible(false);
        }
    }
}