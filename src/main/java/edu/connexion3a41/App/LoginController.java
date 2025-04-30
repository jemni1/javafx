package edu.connexion3a41.App;

import edu.connexion3a41.App.GoogleOAuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

// OpenCV imports
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberMe;
    @FXML private Button faceLoginButton;
    @FXML private ImageView webcamView;

    private static final String URL = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;

    // Face recognition variables
    private boolean isCameraActive = false;
    private Task<Void> cameraTask;
    private AtomicBoolean stopCamera = new AtomicBoolean(false);
    private VideoCapture capture;
    private CascadeClassifier faceCascade;
    private static final double FACE_SIMILARITY_THRESHOLD = 0.1; // 10% threshold for face similarity

    @FXML
    private void initialize() {
        // Initialize the face recognition components
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            faceCascade = new CascadeClassifier("src/main/resources/haarcascade_frontalface_default.xml");
            faceLoginButton.setDisable(false);
        } catch (Exception e) {
            System.out.println("Error initializing face recognition: " + e.getMessage());
            faceLoginButton.setDisable(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password");
            errorLabel.setVisible(true);
            logAuthEvent("LOGIN_ATTEMPT", email, "Empty email or password", "FAILURE");
            return;
        }

        try {
            if (verifyCredentials(email, password)) {
                if (isMfaEnabled(email)) {
                    String verificationCode = generateRandomCode();
                    storeVerificationCode(email, verificationCode);
                    String username = getUsername(email);
                    RequestController.sendEmailStatic(email, username, verificationCode);
                    logAuthEvent("MFA_TRIGGERED", email, "MFA code sent", "INFO");
                    showMfaVerificationScreen(email, verificationCode);
                } else {
                    showHomeScreen();
                }
            } else {
                errorLabel.setText("Invalid email or password");
                errorLabel.setVisible(true);
            }
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("LOGIN_ERROR", email, "Database error: " + e.getMessage(), "ERROR");
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("LOGIN_ERROR", email, "Unexpected error: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void handleGmailSignIn(ActionEvent event) {
        try {
            errorLabel.setText("Connecting to Gmail...");
            errorLabel.setVisible(true);

            GoogleOAuthService oauthService = new GoogleOAuthService(errorLabel);
            oauthService.startOAuthFlow(new GoogleOAuthService.AuthCallback() {
                @Override
                public void onSuccess() {
                    logAuthEvent("OAUTH_LOGIN", "[OAuth User]", "Successful Google login", "SUCCESS");
                    showHomeScreen();
                }

                @Override
                public void onFailure(String errorMessage) {
                    errorLabel.setText(errorMessage);
                    errorLabel.setVisible(true);
                    logAuthEvent("OAUTH_LOGIN_FAILED", "[OAuth User]", "Google login failed: " + errorMessage, "FAILURE");
                }
            });
        } catch (Exception e) {
            errorLabel.setText("Error connecting to Gmail: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("OAUTH_LOGIN_ERROR", "[OAuth User]", "Error connecting to Gmail: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/request.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 400));
            stage.setTitle("Password Reset");
            logAuthEvent("PASSWORD_RESET_REQUEST", emailField.getText().trim(), "Password reset screen accessed", "INFO");
        } catch (IOException e) {
            errorLabel.setText("Error loading reset screen: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("PASSWORD_RESET_ERROR", emailField.getText().trim(), "Error loading reset screen: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Register");
            logAuthEvent("REGISTER_REQUEST", emailField.getText().trim(), "Registration screen accessed", "INFO");
        } catch (IOException e) {
            errorLabel.setText("Error loading register screen: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("REGISTER_ERROR", emailField.getText().trim(), "Error loading register screen: " + e.getMessage(), "ERROR");
        }
    }

    @FXML
    private void handleFaceLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        if (!isCameraActive) {
            logAuthEvent("FACE_LOGIN_START", email, "Face login camera started", "INFO");
            startCamera();
        } else {
            logAuthEvent("FACE_LOGIN_STOP", email, "Face login camera stopped", "INFO");
            stopCamera();
        }
    }

    private void startCamera() {
        webcamView.setVisible(true);
        stopCamera.set(false);
        isCameraActive = true;
        faceLoginButton.setText("STOP CAMERA");

        cameraTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                capture = new VideoCapture(0);

                if (!capture.isOpened()) {
                    Platform.runLater(() -> {
                        errorLabel.setText("Failed to open camera");
                        errorLabel.setVisible(true);
                        logAuthEvent("FACE_LOGIN_ERROR", emailField.getText().trim(), "Failed to open camera", "ERROR");
                    });
                    return null;
                }

                Mat frame = new Mat();
                Mat grayFrame = new Mat();

                while (!stopCamera.get() && !isCancelled()) {
                    if (!capture.read(frame)) {
                        Thread.sleep(30);
                        continue;
                    }

                    if (frame.empty()) {
                        Thread.sleep(30);
                        continue;
                    }

                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    MatOfRect faces = new MatOfRect();
                    faceCascade.detectMultiScale(grayFrame, faces);

                    for (Rect rect : faces.toArray()) {
                        Imgproc.rectangle(frame, new Point(rect.x, rect.y),
                                new Point(rect.x + rect.width, rect.y + rect.height),
                                new Scalar(0, 255, 0), 2);

                        Mat faceROI = new Mat(grayFrame, rect);
                        Mat resizedFace = new Mat();
                        Imgproc.resize(faceROI, resizedFace, new Size(100, 100));

                        try {
                            String matchedEmail = findBestFaceMatch(resizedFace);
                            if (matchedEmail != null) {
                                stopCamera.set(true);
                                Platform.runLater(() -> {
                                    try {
                                        emailField.setText(matchedEmail);
                                        logAuthEvent("FACE_LOGIN", matchedEmail, "Face recognition successful", "SUCCESS");

                                        if (isMfaEnabled(matchedEmail)) {
                                            String verificationCode = generateRandomCode();
                                            storeVerificationCode(matchedEmail, verificationCode);
                                            String username = getUsername(matchedEmail);
                                            RequestController.sendEmailStatic(matchedEmail, username, verificationCode);
                                            logAuthEvent("MFA_TRIGGERED", matchedEmail, "MFA code sent", "INFO");
                                            showMfaVerificationScreen(matchedEmail, verificationCode);
                                        } else {
                                            showHomeScreen();
                                        }
                                    } catch (Exception e) {
                                        logAuthEvent("FACE_LOGIN_ERROR", matchedEmail, "Error after face recognition: " + e.getMessage(), "ERROR");
                                        errorLabel.setText("Face login error: " + e.getMessage());
                                        errorLabel.setVisible(true);
                                        e.printStackTrace();
                                    }
                                });
                                break;
                            } else {
                                Platform.runLater(() -> {
                                    errorLabel.setText("No face match found within range");
                                    errorLabel.setVisible(true);
                                    logAuthEvent("FACE_LOGIN_FAILED", emailField.getText().trim(), "No face match found within range", "FAILURE");
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Face recognition error: " + e.getMessage());
                            logAuthEvent("FACE_LOGIN_ERROR", emailField.getText().trim(), "Face recognition error: " + e.getMessage(), "ERROR");
                            e.printStackTrace();
                        }
                    }

                    MatOfByte buffer = new MatOfByte();
                    Imgcodecs.imencode(".png", frame, buffer);
                    Image image = new Image(new ByteArrayInputStream(buffer.toArray()));

                    Platform.runLater(() -> {
                        webcamView.setImage(image);
                    });

                    Thread.sleep(30);
                }

                if (capture != null) {
                    capture.release();
                }

                return null;
            }
        };

        cameraTask.setOnSucceeded(e -> {
            isCameraActive = false;
            faceLoginButton.setText("FACE LOGIN");
        });

        cameraTask.setOnFailed(e -> {
            isCameraActive = false;
            faceLoginButton.setText("FACE LOGIN");
            errorLabel.setText("Camera error: " + cameraTask.getException().getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("FACE_LOGIN_ERROR", emailField.getText().trim(), "Camera error: " + cameraTask.getException().getMessage(), "ERROR");
            cameraTask.getException().printStackTrace();
        });

        Thread th = new Thread(cameraTask);
        th.setDaemon(true);
        th.start();
    }

    private void stopCamera() {
        if (cameraTask != null && isCameraActive) {
            stopCamera.set(true);
            faceLoginButton.setText("FACE LOGIN");
            isCameraActive = false;
            if (capture != null && capture.isOpened()) {
                capture.release();
                capture = null;
                System.out.println("Camera released");
            }
        }
    }

    private String findBestFaceMatch(Mat capturedFace) throws SQLException {
        String matchedEmail = null;
        double bestSimilarity = 0.15; // Minimum threshold is 15% (0.15)

        String sql = "SELECT id, email, face_data FROM user WHERE face_data IS NOT NULL";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String email = rs.getString("email");
                byte[] faceImageData = rs.getBytes("face_data");

                if (faceImageData != null) {
                    Mat storedFace = Imgcodecs.imdecode(new MatOfByte(faceImageData), Imgcodecs.IMREAD_GRAYSCALE);
                    if (storedFace.size().width != 100 || storedFace.size().height != 100) {
                        Imgproc.resize(storedFace, storedFace, new Size(100, 100));
                    }

                    double similarity = compareFaces(capturedFace, storedFace);
                    System.out.println("Comparing with " + email + ": Similarity = " + String.format("%.2f", similarity * 100) + "%");

                    final double finalSimilarity = similarity;
                    final String finalEmail = email;
                    Platform.runLater(() -> {
                        errorLabel.setText("Comparing with " + finalEmail + ": " + String.format("%.2f", finalSimilarity * 100) + "%");
                        errorLabel.setVisible(true);
                    });

                    // Accept the highest similarity above 15%
                    if (similarity > 0.15 && similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        matchedEmail = email;
                    }
                }
            }
        }

        if (matchedEmail == null) {
            System.out.println("No face match found above 15%");
            Platform.runLater(() -> {
                errorLabel.setText("No face match found above 15%");
                errorLabel.setVisible(true);
            });
        } else {
            System.out.println("Best match: " + matchedEmail + " with similarity " + String.format("%.2f", bestSimilarity * 100) + "%");
        }

        return matchedEmail;
    }
    private double compareFaces(Mat face1, Mat face2) {
        Mat result = new Mat();
        Mat normalizedFace1 = new Mat();
        Mat normalizedFace2 = new Mat();
        Core.normalize(face1, normalizedFace1, 0, 255, Core.NORM_MINMAX);
        Core.normalize(face2, normalizedFace2, 0, 255, Core.NORM_MINMAX);
        Imgproc.matchTemplate(normalizedFace1, normalizedFace2, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        return mmr.maxVal;
    }

    private boolean verifyCredentials(String email, String password) throws SQLException {
        String sql = "SELECT password FROM user WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                try {
                    if (verifyPassword(password, storedPassword)) {
                        updateLoginInfo(email);
                        logAuthEvent("LOGIN", email, "Successful password authentication", "SUCCESS");
                        return true;
                    }
                    logAuthEvent("LOGIN_FAILED", email, "Invalid password", "FAILURE");
                    logFailedLogin(email, getClientIp(), "Invalid password");
                    return false;
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    logAuthEvent("LOGIN_ERROR", email, "Password verification error: " + e.getMessage(), "ERROR");
                    throw new SQLException("Password verification error: " + e.getMessage());
                }
            }
            logAuthEvent("LOGIN_FAILED", email, "Email not found", "FAILURE");
            logFailedLogin(email, getClientIp(), "Email not found");
            return false;
        }
    }

    private void updateLoginInfo(String email) throws SQLException {
        String sql = "UPDATE user SET login_count = login_count + 1, last_login = NOW(), last_login_ip = ? WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String clientIp = getClientIp();
            pstmt.setString(1, clientIp);
            pstmt.setString(2, email);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update login info for email: " + email);
            }
        }
    }

    private String getClientIp() {
        return "127.0.0.1"; // Replace with actual IP retrieval logic
    }

    private void logFailedLogin(String email, String ip, String reason) throws SQLException {
        String sql = "CALL log_failed_login(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, ip);
            pstmt.setString(3, reason);
            pstmt.execute();
        }
    }

    private void logAuthEvent(String action, String email, String details, String status) {
        System.out.println("Attempting to log event: action=" + action + ", email=" + (email != null ? email : "null") + ", status=" + status);
        String sql = "INSERT INTO audit_log (action, user_email, details, ip_address, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(true);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, action != null ? action : "UNKNOWN");
                pstmt.setString(2, email); // Can be null
                pstmt.setString(3, details != null ? details : "");
                pstmt.setString(4, getClientIp() != null ? getClientIp() : "0.0.0.0");
                pstmt.setString(5, status != null ? status : "UNKNOWN");
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.err.println("No rows inserted into audit_log for action: " + action);
                    Platform.runLater(() -> {
                        errorLabel.setText("Failed to log audit event: " + action);
                        errorLabel.setVisible(true);
                    });
                } else {
                    System.out.println("Audit event logged successfully: action=" + action + ", email=" + (email != null ? email : "null") + ", status=" + status);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Failed to log auth event: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                errorLabel.setText("Audit log error: " + e.getMessage());
                errorLabel.setVisible(true);
            });
        }
    }
    private boolean verifyPassword(String inputPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] parts = storedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        byte[] hash = Base64.getDecoder().decode(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);

        PBEKeySpec spec = new PBEKeySpec(inputPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        if (hash.length != testHash.length) {
            return false;
        }
        for (int i = 0; i < hash.length; i++) {
            if (hash[i] != testHash[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isMfaEnabled(String email) {
        String sql = "SELECT is_mfa_enabled FROM user WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_mfa_enabled");
            }
            return false;
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("MFA_CHECK_ERROR", email, "Database error: " + e.getMessage(), "ERROR");
            return false;
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

    private void showMfaVerificationScreen(String email, String verificationCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MfaScreen.fxml"));
            Parent root = loader.load();
            MfaController controller = loader.getController();
            controller.initialize(email, verificationCode);
            Stage stage = (Stage) emailField.getScene().getWindow();
            controller.setStage(stage);
            stage.setScene(new Scene(root, 500, 400));
            stage.setTitle("Two-Factor Authentication");
        } catch (IOException e) {
            errorLabel.setText("Error loading MFA verification screen: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("MFA_SCREEN_ERROR", email, "Error loading MFA verification screen: " + e.getMessage(), "ERROR");
        }
    }

    private void showHomeScreen() {
        try {
            String email = emailField.getText().trim();
            Stage stage = (Stage) emailField.getScene().getWindow();

            if ("Admin@datafarm.com".equalsIgnoreCase(email)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
                Parent root = loader.load();
                stage.setScene(new Scene(root, 900, 600));
                stage.setTitle("Admin Dashboard");
                logAuthEvent("ADMIN_LOGIN", email, "Redirected to Admin Dashboard", "SUCCESS");
                System.out.println("Redirected to Admin Dashboard for: " + email);
            } else {
                VBox root = new VBox(20);
                root.setAlignment(Pos.CENTER);
                root.setPadding(new Insets(20));
                Label successLabel = new Label("Login Successful for " + email);
                successLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                root.getChildren().add(successLabel);
                stage.setScene(new Scene(root, 800, 600));
                stage.setTitle("Home");
                logAuthEvent("USER_LOGIN", email, "Displayed home screen", "SUCCESS");
                System.out.println("Displayed home screen for: " + email);
            }
        } catch (IOException e) {
            errorLabel.setText("Error loading screen: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("SCREEN_LOAD_ERROR", emailField.getText().trim(), "Error loading screen: " + e.getMessage(), "ERROR");
            e.printStackTrace();
        } catch (Exception e) {
            errorLabel.setText("Unexpected error: " + e.getMessage());
            errorLabel.setVisible(true);
            logAuthEvent("UNEXPECTED_ERROR", emailField.getText().trim(), "Unexpected error: " + e.getMessage(), "ERROR");
            e.printStackTrace();
        }
    }

    public void logLogout(String email) {
        logAuthEvent("LOGOUT", email, "User logged out", "INFO");
    }

    @Override
    protected void finalize() {
        stopCamera();
    }

    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            stopCamera();
            logAuthEvent("APP_CLOSE", emailField.getText().trim(), "Application closed, camera stopped", "INFO");
            System.out.println("Application closing, camera stopped");
        });
    }
}