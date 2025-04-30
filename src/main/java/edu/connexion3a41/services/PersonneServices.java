package edu.connexion3a41.services;

import edu.connexion3a41.entities.Personne;
import edu.connexion3a41.interfaces.IService;
import edu.connexion3a41.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonneServices implements IService<Personne> {
    @Override
    public void addEntity(Personne personne) {
        try {
            String requete = "INSERT INTO personne (nom,prenom)" +
                    "VALUES ('" + personne.getNom() + "','" + personne.getPrenom() + "')";


            Statement st = MyConnection.getInstance().getCnx().createStatement();
            st.executeUpdate(requete);
            System.out.println("Personne ajouté");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void ajouterPersonne(Personne personne){
        try {
        String requete = "INSERT INTO personne (nom,prenom)" +
                "VALUES (?,?)";


            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, personne.getNom());
            pst.setString(2, personne.getPrenom());
            pst.executeUpdate();
            System.out.println("Person added");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Personne personne) {

    }

    @Override
    public void updateEntity(Personne personne, int id) {

    }

    @Override
    public List<Personne> getAllData() {
        List<Personne> data = new ArrayList<>();
        try {
        String requete = "SELECT * FROM personne";

        Statement st = MyConnection.getInstance().getCnx().createStatement();
        ResultSet rs = st.executeQuery(requete);

           while(rs.next()){
               Personne p = new Personne();
               p.setId(rs.getInt(1));
               p.setNom(rs.getString("nom"));
               p.setPrenom(rs.getString("prenom"));
                data.add(p);
           }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }
}
