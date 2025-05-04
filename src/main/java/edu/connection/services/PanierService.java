package edu.connection.services;

import edu.connection.entities.PanierItem;
import edu.connection.entities.Produit;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PanierService {
    // Le panier est une Map associant l'id du produit à un objet PanierItem
    private Map<Integer, PanierItem> cart = new HashMap<>();

    // Ajouter un produit au panier
    public void addToCart(int idProduit, int quantity) throws SQLException {
        // Récupérer le produit à partir de son ID
        ProduitService produitService = new ProduitService();
        Produit produit = produitService.getList().stream()
                .filter(p -> p.getId() == idProduit)
                .findFirst()
                .orElse(null);

        // Si le produit n'existe pas
        if (produit == null) {
            throw new IllegalArgumentException("Produit non trouvé.");
        }

        // Vérification de la quantité
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité doit être supérieure ou égale à 1.");
        }

        if (quantity > produit.getQuantite()) {
            throw new IllegalArgumentException("La quantité demandée dépasse le stock disponible.");
        }

        // Si le produit existe déjà dans le panier, on met à jour la quantité
        PanierItem item = cart.get(idProduit);
        if (item != null) {
            item.setQuantity(item.getQuantity() - quantity);
        } else {
            // Sinon, on ajoute un nouveau produit dans le panier
            cart.put(idProduit, new PanierItem(produit, quantity));
        }

        System.out.println("Produit ajouté au panier : " + produit.getNom() + " avec quantité " + quantity);
    }

    // Supprimer un produit du panier
    public void removeFromCart(int idProduit) {
        if (cart.containsKey(idProduit)) {
            cart.remove(idProduit);
            System.out.println("Produit supprimé du panier.");
        } else {
            System.out.println("Produit non trouvé dans le panier.");
        }
    }

    // Mettre à jour la quantité d'un produit dans le panier
    public void updateCart(int idProduit, int quantity) throws SQLException {
        // Récupérer le produit à partir de son ID
        ProduitService produitService = new ProduitService();
        Produit produit = produitService.getList().stream()
                .filter(p -> p.getId() == idProduit)
                .findFirst()
                .orElse(null);

        if (produit == null) {
            throw new IllegalArgumentException("Produit non trouvé.");
        }

        // Vérification de la quantité
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité doit être supérieure ou égale à 1.");
        }

        if (quantity > produit.getQuantite()) {
            throw new IllegalArgumentException("La quantité demandée dépasse le stock disponible.");
        }

        // Si le produit existe déjà dans le panier, on met à jour la quantité
        PanierItem item = cart.get(idProduit);
        if (item != null) {
            item.setQuantity(quantity);
            System.out.println("Quantité mise à jour dans le panier.");
        } else {
            throw new IllegalArgumentException("Produit non trouvé dans le panier.");
        }
    }

    // Récupérer le total du panier
    public double getTotal() {
        double total = 0;
        for (PanierItem item : cart.values()) {
            total += item.getProduit().getPrix() * item.getQuantity();
        }
        return total;
    }

    // Afficher les produits dans le panier
    public Map<Integer, PanierItem> getCart() {
        return cart;
    }
    public void processPayment() throws SQLException {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Le panier est vide. Impossible de procéder au paiement.");
        }

        ProduitService produitService = new ProduitService();

        for (PanierItem item : cart.values()) {
            Produit produit = item.getProduit();
            int nouvelleQuantite = produit.getQuantite() - item.getQuantity();

            if (nouvelleQuantite < 0) {
                throw new IllegalArgumentException("Stock insuffisant pour le produit : " + produit.getNom());
            }

            // Mettre à jour la quantité dans la base de données
            produit.setQuantite(nouvelleQuantite);
            produitService.updateEntity(produit, produit.getId());
        }

        // Vider le panier après paiement
        cart.clear();
        System.out.println("Paiement effectué avec succès ! Le panier a été vidé.");
    }

}
