package edu.connexion3a41.App;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private PieChart rolePieChart;

    @FXML
    private TableView<AuditLog> auditLogTable;

    @FXML
    private TableColumn<AuditLog, String> actionColumn;

    @FXML
    private TableColumn<AuditLog, String> userEmailColumn;

    @FXML
    private TableColumn<AuditLog, Timestamp> dateColumn;

    @FXML
    private TableColumn<AuditLog, String> detailsColumn;

    @FXML
    private TableColumn<AuditLog, Integer> loginCountColumn;

    @FXML
    private TableColumn<AuditLog, Timestamp> lastLoginColumn;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label activeConnectionsLabel;

    @FXML
    private Label totalLoginsLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ImageView dashboardIcon;

    @FXML
    private ImageView usersIcon;

    @FXML
    private ImageView connectionsIcon;

    @FXML
    private ImageView loginsIcon;

    @FXML
    private ImageView backIcon;

    private static final String URL = "jdbc:mysql://localhost:3306/data";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private ObservableList<AuditLog> auditLogList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize audit log table columns
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        loginCountColumn.setCellValueFactory(new PropertyValueFactory<>("loginCount"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

        // Load statistics and audit logs
        loadStatistics();
        loadAuditLogs();

        // Add search functionality
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> {
                filterAuditLogs(newValue);
            });
        }

        // Load images programmatically
        try {
            if (dashboardIcon != null) {
                dashboardIcon.setImage(new Image(getClass().getResourceAsStream("/Dashboard.png")));
            }
            if (usersIcon != null) {
                usersIcon.setImage(new Image(getClass().getResourceAsStream("/User.png")));
            }
            if (connectionsIcon != null) {
                connectionsIcon.setImage(new Image(getClass().getResourceAsStream("/edu/connexion3a41/App/Connect.jpg")));
            }
            if (loginsIcon != null) {
                loginsIcon.setImage(new Image(getClass().getResourceAsStream("/Logins.png")));
            }
            if (backIcon != null) {
                backIcon.setImage(new Image(getClass().getResourceAsStream("/Back.png")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            totalUsersLabel.setText("Error loading images: " + e.getMessage());
        }
    }

    public void createAuditLog(String action, String userEmail, String details, String ipAddress, String status) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO audit_log (action, user_email, date, details, ip_address, status) " +
                             "VALUES (?, ?, NOW(), ?, ?, ?)")) {

            pstmt.setString(1, action);
            pstmt.setString(2, userEmail);
            pstmt.setString(3, details);
            pstmt.setString(4, ipAddress);
            pstmt.setString(5, status);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Total users
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM user WHERE email != 'Admin@datafarm.com'");
            if (rs.next()) {
                totalUsersLabel.setText("Total Users: " + rs.getInt(1));
            }

            // Active connections (assuming is_mfa_enabled as a proxy for active users)
            rs = stmt.executeQuery("SELECT COUNT(*) FROM user WHERE is_mfa_enabled = true");
            if (rs.next()) {
                activeConnectionsLabel.setText("Active Connections: " + rs.getInt(1));
            }

            // Total login attempts
            rs = stmt.executeQuery("SELECT COUNT(*) FROM audit_log WHERE action = 'LOGIN'");
            if (rs.next()) {
                totalLoginsLabel.setText("Total Logins: " + rs.getInt(1));
            }

            // Role distribution - with improved handling
            ObservableList<PieChart.Data> roleData = FXCollections.observableArrayList();
            rs = stmt.executeQuery("SELECT roles, COUNT(*) as count FROM user WHERE email != 'Admin@datafarm.com' GROUP BY roles");

            int totalUsers = 0;
            Map<String, Integer> roleCounts = new HashMap<>();

            // First pass to collect data and calculate total
            while (rs.next()) {
                String role = rs.getString("roles").replaceAll("\\[|\\]|\"", "");
                int count = rs.getInt("count");
                if (count > 0) {  // Only consider roles with users
                    roleCounts.put(role, count);
                    totalUsers += count;
                }
            }

            // Second pass to create pie chart data without combining small slices
            for (Map.Entry<String, Integer> entry : roleCounts.entrySet()) {
                String role = entry.getKey();
                int count = entry.getValue();
                roleData.add(new PieChart.Data(role, count));
            }

            rolePieChart.setData(roleData);
            rolePieChart.setTitle("User Role Distribution");

            // Configure pie chart appearance
            rolePieChart.setLabelsVisible(true);
            rolePieChart.setLegendVisible(true);
            rolePieChart.setLabelLineLength(15);

            // Apply custom colors for better visual distinction (optional)
            String[] pieColors = {
                    "rgba(231, 76, 60, 1.0)",
                    "rgba(52, 152, 219, 1.0)",
                    "rgba(46, 204, 113, 1.0)",
                    "rgba(241, 196, 15, 1.0)",
                    "rgba(155, 89, 182, 1.0)"
            };

            // Add visible labels with percentage for ALL slices
            int colorIndex = 0;
            for (final PieChart.Data data : rolePieChart.getData()) {
                double percentage = (data.getPieValue() / totalUsers) * 100;
                String percentageText = String.format("%.1f%%", percentage);

                // Apply custom colors (optional)
                if (colorIndex < pieColors.length) {
                    data.getNode().setStyle("-fx-pie-color: " + pieColors[colorIndex]);
                    colorIndex++;
                }

                // Add event listener to display labels after chart is rendered
                Platform.runLater(() -> {
                    // Create a text node for label
                    Text dataText = new Text(data.getName() + ": " + percentageText);
                    dataText.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

                    // Create container for placing label
                    StackPane labelContainer = new StackPane();
                    labelContainer.getChildren().add(dataText);
                    labelContainer.setTranslateX(data.getNode().getBoundsInParent().getCenterX());
                    labelContainer.setTranslateY(data.getNode().getBoundsInParent().getCenterY() - 30);

                    // Add label directly to the chart's scene without casting the parent
                    // First check if the pie chart is in a scene
                    if (rolePieChart.getScene() != null) {
                        Pane chartParent = (Pane) rolePieChart.getParent();
                        chartParent.getChildren().add(labelContainer);
                    }

                    // Create a tooltip for more details
                    Tooltip tooltip = new Tooltip(data.getName() + ": " + (int)data.getPieValue() + " users (" + percentageText + ")");
                    Tooltip.install(data.getNode(), tooltip);

                    // Add a click event to highlight the segment (optional)
                    data.getNode().setOnMouseClicked(event -> {
                        for (PieChart.Data d : rolePieChart.getData()) {
                            d.getNode().setScaleX(1.0);
                            d.getNode().setScaleY(1.0);
                        }
                        data.getNode().setScaleX(1.1);
                        data.getNode().setScaleY(1.1);
                    });
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            totalUsersLabel.setText("Error loading stats: " + e.getMessage());
        }
    }
    private void loadAuditLogs() {
        auditLogList.clear();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT al.action, al.user_email, al.date, al.details, " +
                             "u.roles, u.is_mfa_enabled, " +
                             "u.login_count, u.last_login " +
                             "FROM audit_log al " +
                             "LEFT JOIN user u ON al.user_email = u.email " +
                             "ORDER BY al.date DESC")) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String action = rs.getString("action");
                String userEmail = rs.getString("user_email");
                String roles = rs.getString("roles") != null ?
                        rs.getString("roles").replaceAll("\\[|\\]|\"", "") : "N/A";
                boolean isActive = rs.getBoolean("is_mfa_enabled");
                String details = rs.getString("details");

                // Enhance details with more information
                String enhancedDetails = String.format(
                        "User: %s | Role: %s | Status: %s | Action: %s | Details: %s",
                        userEmail,
                        roles,
                        isActive ? "Active" : "Inactive",
                        action,
                        details != null ? details : "No additional details"
                );

                AuditLog log = new AuditLog(
                        action,
                        userEmail,
                        rs.getTimestamp("date"),
                        enhancedDetails,
                        rs.getInt("login_count"),
                        rs.getTimestamp("last_login")
                );
                auditLogList.add(log);
            }

            auditLogTable.setItems(auditLogList);

        } catch (SQLException e) {
            e.printStackTrace();
            totalUsersLabel.setText("Error loading audit logs: " + e.getMessage());
        }
    }

    private void filterAuditLogs(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            auditLogTable.setItems(auditLogList);
        } else {
            ObservableList<AuditLog> filteredList = FXCollections.observableArrayList();
            String lowerCaseFilter = searchText.toLowerCase();
            for (AuditLog log : auditLogList) {
                if (log.getUserEmail().toLowerCase().contains(lowerCaseFilter) ||
                        log.getAction().toLowerCase().contains(lowerCaseFilter) ||
                        log.getDetails().toLowerCase().contains(lowerCaseFilter)) {
                    filteredList.add(log);
                }
            }
            auditLogTable.setItems(filteredList);
        }
    }

    @FXML
    private void handleBackToAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminScreen.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rolePieChart.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Admin Panel");

        } catch (IOException e) {
            e.printStackTrace();
            totalUsersLabel.setText("Error loading admin panel: " + e.getMessage());
        }
    }

    public static class AuditLog {
        private final String action;
        private final String userEmail;
        private final Timestamp date;
        private final String details;
        private final int loginCount;
        private final Timestamp lastLogin;

        public AuditLog(String action, String userEmail, Timestamp date, String details, int loginCount, Timestamp lastLogin) {
            this.action = action;
            this.userEmail = userEmail;
            this.date = date;
            this.details = details;
            this.loginCount = loginCount;
            this.lastLogin = lastLogin;
        }

        public String getAction() { return action; }
        public String getUserEmail() { return userEmail; }
        public Timestamp getDate() { return date; }
        public String getDetails() { return details; }
        public int getLoginCount() { return loginCount; }
        public Timestamp getLastLogin() { return lastLogin; }
    }
}