package edu.connexion3a41.tests;

import edu.connexion3a41.entities.Personne;
import edu.connexion3a41.services.PersonneServices;
import edu.connexion3a41.tools.MyConnection;

public class TestClass {

    public static void main(String[] args) {
        //MyConnection mc = new MyConnection();

        Personne p = new Personne("Jabeur","Ons");
        PersonneServices ps = new PersonneServices();
     //   ps.ajouterPersonne(p);
       // System.out.println(ps.getAllData());

        MyConnection m1 = MyConnection.getInstance();
        MyConnection m2 = MyConnection.getInstance();
        System.out.println(m1.hashCode()+ " - "+ m2.hashCode());
    }
}
