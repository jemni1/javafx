package edu.connection.controllers;

import edu.connection.entities.Produit;
import edu.connection.services.ProduitService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Pagination;

import java.io.IOException;
import java.util.List;
import javafx.scene.control.TextField;

public class listeProduits {

    @FXML private FlowPane flowPaneProduits;
    private final ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        allProduits = produitService.getList();
        setupPagination();
        afficherProduits();
        pagination.setStyle("-fx-page-information-visible: false;");
        pagination.getStyleClass().add(Pagination.STYLE_CLASS_BULLET);
    }
    @FXML private TextField searchNomField;
    @FXML private TextField minPrixField;
    @FXML private TextField maxPrixField;
    @FXML private Pagination pagination;

    private static final int ITEMS_PER_PAGE = 5;
    private List<Produit> allProduits;
    private void setupPagination() {
        int totalPages = (int) Math.ceil((double) allProduits.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(totalPages);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }
    private VBox createPage(int pageIndex) {
        flowPaneProduits.getChildren().clear();

        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allProduits.size());
        List<Produit> produitsPage = allProduits.subList(start, end);

        for (Produit produit : produitsPage) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProduitCard.fxml"));
                VBox carteProduit = loader.load();

                ProduitCard controller = loader.getController();
                controller.setData(produit);

                flowPaneProduits.getChildren().add(carteProduit);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new VBox(); // Ne pas afficher de contenu spécifique ici, c'est déjà géré via flowPane
    }

    private void afficherProduits() {
        List<Produit> produits = produitService.getList();

        for (Produit produit : produits) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProduitCard.fxml"));
                VBox carteProduit = loader.load();

                ProduitCard controller = loader.getController();
                controller.setData(produit);

                flowPaneProduits.getChildren().add(carteProduit);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void goToCommandes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_commandes.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page des commandes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void goToStatistique(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/statistiques.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page des commandes");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void goToObjectDetection(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ObjectDetectionView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page de detection");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void filtrerProduits() {
        String nom = searchNomField.getText().trim();
        String minPrixStr = minPrixField.getText().trim();
        String maxPrixStr = maxPrixField.getText().trim();

        Double minPrix = minPrixStr.isEmpty() ? null : Double.parseDouble(minPrixStr);
        Double maxPrix = maxPrixStr.isEmpty() ? null : Double.parseDouble(maxPrixStr);

        // On récupère les produits filtrés en fonction des critères
        List<Produit> produitsFiltres = produitService.rechercherProduits(nom, minPrix, maxPrix);

        // On efface les anciens produits affichés
        flowPaneProduits.getChildren().clear();

        // Mettez à jour la pagination avec les produits filtrés
        allProduits = produitsFiltres;  // On met à jour la liste avec les produits filtrés
        setupPagination();  // Appel de la méthode pour mettre en place la pagination
    }


    @FXML
    private void goToAjoutProduit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajout_produit.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle depuis le bouton
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Changer la scène
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
