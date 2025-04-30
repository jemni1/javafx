package edu.connexion3a41.App;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class CompleteProfileController implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField cinField;
    @FXML
    private ComboBox<String> rolesCombo;
    @FXML
    private Label errorLabel;

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,20}$";
    private static final String CIN_PATTERN = "^\\d{8}$";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/data";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private String email;
    private String name;
    private String profilePictureUrl; // For storing Google profile picture URL
    private GoogleOAuthService.AuthCallback callback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rolesCombo.setItems(FXCollections.observableArrayList("Client", "Worker", "Agriculture"));
    }

    public void setUserData(String email, String name, String profilePictureUrl, GoogleOAuthService.AuthCallback callback) {
        this.email = email;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.callback = callback;

        // Pre-fill name field if available
        if (name != null && !name.isEmpty()) {
            String[] names = name.split(" ");
            if (names.length > 0) {
                surnameField.setText(names.length > 1 ? names[1] : "");
            }
        }
    }

    @FXML
    private void handleSubmit() {
        String username = usernameField.getText().trim();
        String surname = surnameField.getText().trim();
        String cin = cinField.getText().trim();
        String role = rolesCombo.getValue();

        // Validate inputs
        if (!Pattern.matches(USERNAME_PATTERN, username)) {
            errorLabel.setText("Username must be 3-20 characters (letters, numbers, _, -).");
            return;
        }
        if (surname.isEmpty()) {
            errorLabel.setText("Surname cannot be empty.");
            return;
        }
        if (!Pattern.matches(CIN_PATTERN, cin)) {
            errorLabel.setText("CIN must be an 8-digit number.");
            return;
        }
        if (role == null) {
            errorLabel.setText("Please select a role.");
            return;
        }

        // Map role to database format
        String dbRole;
        switch(role) {
            case "Client":
                dbRole = "ROLE_CLIENT";
                break;
            case "Worker":
                dbRole = "ROLE_WORKER";
                break;
            case "Agriculture":
                dbRole = "ROLE_AGRICULTURE";
                break;
            default:
                dbRole = "ROLE_CLIENT";
        }

        try {
            // Check if username and CIN are unique
            if (!isUsernameUnique(username)) {
                errorLabel.setText("Username is already taken.");
                return;
            }
            if (!isCinUnique(cin)) {
                errorLabel.setText("CIN is already in use.");
                return;
            }

            // Save user to database
            saveUserToDatabase(username, surname, cin, dbRole);
            Platform.runLater(() -> {
                callback.onSuccess();
                // Close the window
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.close();
            });
        } catch (SQLException e) {
            errorLabel.setText("Error saving profile: " + e.getMessage());
        }
    }

    private boolean isUsernameUnique(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            var rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private boolean isCinUnique(String cin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE CIN = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cin);
            var rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void saveUserToDatabase(String username, String surname, String cin, String role) throws SQLException {
        String sql = "INSERT INTO user (email, name, surname, username, CIN, password, roles, profile_picture, updated_at, is_mfa_enabled, face_label) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, name);
            pstmt.setString(3, surname);
            pstmt.setString(4, username);
            pstmt.setString(5, cin);
            pstmt.setString(6, ""); // No password for OAuth users
            pstmt.setString(7, "[\"" + role + "\"]"); // JSON array for roles
            pstmt.setString(8, profilePictureUrl); // Google profile picture
            pstmt.setString(9, currentTime); // Current timestamp for updated_at
            pstmt.setBoolean(10, false); // MFA disabled by default
            pstmt.setInt(11, 0); // Default face_label value

            pstmt.executeUpdate();
        }
    }
}