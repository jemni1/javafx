package edu.connexion3a41.App;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.scene.Parent;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GoogleOAuthService {
    private static final String CLIENT_ID = "127736367393-tcras6qjcatm4ebto7le5dgtc6p6eftg.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private static final String SCOPE = "email%20profile";
    private static final String URL = "jdbc:mysql://localhost:3306/data";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Label errorLabel;

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public GoogleOAuthService(Label errorLabel) {
        this.errorLabel = errorLabel;
    }

    public void startOAuthFlow(AuthCallback callback) {
        try {
            String authUrl = "https://accounts.google.com/o/oauth2/auth" +
                    "?client_id=" + CLIENT_ID +
                    "&redirect_uri=" + REDIRECT_URI +
                    "&response_type=code" +
                    "&scope=" + SCOPE;

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Gmail Authentication");
                dialog.setHeaderText("Enter the authorization code from Google");
                dialog.setContentText("After signing in with Google, copy the code from the redirect URL and paste it here:");

                dialog.showAndWait().ifPresent(code -> {
                    try {
                        exchangeCodeForTokens(code, callback);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            callback.onFailure("Error processing Google authentication: " + e.getMessage());
                        });
                    }
                });
            } else {
                callback.onFailure("Desktop browser not supported. Please use standard login.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailure("Error connecting to Gmail: " + e.getMessage());
        }
    }

    private void exchangeCodeForTokens(String code, AuthCallback callback) throws Exception {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        String requestBody = "code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=" + REDIRECT_URI +
                "&grant_type=authorization_code";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject jsonResponse = new JSONObject(response.body());

        if (jsonResponse.has("error")) {
            Platform.runLater(() -> {
                callback.onFailure("Authentication error: " + jsonResponse.getString("error_description"));
            });
            return;
        }

        String accessToken = jsonResponse.getString("access_token");
        getUserInfo(accessToken, callback);
    }

    private void getUserInfo(String accessToken, AuthCallback callback) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.googleapis.com/oauth2/v1/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject userInfo = new JSONObject(response.body());

        String email = userInfo.getString("email");
        String name = userInfo.getString("name");
        String profilePictureUrl = userInfo.optString("picture", ""); // Get profile picture URL

        // ... rest of your existing code ...

        if (userExistsInDatabase(email)) {
            Platform.runLater(() -> callback.onSuccess());
        } else {
            Platform.runLater(() -> showCompleteProfileScreen(email, name, profilePictureUrl, callback));
        }
    }
    private boolean userExistsInDatabase(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    private void showCompleteProfileScreen(String email, String name, String profilePictureUrl, AuthCallback callback) {
        try {
            // Load from root of resources
            String fxmlPath = "/CompleteProfileScreen.fxml";
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                // Fallback to classloader
                fxmlUrl = getClass().getClassLoader().getResource("CompleteProfileScreen.fxml");
                if (fxmlUrl == null) {
                    throw new IllegalStateException("Cannot find FXML file at: " + fxmlPath);
                }
            }
            System.out.println("Loading FXML from: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Complete Your Profile");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            CompleteProfileController controller = loader.getController();
            controller.setUserData(email, name, profilePictureUrl, callback);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> callback.onFailure("Error loading profile completion screen: " + e.getMessage()));
        }
    }
}