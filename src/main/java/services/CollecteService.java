package services;

import entities.CollecteDechet;
import tools.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CollecteService {

    private static CollecteService instance;
    // Method to return the singleton instance
    public static CollecteService getInstance() {
        if (instance == null) {
            instance = new CollecteService();
        }
        return instance;
    }
    // Ajouter une collecte à la base de données
    public void ajouterCollecte(CollecteDechet collecte) {
        String query = "INSERT INTO collecte_dechet (type_dechet, quantite, date_debut, date_fin, image_url) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, collecte.getTypeDechet());
            stmt.setDouble(2, collecte.getQuantite());
            stmt.setDate(3, Date.valueOf(collecte.getDateDebut()));
            stmt.setDate(4, Date.valueOf(collecte.getDateFin()));
            stmt.setString(5, collecte.getImageUrl());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer toutes les collectes de la base de données
    public List<CollecteDechet> getAllCollectes() {
        List<CollecteDechet> collectes = new ArrayList<>();
        String query = "SELECT * FROM collecte_dechet";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String typeDechet = rs.getString("type_dechet");
                double quantite = rs.getDouble("quantite");
                LocalDate dateDebut = rs.getDate("date_debut").toLocalDate();
                LocalDate dateFin = rs.getDate("date_fin").toLocalDate();
                String imageUrl = rs.getString("image_url");

                CollecteDechet collecte = new CollecteDechet(id, typeDechet, quantite, dateDebut, dateFin, imageUrl);
                collectes.add(collecte);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collectes;
    }

    // Mettre à jour une collecte
    public void updateCollecte(CollecteDechet collecte) {
        String query = "UPDATE collecte_dechet SET type_dechet = ?, quantite = ?, date_debut = ?, date_fin = ?, image_url = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, collecte.getTypeDechet());
            stmt.setDouble(2, collecte.getQuantite());
            stmt.setDate(3, Date.valueOf(collecte.getDateDebut()));
            stmt.setDate(4, Date.valueOf(collecte.getDateFin()));
            stmt.setString(5, collecte.getImageUrl());
            stmt.setInt(6, collecte.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Supprimer une collecte par ID
    public void deleteCollecte(int id) {
        String query = "DELETE FROM collecte_dechet WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Récupérer les collectes sans recyclage associé
    public List<CollecteDechet> getCollectesSansRecyclage() {
        List<CollecteDechet> collectes = new ArrayList<>();
        String query = "SELECT * FROM collecte_dechet WHERE recyclage_dechet_id IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String typeDechet = rs.getString("type_dechet");
                double quantite = rs.getDouble("quantite");
                LocalDate dateDebut = rs.getDate("date_debut").toLocalDate();
                LocalDate dateFin = rs.getDate("date_fin").toLocalDate();
                String imageUrl = rs.getString("image_url");

                CollecteDechet collecte = new CollecteDechet(id, typeDechet, quantite, dateDebut, dateFin, imageUrl);
                collectes.add(collecte);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collectes;
    }


}
