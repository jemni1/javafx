package edu.connexion3a41.App;
import edu.connexion3a41.App.LoginController;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RegisterScreenController {

    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField usernameField;
    @FXML private TextField cinField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> rolesCombo;
    @FXML private Button profilePicButton;
    @FXML private Label profilePicLabel;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckBox;
    @FXML private CheckBox mfaCheckBox;
    @FXML private Label messageLabel;

    private Stage stage;
    private File profilePicture;
    private static final String URL = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;

    @FXML
    private void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        profilePicLabel.setText("No file chosen");
        rolesCombo.getItems().addAll("Client", "Worker", "Agriculture");

        // Add real-time validation listeners
        addRealTimeValidation();
    }

    @FXML
    private void chooseProfilePicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        profilePicture = fileChooser.showOpenDialog(stage);

        if (profilePicture != null) {
            if (profilePicture.length() > 2 * 1024 * 1024) {
                messageLabel.setText("Profile picture exceeds 2MB limit. Please choose a smaller image.");
                messageLabel.setVisible(true);
                profilePicture = null;
                profilePicLabel.setText("No file chosen");
                return;
            }

            if (validateFace(profilePicture)) {
                profilePicLabel.setText(profilePicture.getName());
                messageLabel.setVisible(false); // Clear any previous error
            } else {
                messageLabel.setText("Profile picture must contain a detectable face.");
                messageLabel.setVisible(true);
                profilePicture = null;
                profilePicLabel.setText("No file chosen");
            }
        }
    }

    @FXML
    private void register(ActionEvent event) {
        // Clear previous errors
        clearErrors();

        String name = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        String username = usernameField.getText().trim();
        String cin = cinField.getText().trim();
        String email = emailField.getText().trim();
        String role = rolesCombo.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean mfaEnabled = mfaCheckBox.isSelected();

        if (!validateInputs(name, surname, username, cin, email, role, password, confirmPassword)) {
            return;
        }

        if (!termsCheckBox.isSelected()) {
            messageLabel.setText("You must agree to the terms and conditions.");
            messageLabel.setVisible(true);
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String checkSql = "SELECT COUNT(*) FROM user WHERE email = ? OR username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            checkStmt.setString(2, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                messageLabel.setText("Email or username already exists.");
                messageLabel.setVisible(true);
                return;
            }

            String sql = "INSERT INTO user (name, surname, username, CIN, email, roles, password, is_mfa_enabled, face_data, face_label) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, surname);
            pstmt.setString(3, username);
            pstmt.setString(4, cin);
            pstmt.setString(5, email);
            pstmt.setString(6, "[\"" + role + "\"]");
            pstmt.setString(7, hashPassword(password));
            pstmt.setBoolean(8, mfaEnabled);

            if (profilePicture != null) {
                Mat image = Imgcodecs.imread(profilePicture.getAbsolutePath());
                Mat grayImage = new Mat();
                Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
                Mat resizedImage = new Mat();
                Imgproc.resize(grayImage, resizedImage, new org.opencv.core.Size(100, 100));
                MatOfByte buffer = new MatOfByte();
                Imgcodecs.imencode(".png", resizedImage, buffer);
                pstmt.setBytes(9, buffer.toArray());
            } else {
                pstmt.setNull(9, Types.BLOB);
            }

            pstmt.setInt(10, generateUniqueFaceLabel());
            pstmt.executeUpdate();

            // Navigate to login screen after successful registration
            showLoginScreen(event);
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void showTermsDialog(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Terms and Conditions");
        alert.setHeaderText(null);
        alert.setContentText("Placeholder for terms and conditions.");
        alert.showAndWait();
    }

    @FXML
    private void showLoginScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // Get the stage from the event source
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set stage in LoginController
            LoginController controller = loader.getController();
            controller.setStage(currentStage);

            currentStage.setScene(new Scene(root, 800, 600));
            currentStage.setTitle("Login");
        } catch (IOException e) {
            messageLabel.setText("Error loading login screen: " + e.getMessage());
            messageLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    private boolean validateInputs(String name, String surname, String username, String cin, String email, String role, String password, String confirmPassword) {
        StringBuilder errors = new StringBuilder();

        if (name.length() < 2) {
            errors.append("• Name must be at least 2 characters.\n");
            nameField.setStyle("-fx-border-color: red;");
        }
        if (surname.length() < 2) {
            errors.append("• Surname must be at least 2 characters.\n");
            surnameField.setStyle("-fx-border-color: red;");
        }
        if (!username.matches("^(?=.*[0-9])[a-zA-Z0-9]{3,}$")) {
            errors.append("• Username must be 3+ characters and include a number.\n");
            usernameField.setStyle("-fx-border-color: red;");
        }
        if (!cin.matches("^\\d{8}$")) {
            errors.append("• CIN must be exactly 8 digits.\n");
            cinField.setStyle("-fx-border-color: red;");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.append("• Invalid email format.\n");
            emailField.setStyle("-fx-border-color: red;");
        }
        if (role == null) {
            errors.append("• Please select a role.\n");
            rolesCombo.setStyle("-fx-border-color: red;");
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$")) {
            errors.append("• Password must be 8+ characters with uppercase, lowercase, number, and symbol.\n");
            passwordField.setStyle("-fx-border-color: red;");
        }
        if (!password.equals(confirmPassword)) {
            errors.append("• Passwords do not match.\n");
            confirmPasswordField.setStyle("-fx-border-color: red;");
        }

        if (errors.length() > 0) {
            // CRITICAL: Force message to display
            messageLabel.setText(errors.toString());
            messageLabel.setVisible(true);

            // Add this line to debug in console
            System.out.println("VALIDATION ERRORS: " + errors.toString());

            return false;
        }
        return true;
    }
    private void setupMessageLabel() {
        // Make sure messageLabel exists and is visible when needed
        if (messageLabel == null) {
            System.out.println("ERROR: messageLabel is null - not properly linked in FXML");
        } else {
            // Ensure the label is properly styled to be very visible
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
            messageLabel.setWrapText(true);

            // Test message to verify it's working
            messageLabel.setText("Form validation ready.");
            messageLabel.setVisible(true);

            // Hide after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> {
                        messageLabel.setVisible(false);
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private boolean validateFace(File imageFile) {
        try {
            Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

            CascadeClassifier faceDetector = new CascadeClassifier("src/main/resources/haarcascade_frontalface_default.xml");
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(grayImage, faces);

            return faces.toArray().length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();

        return Base64.getEncoder().encodeToString(hash) + ":" + Base64.getEncoder().encodeToString(salt);
    }

    private int generateUniqueFaceLabel() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void clearErrors() {
        messageLabel.setText("");
        messageLabel.setVisible(false);
        // Reset field styles
        nameField.setStyle("");
        surnameField.setStyle("");
        usernameField.setStyle("");
        cinField.setStyle("");
        emailField.setStyle("");
        rolesCombo.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");
    }

    private void addRealTimeValidation() {
        // Name validation
        nameField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.trim().length() < 2) {
                nameField.setStyle("-fx-border-color: red;");
                messageLabel.setText("Name must be at least 2 characters.");
                messageLabel.setVisible(true);
            } else {
                nameField.setStyle("");
                if (messageLabel.getText().equals("Name must be at least 2 characters.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // Surname validation
        surnameField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.trim().length() < 2) {
                surnameField.setStyle("-fx-border-color: red;");
                messageLabel.setText("Surname must be at least 2 characters.");
                messageLabel.setVisible(true);
            } else {
                surnameField.setStyle("");
                if (messageLabel.getText().equals("Surname must be at least 2 characters.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // Username validation
        usernameField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.trim().matches("^(?=.*[0-9])[a-zA-Z0-9]{3,}$")) {
                usernameField.setStyle("-fx-border-color: red;");
                messageLabel.setText("Username must be 3+ characters and include a number.");
                messageLabel.setVisible(true);
            } else {
                usernameField.setStyle("");
                if (messageLabel.getText().equals("Username must be 3+ characters and include a number.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // CIN validation
        cinField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.trim().matches("^\\d{8}$")) {
                cinField.setStyle("-fx-border-color: red;");
                messageLabel.setText("CIN must be exactly 8 digits.");
                messageLabel.setVisible(true);
            } else {
                cinField.setStyle("");
                if (messageLabel.getText().equals("CIN must be exactly 8 digits.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // Email validation
        emailField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailField.setStyle("-fx-border-color: red;");
                messageLabel.setText("Invalid email format.");
                messageLabel.setVisible(true);
            } else {
                emailField.setStyle("");
                if (messageLabel.getText().equals("Invalid email format.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // Password validation
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$")) {
                passwordField.setStyle("-fx-border-color: red;");
                messageLabel.setText("Password must be 8+ characters with uppercase, lowercase, number, and symbol.");
                messageLabel.setVisible(true);
            } else {
                passwordField.setStyle("");
                if (messageLabel.getText().equals("Password must be 8+ characters with uppercase, lowercase, number, and symbol.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // Confirm Password validation
        confirmPasswordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.equals(passwordField.getText())) {
                confirmPasswordField.setStyle("-fx-border-color: red;");
                messageLabel.setText("Passwords do not match.");
                messageLabel.setVisible(true);
            } else {
                confirmPasswordField.setStyle("");
                if (messageLabel.getText().equals("Passwords do not match.")) {
                    messageLabel.setVisible(false);
                }
            }
        });

        // Role validation
        rolesCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                rolesCombo.setStyle("-fx-border-color: red;");
                messageLabel.setText("Please select a role.");
                messageLabel.setVisible(true);
            } else {
                rolesCombo.setStyle("");
                if (messageLabel.getText().equals("Please select a role.")) {
                    messageLabel.setVisible(false);
                }
            }
        });
    }
}