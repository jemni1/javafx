package edu.connexion3a41.App;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> surnameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> cinColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField cinField;

    @FXML
    private TextField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private CheckBox mfaCheckBox;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView profilePictureView; // New field for ImageView

    @FXML
    private Button uploadPictureButton; // New field for upload button

    private static final String URL = "jdbc:mysql://localhost:3306/data?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser = null;
    private String profilePicturePath = null; // Store the path of the uploaded image

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Initialize ComboBox
        roleComboBox.setItems(FXCollections.observableArrayList("ROLE_CLIENT", "ROLE_WORKER", "ROLE_AGRICULTURE", "ROLE_ADMIN"));

        // Load users from database
        loadUsers();

        // Add listener for table selection
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                populateFields(selectedUser);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                clearFields();
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        // Set default profile picture with error handling
        try {
            InputStream imageStream = getClass().getResourceAsStream("/edu/connexion3a41/App/Connect.png");
            if (imageStream == null) {
                statusLabel.setText("Error: Default profile picture not found");
                profilePictureView.setImage(null); // Or set a fallback image
            } else {
                profilePictureView.setImage(new Image(imageStream));
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading default profile picture: " + e.getMessage());
            profilePictureView.setImage(null); // Or set a fallback image
        }
    }
    private void loadUsers() {
        userList.clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM user WHERE email != 'Admin@datafarm.com'")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String cin = rs.getString("cin");
                String roles = rs.getString("roles");
                boolean isMfaEnabled = rs.getBoolean("is_mfa_enabled");
                String profilePicture = rs.getString("profile_picture"); // New field

                // Extract role from JSON array format
                String role = roles;
                if (roles != null && roles.contains("ROLE_")) {
                    role = roles.replaceAll("\\[|\\]|\"", "");
                }

                User user = new User(id, name, surname, email, username, cin, "", role, isMfaEnabled, profilePicture);
                userList.add(user);
            }

            userTable.setItems(userList);
            statusLabel.setText("Loaded " + userList.size() + " users");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading users: " + e.getMessage());
        }
    }
    private void loadDefaultProfilePicture() {
        try {
            InputStream imageStream = getClass().getResourceAsStream("/edu/connexion3a41/App/Connect.jpg");
            if (imageStream != null) {
                profilePictureView.setImage(new Image(imageStream));
            } else {
                statusLabel.setText("Error: Default profile picture not found");
                profilePictureView.setImage(null); // Or set a fallback image
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading default profile picture: " + e.getMessage());
            profilePictureView.setImage(null); // Or set a fallback image
        }
    }
    private void populateFields(User user) {
        nameField.setText(user.getName());
        surnameField.setText(user.getSurname());
        emailField.setText(user.getEmail());
        usernameField.setText(user.getUsername());
        cinField.setText(user.getCin());
        passwordField.setText(""); // Don't show password
        roleComboBox.setValue(user.getRole());
        mfaCheckBox.setSelected(user.isMfaEnabled());

        // Load profile picture
        profilePicturePath = user.getProfilePicture();
        if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
            try {
                File imageFile = new File(profilePicturePath);
                if (imageFile.exists()) {
                    profilePictureView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    // Load default image if the file doesn't exist
                    loadDefaultProfilePicture();
                }
            } catch (Exception e) {
                // Load default image if there's an error
                loadDefaultProfilePicture();
            }
        } else {
            // Load default image if profilePicturePath is null or empty
            loadDefaultProfilePicture();
        }
    }
    private void clearFields() {
        nameField.clear();
        surnameField.clear();
        emailField.clear();
        usernameField.clear();
        cinField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        mfaCheckBox.setSelected(false);
        profilePictureView.setImage(new Image(getClass().getResourceAsStream("/Connect.png")));
        profilePicturePath = null;
        selectedUser = null;
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        if (validateInput()) {
            try {
                String sql = "INSERT INTO user (name, surname, email, username, cin, password, is_mfa_enabled, roles, profile_picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    pstmt.setString(1, nameField.getText().trim());
                    pstmt.setString(2, surnameField.getText().trim());
                    pstmt.setString(3, emailField.getText().trim());
                    pstmt.setString(4, usernameField.getText().trim());
                    pstmt.setString(5, cinField.getText().trim());
                    pstmt.setString(6, passwordField.getText());
                    pstmt.setBoolean(7, mfaCheckBox.isSelected());
                    pstmt.setString(8, "[\"" + roleComboBox.getValue() + "\"]");
                    pstmt.setString(9, profilePicturePath);

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        ResultSet generatedKeys = pstmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            User newUser = new User(
                                    id,
                                    nameField.getText().trim(),
                                    surnameField.getText().trim(),
                                    emailField.getText().trim(),
                                    usernameField.getText().trim(),
                                    cinField.getText().trim(),
                                    "",
                                    roleComboBox.getValue(),
                                    mfaCheckBox.isSelected(),
                                    profilePicturePath
                            );
                            userList.add(newUser);
                            clearFields();
                            statusLabel.setText("User added successfully");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error adding user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUpdateUser(ActionEvent event) {
        if (selectedUser != null && validateInput()) {
            try {
                String sql = "UPDATE user SET name=?, surname=?, email=?, username=?, cin=?, is_mfa_enabled=?, roles=?, profile_picture=?";

                // Only update password if a new one is provided
                if (!passwordField.getText().isEmpty()) {
                    sql += ", password=?";
                }

                sql += " WHERE id=?";

                try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, nameField.getText().trim());
                    pstmt.setString(2, surnameField.getText().trim());
                    pstmt.setString(3, emailField.getText().trim());
                    pstmt.setString(4, usernameField.getText().trim());
                    pstmt.setString(5, cinField.getText().trim());
                    pstmt.setBoolean(6, mfaCheckBox.isSelected());
                    pstmt.setString(7, "[\"" + roleComboBox.getValue() + "\"]");
                    pstmt.setString(8, profilePicturePath);

                    if (!passwordField.getText().isEmpty()) {
                        pstmt.setString(9, passwordField.getText());
                        pstmt.setInt(10, selectedUser.getId());
                    } else {
                        pstmt.setInt(9, selectedUser.getId());
                    }

                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        // Update the user in the list
                        selectedUser.setName(nameField.getText().trim());
                        selectedUser.setSurname(surnameField.getText().trim());
                        selectedUser.setEmail(emailField.getText().trim());
                        selectedUser.setUsername(usernameField.getText().trim());
                        selectedUser.setCin(cinField.getText().trim());
                        selectedUser.setRole(roleComboBox.getValue());
                        selectedUser.setMfaEnabled(mfaCheckBox.isSelected());
                        selectedUser.setProfilePicture(profilePicturePath);

                        userTable.refresh();
                        clearFields();
                        statusLabel.setText("User updated successfully");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error updating user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete User");
            alert.setHeaderText("Confirm Delete");
            alert.setContentText("Are you sure you want to delete the user: " + selectedUser.getUsername() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    String sql = "DELETE FROM user WHERE id=?";

                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setInt(1, selectedUser.getId());
                        int affectedRows = pstmt.executeUpdate();

                        if (affectedRows > 0) {
                            userList.remove(selectedUser);
                            clearFields();
                            statusLabel.setText("User deleted successfully");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    statusLabel.setText("Error deleting user: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleClearFields(ActionEvent event) {
        clearFields();
        userTable.getSelectionModel().clearSelection();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Login");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading login screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Super Admin Dashboard");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadPicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(userTable.getScene().getWindow());
        if (selectedFile != null) {
            profilePicturePath = selectedFile.getAbsolutePath();
            profilePictureView.setImage(new Image(selectedFile.toURI().toString()));
            statusLabel.setText("Profile picture selected");
        }
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            statusLabel.setText("Name cannot be empty");
            return false;
        }

        if (surnameField.getText().trim().isEmpty()) {
            statusLabel.setText("Surname cannot be empty");
            return false;
        }

        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            statusLabel.setText("Enter a valid email address");
            return false;
        }

        if (usernameField.getText().trim().isEmpty()) {
            statusLabel.setText("Username cannot be empty");
            return false;
        }

        if (cinField.getText().trim().isEmpty() || !cinField.getText().matches("\\d{8}")) {
            statusLabel.setText("CIN must be an 8-digit number");
            return false;
        }

        // Only check password when adding a new user
        if (selectedUser == null && passwordField.getText().isEmpty()) {
            statusLabel.setText("Password cannot be empty for new users");
            return false;
        }

        if (roleComboBox.getValue() == null) {
            statusLabel.setText("Please select a role");
            return false;
        }

        return true;
    }
}