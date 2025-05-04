package edu.connection.services;

import edu.connection.entities.Personne;
import edu.connection.interfaces.Iservices;
import edu.connection.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonneService implements Iservices<Personne> {
    @Override
    public void addEntity(Personne personne) {
        String requette = "insert into personne (nom,prenom)"+"values ('"+personne.getNom()+"','"+personne.getPrenom()+"')";
    }


    public void ajouter(Personne personne) {
        try{
        String requette = "INSERT INTO personne (nom,prenom)"+"values (?,?)";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requette);
            pst.setString(1, personne.getNom());
            pst.setString(2, personne.getPrenom());
            pst.executeUpdate();
            System.out.println("personne");
    }
    catch(Exception e){
        }
 }
public List<Personne> getList(){
        List<Personne> data = new ArrayList<>();
        try {
            String requette = "select * from personne";
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs=st.executeQuery(requette);
            while (rs.next()) {
                Personne personne = new Personne();
                personne.setId(rs.getInt(1));
                personne.setNom(rs.getString("nom"));
                personne.setPrenom(rs.getString("prenom"));
                data.add(personne);
            }
        } catch (SQLException e) {
            System.out.printf(e.getMessage());        }
        return data;
}
    @Override
    public void deleteEntity(Personne personne) {

    }

    @Override
    public void updateEntity(Personne personne, int id) {

    }


}