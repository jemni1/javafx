package edu.connection.controllers;

import edu.connection.services.CommandeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class StatistiquesController {

    @FXML
    private BarChart<String, Number> quantiteBarChart;

    @FXML
    private LineChart<String, Number> chiffreAffairesLineChart;

    @FXML
    private PieChart ventesPieChart;
    @FXML
    private void goToListeProduits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listeProduits.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) quantiteBarChart.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
    private void goToAdminCommandes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_commandes.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) quantiteBarChart.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private final CommandeService commandeService = new CommandeService();

    public void initialize() {
        loadQuantiteParProduit();
        loadChiffreAffairesParDate();
        loadPartVentesProduits();
    }

    private void loadQuantiteParProduit() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> data = commandeService.getQuantiteParProduit();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        quantiteBarChart.getData().add(series);
    }

    private void loadChiffreAffairesParDate() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Double> data = commandeService.getChiffreAffairesParDate();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chiffreAffairesLineChart.getData().add(series);
    }

    private void loadPartVentesProduits() {
        Map<String, Double> data = commandeService.getPartVentesProduits();
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            PieChart.Data pieSlice = new PieChart.Data(entry.getKey(), entry.getValue());
            ventesPieChart.getData().add(pieSlice);
        }
    }
}
