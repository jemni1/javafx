package entities;

import java.time.LocalDate;  // Assurez-vous que le chemin d'importation correspond à votre structure de projet.
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
public class CollecteDechet {
    private int id;
    private String typeDechet;
    private double quantite;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String imageUrl;

    // Ajout de la relation ManyToOne avec RecyclageDechet
    @ManyToOne
    @JoinColumn(name = "recyclage_dechet_id")
    private RecyclageDechet recyclageDechet;

    // Constructeurs
    public CollecteDechet(String typeDechet, double quantite, LocalDate dateDebut, LocalDate dateFin, String imageUrl, RecyclageDechet recyclageDechet) {
        this.typeDechet = typeDechet;
        this.quantite = quantite;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.imageUrl = imageUrl;
        this.recyclageDechet = recyclageDechet; // Initialisation du recyclage
    }

    public CollecteDechet(String typeDechet, double quantite, LocalDate dateDebut, LocalDate dateFin, String imageUrl) {
        this.typeDechet = typeDechet;
        this.quantite = quantite;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.imageUrl = imageUrl;
    }
    public CollecteDechet() {
    }

    // Constructeur sans recyclageDechet
    public CollecteDechet(int id, String typeDechet, double quantite, LocalDate dateDebut, LocalDate dateFin, String imageUrl) {
        this.id = id;
        this.typeDechet = typeDechet;
        this.quantite = quantite;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.imageUrl = imageUrl;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeDechet() {
        return typeDechet;
    }

    public void setTypeDechet(String typeDechet) {
        this.typeDechet = typeDechet;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter et Setter pour recyclageDechet
    public RecyclageDechet getRecyclageDechet() {
        return recyclageDechet;
    }

    public void setRecyclageDechet(RecyclageDechet recyclageDechet) {
        this.recyclageDechet = recyclageDechet;
    }
}
