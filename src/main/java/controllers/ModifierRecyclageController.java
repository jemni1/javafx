package controllers;

import entities.CollecteDechet;
import entities.RecyclageDechet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CollecteService;
import services.RecyclageService;
import tools.DatabaseConnection;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifierRecyclageController {

    @FXML
    private TextField quantiteRecyclageField;
    @FXML
    private TextField energieProduiteField;
    @FXML
    private ComboBox<String> utilisationEnergieField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private ListView<String> collecteListView;
    @FXML
    private Button imageButton;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField searchField; // Added search field for filtering

    private RecyclageDechet recyclageDechet;
    private RecyclageService recyclageService;
    private CollecteService collecteService;
    private String selectedImagePath;
    private String keyword;

    public void initialize() throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        recyclageService = RecyclageService.getInstance();
        collecteService = new CollecteService();

        utilisationEnergieField.getItems().addAll(
                "Irrigation", "Chauffage", "Alimentation", "Stockage d’énergie", "Autre"
        );

        // Load all collectes and set them in the ListView
        chargerCollectes();

        // Add listener to the search field for filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterCollectes(newValue));
    }

    // Method to filter collectes based on the search keyword
    public void filterCollectes(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            chargerCollectes(); // If no search term, show all collectes
        } else {
            String keywordLower = keyword.toLowerCase();
            List<CollecteDechet> allCollectes = collecteService.getAllCollectes();
            collecteListView.getItems().clear();
            for (CollecteDechet collecte : allCollectes) {
                if (collecte.getTypeDechet().toLowerCase().contains(keywordLower)) {
                    collecteListView.getItems().add(
                            collecte.getTypeDechet() + " - " + collecte.getQuantite() + " kg"
                    );
                }
            }
        }
    }

    private void chargerCollectes() {
        List<CollecteDechet> collectes = collecteService.getCollectesSansRecyclage();
        collecteListView.getItems().clear();
        for (CollecteDechet collecte : collectes) {
            collecteListView.getItems().add(
                    collecte.getTypeDechet() + " - " + collecte.getQuantite() + " kg"
            );
        }
    }

    @FXML
    public void choisirImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(imageButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            imageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    public void annuler(ActionEvent actionEvent) {
        Stage stage = (Stage) imageButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void enregistrerRecyclage(ActionEvent actionEvent) {
        try {
            double quantiteRecyclage = Double.parseDouble(quantiteRecyclageField.getText());
            double energieProduite = Double.parseDouble(energieProduiteField.getText());
            String utilisation = utilisationEnergieField.getValue();
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();

            if (utilisation == null || dateDebut == null || dateFin == null) {
                showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs.");
                return;
            }

            // Mise à jour de l'entité recyclageDechet
            recyclageDechet.setQuantiteRecyclage(quantiteRecyclage);
            recyclageDechet.setEnergieProduite(energieProduite);
            recyclageDechet.setUtilisation(utilisation);
            recyclageDechet.setDateDebut(dateDebut);
            recyclageDechet.setDateFin(dateFin);
            recyclageDechet.setImageUrl(selectedImagePath);

            // Mise à jour du recyclage avec collectes
            recyclageService.mettreAJourRecyclageAvecCollectes(recyclageDechet);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le recyclage a été modifié avec succès.");
            Stage stage = (Stage) imageButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez entrer des valeurs numériques valides.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", "Échec de la mise à jour dans la base de données.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setRecyclageDechet(RecyclageDechet selectedRecyclage) {
        this.recyclageDechet = selectedRecyclage;

        // Set the fields with the values from the selected recyclage
        quantiteRecyclageField.setText(String.valueOf(selectedRecyclage.getQuantiteRecyclage()));
        energieProduiteField.setText(String.valueOf(selectedRecyclage.getEnergieProduite()));
        utilisationEnergieField.setValue(selectedRecyclage.getUtilisation());
        dateDebutPicker.setValue(selectedRecyclage.getDateDebut());
        dateFinPicker.setValue(selectedRecyclage.getDateFin());

        selectedImagePath = selectedRecyclage.getImageUrl();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            File file = new File(selectedImagePath);
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    public void filterCollectes(KeyEvent keyEvent) {
        final String keywordLower = keyword.toLowerCase(); // Ensure the keyword is final

        List<CollecteDechet> allCollectes = collecteService.getAllCollectes(); // Effectively final
        collecteListView.getItems().setAll(
                String.valueOf(allCollectes.stream()
                        .filter(c -> c.getTypeDechet().toLowerCase().contains(keywordLower)) // Use the modified keyword
                        .toList())
        );
    }
}
