package controllers;

import entities.CollecteDechet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CollecteService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class ModifierCollecteController {

    @FXML private ComboBox<String> typeDechetField;
    @FXML private TextField quantiteField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField imageUrlField;

    private CollecteDechet collecte;
    private final CollecteService collecteService = new CollecteService();

    public void initialize() {
        typeDechetField.getItems().addAll("Fumier", "Paille", "Déchets végétaux", "Coques de fruits", "Autre");
    }

    public void initData(CollecteDechet collecte) {
        this.collecte = collecte;
        typeDechetField.setValue(collecte.getTypeDechet());
        quantiteField.setText(String.valueOf(collecte.getQuantite()));
        dateDebutPicker.setValue(collecte.getDateDebut());
        dateFinPicker.setValue(collecte.getDateFin());
        imageUrlField.setText(collecte.getImageUrl());
    }

    @FXML
    private void enregistrerCollecte(ActionEvent event) {
        String typeDechet = typeDechetField.getValue();
        String quantiteText = quantiteField.getText();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String imageUrl = imageUrlField.getText();

        if (typeDechet == null || typeDechet.trim().isEmpty()) {
            showAlert("Erreur", "Le type de déchet est obligatoire.");
            return;
        }

        if (quantiteText == null || quantiteText.trim().isEmpty()) {
            showAlert("Erreur", "La quantité est obligatoire.");
            return;
        }

        double quantite;
        try {
            quantite = Double.parseDouble(quantiteText);
            if (quantite <= 0) {
                showAlert("Erreur", "La quantité doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La quantité doit être un nombre valide.");
            return;
        }

        if (dateDebut == null) {
            showAlert("Erreur", "La date de début est obligatoire.");
            return;
        }

        if (dateFin == null) {
            showAlert("Erreur", "La date de fin est obligatoire.");
            return;
        }

        if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur", "La date de début ne peut pas être après la date de fin.");
            return;
        }

        // Gestion de l’image
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (!(imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") || imageUrl.endsWith(".png"))) {
                showAlert("Erreur", "L'image doit être au format JPG, JPEG ou PNG.");
                return;
            }

            String destinationFolder = System.getProperty("user.dir") + "/images/";
            File sourceFile = new File(imageUrl);
            File destFile = new File(destinationFolder + sourceFile.getName());

            File destinationDir = new File(destinationFolder);
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }

            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imageUrl = "images/" + sourceFile.getName();
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de copier l'image : " + e.getMessage());
                return;
            }
        }

        // Mise à jour de l'entité
        collecte.setTypeDechet(typeDechet);
        collecte.setQuantite(quantite);
        collecte.setDateDebut(dateDebut);
        collecte.setDateFin(dateFin);
        collecte.setImageUrl(imageUrl);

        collecteService.updateCollecte(collecte);
        showAlert("Succès", "Collecte modifiée avec succès.");

        // Fermer la fenêtre
        Stage stage = (Stage) typeDechetField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void annuler(ActionEvent event) {
        Stage stage = (Stage) typeDechetField.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void choisirImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(imageFilter);

        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            imageUrlField.setText(file.getAbsolutePath());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
