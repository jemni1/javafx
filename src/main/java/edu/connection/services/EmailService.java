package edu.connection.services;

import edu.connection.entities.Produit;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class EmailService {

    public static void sendOrderConfirmationEmail(String toEmail, Map<Produit, Integer> panier, double total) {
        // Paramètres de la session SMTP (utilisez votre fournisseur SMTP)
        String host = "smtp.gmail.com";  // Par exemple, pour Gmail
        String username = "anisleila2@gmail.com";  // Votre e-mail
        String password = "egsknvkqnufwzfxj";  // Votre mot de passe

        // Propriétés de la session
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

        // Créer une session authentifiée
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Créer un objet message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Confirmation de votre commande");

            // Créer le contenu HTML du message
            StringBuilder emailContent = new StringBuilder();

            emailContent.append("""
<html>
<body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f0f9f0; margin: 0; padding: 20px; color: #1a472a;">
  <div style="width: 90%; max-width: 800px; margin: 20px auto; background-color: #ffffff; padding: 40px; box-shadow: 0 4px 15px rgba(46, 125, 50, 0.1); border-radius: 15px;">
    <h1 style="text-align: center; color: #2e7d32;">Confirmation de votre commande</h1>
    <p>Bonjour,</p>
    <p>Merci pour votre commande ! Voici les détails :</p>
    <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
      <tr>
        <th style="padding: 12px; border-bottom: 1px solid #c8e6c9; text-align: left;">Produit</th>
        <th style="padding: 12px; border-bottom: 1px solid #c8e6c9; text-align: left;">Quantité</th>
        <th style="padding: 12px; border-bottom: 1px solid #c8e6c9; text-align: left;">Prix</th>
      </tr>
""");

// Ajout dynamique des lignes de produits commandés
            for (Map.Entry<Produit, Integer> entry : panier.entrySet()) {
                Produit produit = entry.getKey();
                int quantite = entry.getValue();
                double prixTotal = produit.getPrix() * quantite;

                emailContent.append("<tr>")
                        .append("<td style='padding: 12px; border-bottom: 1px solid #c8e6c9;'>").append(produit.getNom()).append("</td>")
                        .append("<td style='padding: 12px; border-bottom: 1px solid #c8e6c9;'>").append(quantite).append("</td>")
                        .append("<td style='padding: 12px; border-bottom: 1px solid #c8e6c9;'>").append(String.format("%.2f €", prixTotal)).append("</td>")
                        .append("</tr>");
            }

// Ajout du total et du pied de page
            emailContent.append("</table>");
            emailContent.append("<div style='text-align: right; font-size: 20px; color: #2e7d32; margin-top: 30px; font-weight: bold;'>Total : ")
                    .append(String.format("%.2f €", total)).append("</div>");

            emailContent.append("""
    <div style="text-align: center; margin-top: 40px; color: #2e7d32; font-size: 14px;">
      🌿 Merci de votre confiance !<br>
      DataFarm - Votre partenaire de confiance<br>
      <a href="https://www.datafarm.com" style="color: #2e7d32;">www.datafarm.com</a>
    </div>
  </div>
</body>
</html>
""");


            // Définir le contenu HTML dans le message
            message.setContent(emailContent.toString(), "text/html");

            // Envoyer le message
            Transport.send(message);

            System.out.println("E-mail envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
            // Gérer l'erreur ici si l'e-mail n'a pas pu être envoyé
        }
    }
}
