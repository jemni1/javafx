package entities;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;

public class RecyclageDechet {

    private int id;
    private double quantiteRecyclage;
    private double energieProduite;
    private String utilisation;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String imageUrl;

    // Relation avec CollecteDechet (OneToMany)
    @OneToMany(mappedBy = "recyclageDechet")
    private List<CollecteDechet> collectes;

    public RecyclageDechet() {}

    public RecyclageDechet(int id, double quantiteRecyclage, double energieProduite, String utilisation, LocalDate dateDebut, LocalDate dateFin, String imageUrl) {
        this.id = id;
        this.quantiteRecyclage = quantiteRecyclage;
        this.energieProduite = energieProduite;
        this.utilisation = utilisation;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.imageUrl = imageUrl;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getQuantiteRecyclage() { return quantiteRecyclage; }
    public void setQuantiteRecyclage(double quantiteRecyclage) { this.quantiteRecyclage = quantiteRecyclage; }

    public double getEnergieProduite() { return energieProduite; }
    public void setEnergieProduite(double energieProduite) { this.energieProduite = energieProduite; }

    public String getUtilisation() { return utilisation; }
    public void setUtilisation(String utilisation) { this.utilisation = utilisation; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<CollecteDechet> getCollectes() { return collectes; }
    public void setCollectes(List<CollecteDechet> collectes) { this.collectes = collectes; }

    // Méthodes supplémentaires
    public double getQuantiteRecyclee() {
        return quantiteRecyclage;
    }

    public String getUtilisationEnergie() {
        return utilisation;
    }
}
