package edu.connection.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import edu.connection.entities.Produit;
import edu.connection.services.ProduitService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class userProduit implements Initializable {

    @FXML private FlowPane productsContainer;
    @FXML private TextField searchNameField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private Button searchButton;
    @FXML private Button panierBtn;
    @FXML private Label labelCommandes;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private final ProduitService produitService = new ProduitService();
    private final Map<Produit, Integer> panier = new HashMap<>();
    private int currentPage = 0;
    private final int itemsPerPage = 5;
    private List<Produit> allProduits = new ArrayList<>();

    private void updatePanierCount() {
        int totalItems = panier.values().stream().mapToInt(Integer::intValue).sum();
        // Trouver le label dans le StackPane du panier
        StackPane panierContainer = (StackPane) panierBtn.getParent();
        for (javafx.scene.Node node : panierContainer.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setText(String.valueOf(totalItems));
                // Cacher le badge si le panier est vide
                node.setVisible(totalItems > 0);
                // Cacher aussi le cercle si le panier est vide
                for (javafx.scene.Node circleNode : panierContainer.getChildren()) {
                    if (circleNode instanceof Circle) {
                        circleNode.setVisible(totalItems > 0);
                    }
                }
            }
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allProduits = produitService.getList();
        updatePage();
        updatePanierCount();
        panierBtn.setOnMouseEntered(e -> panierBtn.setStyle("-fx-background-color: #004d00; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20;"));
        panierBtn.setOnMouseExited(e -> panierBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 20;"));
        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < allProduits.size()) {
                currentPage++;
                updatePage();
            }
        });

        labelCommandes.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/commande.fxml"));
                Parent root = loader.load();
                labelCommandes.getScene().setRoot(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        searchButton.setOnAction(e -> handleRecherche());

        panierBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/panierPopup.fxml"));
                Parent root = loader.load();
                PanierPopupController controller = loader.getController();
                controller.setPanier(panier);
                controller.setOnPanierChangeCallback(() -> updatePanierCount());

                Stage popupStage = new Stage();
                popupStage.setTitle("Panier");
                popupStage.setScene(new Scene(root));
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.showAndWait();
                updatePanierCount();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updatePage() {
        productsContainer.getChildren().clear();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allProduits.size());
        List<Produit> pageProduits = allProduits.subList(start, end);

        afficherProduits(pageProduits);

        pageLabel.setText("Page " + (currentPage + 1));
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(end >= allProduits.size());
    }

    private void handleRecherche() {
        String nom = searchNameField.getText().trim();
        String minStr = minPriceField.getText().trim();
        String maxStr = maxPriceField.getText().trim();

        Double minPrix = null, maxPrix = null;
        try {
            if (!minStr.isEmpty()) minPrix = Double.parseDouble(minStr);
            if (!maxStr.isEmpty()) maxPrix = Double.parseDouble(maxStr);
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Veuillez entrer des prix valides.").show();
            return;
        }

        allProduits = produitService.rechercherProduits(
                nom.isEmpty() ? null : nom, minPrix, maxPrix
        );
        currentPage = 0;
        updatePage();
    }

    private void afficherProduits(List<Produit> produits) {
        productsContainer.getChildren().clear();
        for (Produit produit : produits) {
            productsContainer.getChildren().add(creerCarteProduit(produit));
        }
    }

    private VBox creerCarteProduit(Produit produit) {
        VBox produitCard = new VBox(10);
        produitCard.setPrefWidth(180);
        produitCard.setPadding(new Insets(10));
        produitCard.setStyle("""
                -fx-background-color: white;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);
                """);

        ImageView image = new ImageView();
        image.setFitWidth(160);
        image.setFitHeight(100);
        try {
            Image img = new Image(new File("images/" + produit.getImage()).toURI().toString());
            image.setImage(img);
        } catch (Exception e) {
            System.out.println("Image non trouvée pour le produit " + produit.getNom());
            e.printStackTrace();
        }

        Label stockLabel = new Label("Stock: " + produit.getQuantite());
        stockLabel.setStyle("""
                -fx-background-color: #007bff;
                -fx-text-fill: white;
                -fx-padding: 3 7 3 7;
                -fx-background-radius: 5;
                """);

        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-weight: bold;");

        Label prixLabel = new Label(produit.getPrix() + " €");
        prixLabel.setStyle("-fx-text-fill: red;");

        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, produit.getQuantite(), 1));
        spinner.setPrefWidth(60);

        Button ajouterPanierBtn = new Button("🛒");
        ajouterPanierBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        ajouterPanierBtn.setOnMouseEntered(e -> ajouterPanierBtn.setStyle("-fx-background-color: #3e8e41; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;"));
        ajouterPanierBtn.setOnAction(e -> {
            int quantite = spinner.getValue();
            if (quantite > 0) {
                panier.merge(produit, quantite, Integer::sum);
                updatePanierCount(); // Mettre à jour le compteur
            }
        });

        HBox actions = new HBox(10, spinner, ajouterPanierBtn);

        produitCard.getChildren().addAll(image, stockLabel, nomLabel, prixLabel, actions);
        return produitCard;
    }
}
