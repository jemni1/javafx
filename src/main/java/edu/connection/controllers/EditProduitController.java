package edu.connection.controllers;

import edu.connection.entities.Produit;
import edu.connection.services.ProduitService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class EditProduitController {

    @FXML private TextField nomField;
    @FXML private TextField quantiteField;
    @FXML private TextField prixField;
    @FXML private TextField imageField;

    private Produit produit;
    private final ProduitService produitService = new ProduitService();
    private Runnable onUpdateCallback;

    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        nomField.setText(produit.getNom());
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        prixField.setText(String.valueOf(produit.getPrix()));
        imageField.setText(produit.getImage());
    }

    @FXML
    private void enregistrerModification() {
        // 🔍 Validation
        if (nomField.getText().isEmpty() || quantiteField.getText().isEmpty() ||
                prixField.getText().isEmpty() || imageField.getText().isEmpty()) {
            showAlert("Tous les champs sont obligatoires.");
            return;
        }
        String nom = nomField.getText().trim();

        if (!nom.matches("[a-zA-ZÀ-ÿ\\s'-]+")) {
            showAlert("Le nom du produit doit contenir uniquement des lettres.");
            return;
        }
        int quantite, prix;
        try {
            quantite = Integer.parseInt(quantiteField.getText());
            prix = Integer.parseInt(prixField.getText());
        } catch (NumberFormatException e) {
            showAlert("Quantité et prix doivent être des nombres entiers.");
            return;
        }

        // ✔️ Mise à jour du produit
        produit.setNom(nomField.getText());
        produit.setQuantite(quantite);
        produit.setPrix(prix);
        produit.setImage(imageField.getText());

        produitService.updateEntity(produit, produit.getId());

        // 👈 Retour à la liste des produits
        if (onUpdateCallback != null) {
            onUpdateCallback.run(); // Rafraîchir dynamiquement
        }

        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close(); // Fermer la popup
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de produit");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Dossier où copier l'image
                File destinationDir = new File("images/");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs(); // crée le dossier s'il n'existe pas
                }

                // Nom unique pour éviter les doublons
                String uniqueName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destinationFile = new File(destinationDir, uniqueName);

                // Copier le fichier
                java.nio.file.Files.copy(
                        selectedFile.toPath(),
                        destinationFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                // Stocker uniquement le nom du fichier ou son chemin relatif
                produit.setImage( uniqueName); // Enregistrer le chemin relatif de l'image
                imageField.setText( uniqueName); // Afficher le chemin dans le champ texte

            } catch (Exception e) {
                showAlert("Erreur lors de la copie de l'image : " + e.getMessage());
            }
        } else {
            imageField.setText("Aucun fichier sélectionné");
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
