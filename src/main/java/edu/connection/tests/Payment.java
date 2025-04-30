package edu.connection.tests;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
public class Payment {
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
