package services;

import entities.CollecteDechet;
import entities.RecyclageDechet;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tools.DatabaseConnection;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecyclageService {

    private Connection connection;

    // Instance unique pour le pattern Singleton
    private static RecyclageService instance;

    // Constructeur qui initialise la connexion à la base de données
    private RecyclageService(Connection connection) {
        this.connection = connection;
    }

    // Méthode pour obtenir l'instance unique de RecyclageService
    public static RecyclageService getInstance() throws SQLException {
        if (instance == null) {
            synchronized (RecyclageService.class) {
                if (instance == null) {
                    // Obtient une connexion à la base de données
                    Connection connection = DatabaseConnection.getConnection();
                    instance = new RecyclageService(connection);
                }
            }
        }
        return instance;
    }

    public void ajouterRecyclageAvecCollectes(RecyclageDechet recyclageDechet, List<CollecteDechet> collectes) throws SQLException {
        String queryRecyclage = "INSERT INTO recyclage_dechet (quantite_recyclee, energie_produite, utilisation, date_debut, date_fin, image_url) VALUES (?, ?, ?, ?, ?, ?)";

        // Insertion du recyclage
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryRecyclage, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setDouble(1, recyclageDechet.getQuantiteRecyclage());
            preparedStatement.setDouble(2, recyclageDechet.getEnergieProduite());
            preparedStatement.setString(3, recyclageDechet.getUtilisation());
            preparedStatement.setDate(4, Date.valueOf(recyclageDechet.getDateDebut()));
            preparedStatement.setDate(5, Date.valueOf(recyclageDechet.getDateFin()));
            preparedStatement.setString(6, recyclageDechet.getImageUrl());
            preparedStatement.executeUpdate();

            // Récupération de l'ID du recyclage inséré
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int recyclageId = generatedKeys.getInt(1);

                // Mise à jour des collectes existantes
                String updateCollecteQuery = "UPDATE collecte_dechet SET recyclage_dechet_id = ? WHERE id = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateCollecteQuery)) {
                    for (CollecteDechet collecte : collectes) {
                        updateStatement.setInt(1, recyclageId);   // nouvel id de recyclage
                        updateStatement.setInt(2, collecte.getId()); // collecte existante à mettre à jour
                        updateStatement.addBatch();
                    }
                    updateStatement.executeBatch(); // exécute toutes les mises à jour en une seule fois
                }
            }
        }
    }


    public void mettreAJourRecyclageAvecCollectes(RecyclageDechet recyclageDechet) throws SQLException {
        String queryRecyclage = "UPDATE recyclage_dechet SET quantite_recyclee = ?, energie_produite = ?, utilisation = ?, date_debut = ?, date_fin = ?, image_url = ? WHERE id = ?";

        // Mise à jour du recyclage
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryRecyclage)) {
            preparedStatement.setDouble(1, recyclageDechet.getQuantiteRecyclage());
            preparedStatement.setDouble(2, recyclageDechet.getEnergieProduite());
            preparedStatement.setString(3, recyclageDechet.getUtilisation());
            preparedStatement.setDate(4, Date.valueOf(recyclageDechet.getDateDebut()));
            preparedStatement.setDate(5, Date.valueOf(recyclageDechet.getDateFin()));
            preparedStatement.setString(6, recyclageDechet.getImageUrl());
            preparedStatement.setInt(7, recyclageDechet.getId());
            preparedStatement.executeUpdate();

            // Récupérer la liste des collectes associées à ce recyclage
            List<CollecteDechet> collectes = recyclageDechet.getCollectes();

            // Suppression des collectes existantes pour ce recyclage
            String deleteCollectesQuery = "DELETE FROM collecte_dechet WHERE recyclage_dechet_id = ?";
            try (PreparedStatement preparedStatementDelete = connection.prepareStatement(deleteCollectesQuery)) {
                preparedStatementDelete.setInt(1, recyclageDechet.getId());
                preparedStatementDelete.executeUpdate();
            }

            // Insertion ou mise à jour des collectes
            String selectCollecteQuery = "SELECT id FROM collecte_dechet WHERE recyclage_dechet_id = ? AND type_dechet = ? AND date_debut = ? AND date_fin = ?";
            String insertCollecteQuery = "INSERT INTO collecte_dechet (type_dechet, quantite, date_debut, date_fin, recyclage_dechet_id) VALUES (?, ?, ?, ?, ?)";
            String updateCollecteQuery = "UPDATE collecte_dechet SET quantite = ? WHERE id = ?";

            try (PreparedStatement preparedStatementInsert = connection.prepareStatement(insertCollecteQuery);
                 PreparedStatement preparedStatementUpdate = connection.prepareStatement(updateCollecteQuery);
                 PreparedStatement preparedStatementSelect = connection.prepareStatement(selectCollecteQuery)) {

                for (CollecteDechet collecte : collectes) {
                    // Vérifier si la collecte existe déjà
                    preparedStatementSelect.setInt(1, recyclageDechet.getId());
                    preparedStatementSelect.setString(2, collecte.getTypeDechet());
                    preparedStatementSelect.setDate(3, Date.valueOf(collecte.getDateDebut()));
                    preparedStatementSelect.setDate(4, Date.valueOf(collecte.getDateFin()));

                    try (ResultSet resultSet = preparedStatementSelect.executeQuery()) {
                        if (resultSet.next()) {
                            // Si la collecte existe, mettre à jour la quantité
                            int collecteId = resultSet.getInt("id");
                            preparedStatementUpdate.setDouble(1, collecte.getQuantite());
                            preparedStatementUpdate.setInt(2, collecteId);
                            preparedStatementUpdate.executeUpdate();
                        } else {
                            // Si la collecte n'existe pas, l'insérer
                            preparedStatementInsert.setString(1, collecte.getTypeDechet());
                            preparedStatementInsert.setDouble(2, collecte.getQuantite());
                            preparedStatementInsert.setDate(3, Date.valueOf(collecte.getDateDebut()));
                            preparedStatementInsert.setDate(4, Date.valueOf(collecte.getDateFin()));
                            preparedStatementInsert.setInt(5, recyclageDechet.getId());
                            preparedStatementInsert.addBatch();
                        }
                    }
                }

                // Exécuter l'insertion en batch pour les nouvelles collectes
                preparedStatementInsert.executeBatch();
            }
        }
    }

    // Méthode pour supprimer un recyclage et ses collectes associées
    public void supprimerRecyclageAvecCollectes(int id) throws SQLException {
        // Suppression des collectes associées
        String deleteCollectesQuery = "DELETE FROM collecte_dechet WHERE recyclage_dechet_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteCollectesQuery)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }

        // Suppression du recyclage
        String query = "DELETE FROM recyclage_dechet WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }

    // Méthode pour récupérer tous les recyclages
    public List<RecyclageDechet> getAllRecyclages() throws SQLException {
        List<RecyclageDechet> recyclages = new ArrayList<>();
        String query = "SELECT * FROM recyclage_dechet";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                RecyclageDechet recyclage = new RecyclageDechet();
                recyclage.setId(resultSet.getInt("id"));
                recyclage.setQuantiteRecyclage(resultSet.getDouble("quantite_recyclee"));
                recyclage.setEnergieProduite(resultSet.getDouble("energie_produite"));
                recyclage.setUtilisation(resultSet.getString("utilisation"));
                recyclage.setDateDebut(resultSet.getDate("date_debut").toLocalDate());
                recyclage.setDateFin(resultSet.getDate("date_fin").toLocalDate());
                recyclage.setImageUrl(resultSet.getString("image_url"));

                // Récupération des collectes associées à ce recyclage
                List<CollecteDechet> collectes = getCollectesByRecyclageId(recyclage.getId());
                recyclage.setCollectes(collectes);  // Associe les collectes au recyclage

                recyclages.add(recyclage);
            }
        }
        return recyclages;
    }

    // Méthode pour récupérer un recyclage par ID avec les collectes associées
    public RecyclageDechet getRecyclageById(int id) throws SQLException {
        String queryRecyclage = "SELECT * FROM recyclage_dechet WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryRecyclage)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Création de l'objet RecyclageDechet
                    RecyclageDechet recyclage = new RecyclageDechet();
                    recyclage.setId(resultSet.getInt("id"));
                    recyclage.setQuantiteRecyclage(resultSet.getDouble("quantite_recyclee"));
                    recyclage.setEnergieProduite(resultSet.getDouble("energie_produite"));
                    recyclage.setUtilisation(resultSet.getString("utilisation"));
                    recyclage.setDateDebut(resultSet.getDate("date_debut").toLocalDate());
                    recyclage.setDateFin(resultSet.getDate("date_fin").toLocalDate());
                    recyclage.setImageUrl(resultSet.getString("image_url"));

                    // Récupération des collectes associées à ce recyclage
                    List<CollecteDechet> collectes = getCollectesByRecyclageId(id);
                    recyclage.setCollectes(collectes);  // Associe les collectes au recyclage

                    return recyclage;
                }
            }
        }
        return null; // Si aucun recyclage n'est trouvé
    }

    // Méthode pour récupérer les collectes associées à un recyclage
    private List<CollecteDechet> getCollectesByRecyclageId(int recyclageId) throws SQLException {
        List<CollecteDechet> collectes = new ArrayList<>();
        String queryCollecte = "SELECT * FROM collecte_dechet WHERE recyclage_dechet_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryCollecte)) {
            preparedStatement.setInt(1, recyclageId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    CollecteDechet collecte = new CollecteDechet();
                    collecte.setId(resultSet.getInt("id"));
                    collecte.setTypeDechet(resultSet.getString("type_dechet"));
                    collecte.setQuantite(resultSet.getDouble("quantite"));
                    collecte.setDateDebut(resultSet.getDate("date_debut").toLocalDate());
                    collecte.setDateFin(resultSet.getDate("date_fin").toLocalDate());
                    collectes.add(collecte);
                }
            }
        }
        return collectes;
    }

    public double calculerRendement(RecyclageDechet recyclage) {
        if (recyclage.getQuantiteRecyclee() == 0) {
            return 0;
        }
        return (recyclage.getEnergieProduite() / recyclage.getQuantiteRecyclee())*100;
    }

}
