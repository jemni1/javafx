package edu.connection.services;

import edu.connection.entities.Produit;
import edu.connection.interfaces.Iservices;
import edu.connection.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements Iservices<Produit> {
    @Override
    public void addEntity(Produit produit) {
        // Vérification des données d'entrée
        if (produit.getNom() == null || produit.getNom().trim().isEmpty()) {
            System.out.println("Erreur : le nom du produit ne doit pas être vide.");
            return;
        }
        if (produit.getQuantite() <= 0) {
            System.out.println("Erreur : la quantité doit être supérieure à 0.");
            return;
        }
        if (produit.getPrix() <= 0) {
            System.out.println("Erreur : le prix doit être supérieur à 0.");
            return;
        }
        if (produit.getImage() == null || produit.getImage().trim().isEmpty()) {
            System.out.println("Erreur : l'image du produit ne doit pas être vide.");
            return;
        }

        // Vérifier si le produit existe déjà dans la base de données
        if (produitExiste(produit)) {
            System.out.println("Erreur : un produit avec ce nom existe déjà.");
            return;
        }

        // Si tout est valide, procéder à l'ajout du produit
        try {
            String requete = "INSERT INTO produits (nom, quantite, prix, image) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, produit.getNom());
            pst.setInt(2, produit.getQuantite());
            pst.setInt(3, produit.getPrix());
            pst.setString(4, produit.getImage());
            pst.executeUpdate();
            System.out.println("Produit ajouté avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du produit: " + e.getMessage());
        }
    }

    // Méthode pour vérifier si le produit existe déjà dans la base de données
    private boolean produitExiste(Produit produit) {
        String requete = "SELECT COUNT(*) FROM produits WHERE nom = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, produit.getNom());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; // Retourne true si le produit existe déjà
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification du produit: " + e.getMessage());
        }
        return false; // Retourne false si le produit n'existe pas
    }


    public List<Produit> getList() {
        List<Produit> data = new ArrayList<>();
        try {
            String requete = "SELECT * FROM produits WHERE quantite > 1";
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getInt("id"));
                produit.setNom(rs.getString("nom"));
                produit.setQuantite(rs.getInt("quantite"));
                produit.setPrix(rs.getInt("prix"));
                produit.setImage(rs.getString("image"));
                data.add(produit);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits: " + e.getMessage());
        }
        return data;
    }

    @Override
    public void deleteEntity(Produit produit) {
        try {
            String requete = "DELETE FROM produits WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, produit.getId());
            pst.executeUpdate();
            System.out.println("Produit supprimé avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du produit: " + e.getMessage());
        }
    }

    public void deleteEntityById(int id) {
        try {
            String requete = "DELETE FROM produits WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            int rowsDeleted =pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Produit supprimé avec succès (ID: " + id + ")");
            } else {
                System.out.println("Aucun produit trouvé avec cet ID: " + id);
            }        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du produit: " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(Produit produit, int id) {
        if (produit.getNom() == null || produit.getNom().trim().isEmpty()) {
            System.out.println("Erreur : le nom du produit ne doit pas être vide.");
            return;
        }
        if (produit.getQuantite() <= 0) {
            System.out.println("Erreur : la quantité doit être supérieure à 0.");
            return;
        }
        if (produit.getPrix() <= 0) {
            System.out.println("Erreur : le prix doit être supérieur à 0.");
            return;
        }
        if (produit.getImage() == null || produit.getImage().trim().isEmpty()) {
            System.out.println("Erreur : l'image du produit ne doit pas être vide.");
            return;
        }
        try {
            String requete = "UPDATE produits SET nom = ?, quantite = ?, prix = ?, image = ? WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, produit.getNom());
            pst.setInt(2, produit.getQuantite());
            pst.setInt(3, produit.getPrix());
            pst.setString(4, produit.getImage());
            pst.setInt(5, id);
            pst.executeUpdate();
            System.out.println("Produit mis à jour avec succès");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du produit: " + e.getMessage());
        }
    }
    public List<Produit> rechercherProduits(String nom, Double minPrix, Double maxPrix) {
        List<Produit> resultats = new ArrayList<>();

        for (Produit produit : getList()) {
            boolean correspond = true;

            if (nom != null && !nom.isEmpty() && !produit.getNom().toLowerCase().contains(nom.toLowerCase())) {
                correspond = false;
            }

            if (minPrix != null && produit.getPrix() < minPrix) {
                correspond = false;
            }

            if (maxPrix != null && produit.getPrix() > maxPrix) {
                correspond = false;
            }

            if (correspond) {
                resultats.add(produit);
            }
        }

        return resultats;
    }

    public List<Produit> searchByName(String nom) {
        List<Produit> produits = new ArrayList<>();
        try {
            String requete = "SELECT * FROM produits WHERE nom LIKE ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, "%" + nom + "%"); // Recherche partielle avec LIKE
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setQuantite(rs.getInt("quantite"));
                p.setPrix(rs.getInt("prix"));
                p.setImage(rs.getString("image"));
                produits.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche du produit: " + e.getMessage());
        }
        return produits;
    }

}
