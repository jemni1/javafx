package edu.connection.services;
import edu.connection.entities.PanierItem;

import edu.connection.entities.Commande;
import edu.connection.interfaces.Iservices;
import edu.connection.tools.MyConnection;
import edu.connection.entities.Produit;


import java.util.HashMap;
import java.util.Map;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements Iservices<Commande> {

    @Override
    public void addEntity(Commande commande) {
        if (commande.getQuantite() <= 0) {
            System.out.println("Erreur : la quantité doit être supérieure à 0.");
            return;
        }
        if (commande.getPrix() <= 0) {
            System.out.println("Erreur : le prix doit être supérieur à 0.");
            return;
        }
        if (commande.getNom() == null || commande.getNom().trim().isEmpty()) {
            System.out.println("Erreur : le nom ne doit pas être vide.");
            return;
        }
        try {
            String requete = "INSERT INTO commandes (quantite, id_client, id_produit, date, prix, nom) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, commande.getQuantite());
            pst.setInt(2, commande.getIdClient());
            pst.setInt(3, commande.getIdProduit());
            // Date est déjà gérée automatiquement grâce au constructeur de Commande
            pst.setTimestamp(4, new Timestamp(commande.getDate().getTime()));
            pst.setInt(5, commande.getPrix());
            pst.setString(6, commande.getNom());
            pst.executeUpdate();
            System.out.println("Commande ajoutée avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la commande: " + e.getMessage());
        }
    }
    public List<Commande> searchByName(String keyword) {
        List<Commande> commandes = new ArrayList<>();
        try {
            String requete = "SELECT * FROM commandes WHERE nom LIKE ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setQuantite(rs.getInt("quantite"));
                commande.setIdClient(rs.getInt("id_client"));
                commande.setIdProduit(rs.getInt("id_produit"));
                commande.setDate(rs.getTimestamp("date"));
                commande.setPrix(rs.getInt("prix"));
                commande.setNom(rs.getString("nom"));
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche des commandes : " + e.getMessage());
        }
        return commandes;
    }


    @Override
    public void updateEntity(Commande commande, int id) {
        if (commande.getQuantite() <= 0) {
            System.out.println("Erreur : la quantité doit être supérieure à 0.");
            return;
        }
        if (commande.getPrix() <= 0) {
            System.out.println("Erreur : le prix doit être supérieur à 0.");
            return;
        }
        if (commande.getNom() == null || commande.getNom().trim().isEmpty()) {
            System.out.println("Erreur : le nom ne doit pas être vide.");
            return;
        }
        try {
            String requete = "UPDATE commandes SET quantite = ?, id_client = ?, id_produit = ?, date = ?, prix = ?, nom = ? WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, commande.getQuantite());
            pst.setInt(2, commande.getIdClient());
            pst.setInt(3, commande.getIdProduit());
            pst.setTimestamp(4, new Timestamp(commande.getDate().getTime()));
            pst.setInt(5, commande.getPrix());
            pst.setString(6, commande.getNom());
            pst.setInt(7, id);
            pst.executeUpdate();
            System.out.println("Commande mise à jour avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de la commande: " + e.getMessage());
        }
    }

    @Override
    public void deleteEntity(Commande commande) {
        try {
            String requete = "DELETE FROM commandes WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, commande.getId());
            pst.executeUpdate();
            System.out.println("Commande supprimée avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la commande: " + e.getMessage());
        }
    }

    public void deleteEntityById(int id) {
        try {
            String requete = "DELETE FROM commandes WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Produit supprimé avec succès (ID: " + id + ")");
            } else {
                System.out.println("Aucun produit trouvé avec cet ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la commande: " + e.getMessage());
        }
    }

    public List<Commande> getList() {
        List<Commande> commandes = new ArrayList<>();
        try {
            String requete = "SELECT * FROM commandes";
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setQuantite(rs.getInt("quantite"));
                commande.setIdClient(rs.getInt("id_client"));
                commande.setIdProduit(rs.getInt("id_produit"));
                commande.setDate(rs.getTimestamp("date"));
                commande.setPrix(rs.getInt("prix"));
                commande.setNom(rs.getString("nom"));
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commandes: " + e.getMessage());
        }
        return commandes;
    }

    public void validerCommande(Map<Produit, Integer> panier, int idClient) throws SQLException {
        double total = 0;

        // Parcourir les éléments du panier pour ajouter chaque produit comme une commande distincte
        for (Map.Entry<Produit, Integer> entry : panier.entrySet()) {
            Produit produit = entry.getKey();
            int quantity = entry.getValue();

            // Calcul du total pour chaque produit
            double produitTotal = produit.getPrix() * quantity;
            total += produitTotal;

            // Créer une nouvelle commande pour chaque produit
            Commande commande = new Commande();
            commande.setIdClient(idClient);
            commande.setDate(new java.util.Date()); // Utilise la date actuelle
            commande.setNom(produit.getNom());
            commande.setQuantite(quantity);
            commande.setIdProduit(produit.getId());
            commande.setPrix((int) produitTotal); // Prix total pour ce produit

            // Mettre à jour la quantité du produit dans la base de données
            ProduitService produitService = new ProduitService();
            produit.setQuantite(produit.getQuantite() - quantity);  // Réduction de la quantité en stock
            produitService.updateEntity(produit, produit.getId());

            // Enregistrer la commande dans la base de données
            addEntity(commande); // Utilise la méthode addEntity pour sauvegarder la commande

            // Optionnel : Afficher un message de succès pour chaque commande validée
            System.out.println("Commande pour " + produit.getNom() + " validée avec succès !");
        }

        // Affichage du total global si vous le souhaitez
        System.out.println("Total de la commande : " + total + " €");

    }
    // Quantité livrée par produit
    public Map<String, Integer> getQuantiteParProduit() {
        Map<String, Integer> map = new HashMap<>();
        try {
            String requete = "SELECT nom, SUM(quantite) as total FROM commandes GROUP BY nom";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("nom"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getQuantiteParProduit: " + e.getMessage());
        }
        return map;
    }

    // Revenus par jour
    public Map<String, Double> getChiffreAffairesParDate() {
        Map<String, Double> map = new HashMap<>();
        try {
            String requete = "SELECT DATE(date) as jour, SUM(prix) as total FROM commandes GROUP BY jour";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("jour"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getChiffreAffairesParDate: " + e.getMessage());
        }
        return map;
    }

    // Part des ventes par produit
    public Map<String, Double> getPartVentesProduits() {
        Map<String, Double> map = new HashMap<>();
        try {
            String requete = "SELECT nom, SUM(prix) as total FROM commandes GROUP BY nom";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("nom"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getPartVentesProduits: " + e.getMessage());
        }
        return map;
    }




}
