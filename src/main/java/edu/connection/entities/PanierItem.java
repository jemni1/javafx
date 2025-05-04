package edu.connection.entities;

public class PanierItem {
    private Produit produit;
    private int quantity;

    public PanierItem(Produit produit, int quantity) {
        this.produit = produit;
        this.quantity = quantity;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
