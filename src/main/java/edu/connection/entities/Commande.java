package edu.connection.entities;

import java.util.Date;

public class Commande {
    private int id;
    private int quantite;
    private int idClient;
    private int idProduit;
    private Date date;
    private int prix;
    private String nom;

    public Commande() {}

    public Commande(int quantite, int idClient, int idProduit,  int prix, String nom) {
        this.quantite = quantite;
        this.idClient = idClient;
        this.idProduit = idProduit;
        this.date = new Date();
        this.prix = prix;
        this.nom = nom;
    }

    public Commande(int id, int quantite, int idClient, int idProduit,   int prix, String nom) {
        this.id = id;
        this.quantite = quantite;
        this.idClient = idClient;
        this.idProduit = idProduit;
        this.date = new Date();
        this.prix = prix;
        this.nom = nom;
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

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }



    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "Commandes{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", idClient=" + idClient +
                ", idProduit=" + idProduit +
                ", date=" + date +
                ", prix=" + prix +
                ", nom='" + nom + '\'' +
                '}';
    }
}
