package edu.connection.tests;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import edu.connection.entities.Commande;
import edu.connection.entities.PanierItem;
import edu.connection.entities.Personne;
import edu.connection.services.CommandeService;
import edu.connection.services.PanierService;
import edu.connection.services.PersonneService;
import edu.connection.entities.Produit;
import edu.connection.services.ProduitService;
import edu.connection.tools.MyConnection;

import java.sql.SQLException;

public class TestClass {

    public static void main(String[] args) {
        // Mets ici ta clé secrète de test

        try {
            // Essaye de récupérer les infos du compte Stripe
            Account account = Account.retrieve();
            System.out.println("✅ Connexion réussie !");
            System.out.println("ID compte Stripe : " + account.getId());
        } catch (StripeException e) {
            System.out.println("❌ Connexion échouée !");
            e.printStackTrace();
        }
    }
}