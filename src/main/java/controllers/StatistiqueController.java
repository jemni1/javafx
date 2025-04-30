package controllers;

import entities.RecyclageDechet;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class StatistiqueController {

    @FXML
    private BarChart<String, Number> quantiteRecycleeChart;

    @FXML
    private ProgressBar energieProduiteProgress;

    @FXML
    private Label rendementLabel;

    private RecyclageDechet recyclage; // Le recyclage reçu

    public void setRecyclage(RecyclageDechet recyclage) {
        this.recyclage = recyclage;
        afficherStatistiques();
    }

    private void afficherStatistiques() {
        if (recyclage != null) {
            // Remplir le BarChart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Déchet", recyclage.getQuantiteRecyclee()));
            quantiteRecycleeChart.getData().clear();
            quantiteRecycleeChart.getData().add(series);

            // ProgressBar énergie (exemple, pourcentage par rapport à 100 kWh max)
            double maxEnergie = 10000.0; // Tu peux ajuster
            double pourcentage = recyclage.getEnergieProduite() / maxEnergie;
            energieProduiteProgress.setProgress(Math.min(pourcentage, 1.0));

            // Rendement = Energie / Quantité
            if (recyclage.getQuantiteRecyclee() != 0) {
                double rendement = recyclage.getEnergieProduite() / recyclage.getQuantiteRecyclee();
                rendementLabel.setText(String.format("%.2f", rendement));
            } else {
                rendementLabel.setText("N/A");
            }
        }
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) quantiteRecycleeChart.getScene().getWindow();
        stage.close();
    }
}
