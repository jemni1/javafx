package edu.connection.controllers;

import edu.connection.entities.Produit;
import edu.connection.services.CurrencyService;
import edu.connection.services.ProduitService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;

public class ajoutProduit {

    @FXML
    private TextField nomProduitField;

    @FXML
    private TextField quantiteField;

    @FXML
    private Spinner<Integer> prixSpinner;

    @FXML
    private Label imageLabel;

    private String imagePath;

    private final ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        prixSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0));
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de produit");
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
                imagePath =  uniqueName;
                imageLabel.setText(selectedFile.getName());

            } catch (Exception e) {
                System.out.println("Erreur lors de la copie de l'image : " + e.getMessage());
            }
        } else {
            imageLabel.setText("Aucun fichier sélectionné");
        }
    }

    @FXML
    private void retourALaListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/listeProduits.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void envoyerProduit(ActionEvent event) {
        String nom = nomProduitField.getText();
        String quantiteText = quantiteField.getText();
        Integer prixOriginal = prixSpinner.getValue();

        if (nom == null || nom.trim().isEmpty()) {
            showAlert("Champ manquant", "Veuillez saisir le nom du produit.");
            return;
        }
        if (!nom.matches("[a-zA-ZÀ-ÿ\\s'-]+")) {
            showAlert("Nom invalide", "Le nom du produit doit contenir uniquement des lettres.");
            return;
        }
        if (quantiteText == null || quantiteText.trim().isEmpty()) {
            showAlert("Champ manquant", "Veuillez saisir la quantité.");
            return;
        }
        int quantite;
        try {
            quantite = Integer.parseInt(quantiteText);
            if (quantite < 0) {
                showAlert("Valeur invalide", "La quantité doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Veuillez saisir une quantité valide (nombre entier).");
            return;
        }
        if (prixOriginal == null || prixOriginal <= 0) {
            showAlert("Champ manquant", "Veuillez sélectionner un prix supérieur à 0.");
            return;
        }
        if (imagePath == null || imagePath.trim().isEmpty()) {
            showAlert("Image manquante", "Veuillez choisir une image pour le produit.");
            return;
        }

        // Conversion du prix en dinar tunisien si nécessaire
        double prixFinal = prixOriginal;
        try {
            // Détecter l'adresse IP de l'utilisateur
            String userIP = CurrencyService.detectUserIP();
            System.out.println("IP détectée: " + userIP);

            // Déterminer la devise en fonction de l'IP
            String userCurrency = CurrencyService.getCurrencyFromIP(userIP);
            System.out.println("Devise détectée: " + userCurrency);

            // Convertir le prix en dinar tunisien
            prixFinal = CurrencyService.convertToTND(prixOriginal, userCurrency);
            System.out.println("Prix converti: " + prixFinal);

            // Arrondir à l'entier le plus proche si nécessaire
            prixFinal = Math.round(prixFinal);
            System.out.println("Prix arrondi: " + prixFinal);

            // Afficher un message d'information sur la conversion effectuée
            if (!userCurrency.equals("TND")) {
                showAlert("Information", "Prix converti de " + userCurrency + " vers TND: "
                        + prixOriginal + " " + userCurrency + " = " + prixFinal + " TND");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur, on utilise le prix original
            prixFinal = prixOriginal;
            showAlert("Information", "Impossible de convertir la devise. Prix utilisé sans conversion.");
        }

        // Création du produit avec le prix en dinar tunisien
        Produit produit = new Produit(quantite, nom, (int)prixFinal, imagePath);
        produitService.addEntity(produit);

        // Redirection vers la liste des produits
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeProduits.fxml"));
            Parent root = loader.load();
            // Appeler une méthode de rafraîchissement si elle existe
            // Exemple : ((ListeProduitsController) loader.getController()).rafraichirListe();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void annuler() {
        nomProduitField.clear();
        quantiteField.clear();
        prixSpinner.getValueFactory().setValue(0);
        imageLabel.setText("Aucun fichier sélectionné");
        imagePath = null;
    }
}
