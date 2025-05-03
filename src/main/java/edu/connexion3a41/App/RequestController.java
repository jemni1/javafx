package edu.connexion3a41.App;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Random;

public class RequestController {
    @FXML
    private TextField emailField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label successLabel;

    private static String storedEmail;
    private static String verificationCode;

    private static final String EMAIL_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Your Secure Login Code</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { background-color: #f0f4f8; min-height: 100vh; font-family: 'Helvetica', 'Arial', sans-serif; line-height: 1.6; padding: 20px; }
                .container { max-width: 640px; margin: 0 auto; }
                .card { background: #ffffff; padding: 2rem; border-radius: 12px; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.05); border: 1px solid #e5e7eb; }
                .header { text-align: center; padding-bottom: 1.5rem; border-bottom: 1px solid #f0f0f0; }
                .brand-logo { max-width: 90px; height: auto; margin-bottom: 1rem; }
                .text-primary { color: #2c7be5; }
                .code { font-size: 2.5rem; font-weight: 700; color: #1e40af; text-align: center; letter-spacing: 0.5rem; padding: 1rem; background: #f8fafc; border-radius: 8px; margin: 1.5rem 0; border: 1px dashed #dbeafe; }
                .content { padding: 1.5rem 0; color: #4b5563; font-size: 1rem; }
                .footer { margin-top: 2rem; padding-top: 1rem; border-top: 1px solid #f0f0f0; font-size: 0.875rem; color: #6b7280; text-align: center; }
                @media (max-width: 600px) { .card { padding: 1.5rem; } .code { font-size: 2rem; letter-spacing: 0.3rem; } }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="card">
                    <div class="header">
                        <img src="https://i.postimg.cc/XYzmTTv2/logo.png alt="Company Logo" class="brand-logo">
                        <h2 class="text-primary">Verification Code</h2>
                    </div>
                    <div class="content">
                        <p style="text-align: center">Hello {{ user.username }},</p>
                        <p>Use this code to complete your password reset:</p>
                        <div class="code">{{ code }}</div>
                        <p>This code expires in <strong>15 minutes</strong>. Please keep it confidential.</p>
                        <p>If you didn't request this code, please secure your account immediately.</p>
                    </div>
                    <div class="footer">
                        <p>Need help? Contact us at support@datafarm.com</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """;

    @FXML
    public void sendResetCode() {
        String email = emailField.getText().trim();

        // Reset message states
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Invalid email format");
            errorLabel.setVisible(true);
            return;
        }

        storedEmail = email;
        verificationCode = String.format("%06d", new Random().nextInt(999999));

        try {
            saveVerificationCode(email, verificationCode);
            String username = getUsername(email);
            sendEmailStatic(email, username, verificationCode);

            // Show success message
            successLabel.setText("✓ Verification code sent successfully!");
            successLabel.setVisible(true);

            // Switch to verify scene after short delay
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(event -> {
                try {
                    switchToVerifyScene();
                } catch (Exception e) {
                    showError("Error switching scenes: " + e.getMessage());
                }
            });
            delay.play();

        } catch (Exception e) {
            showError("Failed to send verification code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void saveVerificationCode(String email, String code) throws Exception {
        String url = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE user SET reset_code = ?, reset_code_expires_at = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000);
            stmt.setTimestamp(2, expiresAt);
            stmt.setString(3, email);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new Exception("Email not found in database");
            }
            System.out.println("Verification code saved for " + email + ": " + code + ", expires at " + expiresAt);
        }
    }

    private String getUsername(String email) throws Exception {
        String url = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "SELECT username FROM user WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return username != null && !username.isEmpty() ? username : "User";
            }
            return "User";
        }
    }

    public static void sendEmailStatic(String to, String username, String code) throws MessagingException {
        String from = "monta.bellakhal10@gmail.com";
        String password = "txke lupf wnjg hzfz"; // Your App Password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(from, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Password Reset Verification Code");

        String emailContent = EMAIL_TEMPLATE
                .replace("{{ user.username }}", username)
                .replace("{{ code }}", code);

        message.setContent(emailContent, "text/html; charset=utf-8");
        Transport.send(message);
    }

    private void switchToVerifyScene() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/verify.fxml"));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Verify Code");
    }

    public static String getStoredEmail() {
        return storedEmail;
    }

    public static String getVerificationCode() {
        return verificationCode;
    }

    @FXML
    public void backToLogin() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Login");
    }
}