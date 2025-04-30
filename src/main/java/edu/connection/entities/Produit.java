package edu.connection.entities;

import java.util.Date;

public class Produit {
    private int id;
    private int quantite;
    private String nom;
    private int prix;
    private String image;
    private int idTerrain;

    public Produit() {}

    public Produit(int quantite, String nom, int prix, String image, int idTerrain) {
        this.quantite = quantite;
        this.nom = nom;
        this.prix = prix;
        this.image = image;
        this.idTerrain = idTerrain;
    }
    public Produit(int quantite, String nom, int prix, String image) {
        this.quantite = quantite;
        this.nom = nom;
        this.prix = prix;
        this.image = image;
    }

    public Produit(int id, int quantite, String nom, int prix, String image, int idTerrain) {
        this.id = id;
        this.quantite = quantite;
        this.nom = nom;
        this.prix = prix;
        this.image = image;
        this.idTerrain = idTerrain;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIdTerrain() {
        return idTerrain;
    }

    public void setIdTerrain(int idTerrain) {
        this.idTerrain = idTerrain;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", image='" + image + '\'' +
                ", idTerrain=" + idTerrain +
                '}';
    }
}
