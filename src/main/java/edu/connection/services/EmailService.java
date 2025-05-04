package edu.connection.services;

import edu.connection.entities.Produit;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class EmailService {

    public static void sendOrderConfirmationEmail(String toEmail, Map<Produit, Integer> panier, double total) {
        // Paramètres de la session SMTP
        String host = "smtp.gmail.com";
        String username = "anisleila2@gmail.com";
        String password = "egsknvkqnufwzfxj";

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
            message.setSubject("🎉 Confirmation de votre commande chez DataFarm");

            // Créer le contenu HTML du message
            StringBuilder emailContent = new StringBuilder();

            emailContent.append("""
<html>
<head>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap');
    </style>
</head>
<body style="font-family: 'Poppins', Arial, sans-serif; background-color: #f8f9fa; margin: 0; padding: 0;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
        <!-- Header avec image de bannière -->
        <div style="background: linear-gradient(135deg, #4CAF50 0%, #2E7D32 100%); padding: 30px 0; text-align: center; border-radius: 8px 8px 0 0;">
            <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 600;">Merci pour votre commande !</h1>
        </div>
        
        <!-- Contenu principal -->
        <div style="padding: 30px;">
            <p style="font-size: 16px; color: #555; line-height: 1.6;">Bonjour,</p>
            <p style="font-size: 16px; color: #555; line-height: 1.6;">Nous avons bien reçu votre commande et nous vous en remercions. Voici le récapitulatif :</p>
            
            <!-- Tableau des produits -->
            <table style="width: 100%; border-collapse: collapse; margin: 25px 0;">
                <thead>
                    <tr style="background-color: #f5f5f5;">
                        <th style="padding: 12px 15px; text-align: left; font-weight: 500; color: #333; border-bottom: 2px solid #e0e0e0;">Produit</th>
                        <th style="padding: 12px 15px; text-align: center; font-weight: 500; color: #333; border-bottom: 2px solid #e0e0e0;">Qté</th>
                        <th style="padding: 12px 15px; text-align: right; font-weight: 500; color: #333; border-bottom: 2px solid #e0e0e0;">Prix</th>
                    </tr>
                </thead>
                <tbody>
""");

            // Ajout dynamique des lignes de produits commandés
            for (Map.Entry<Produit, Integer> entry : panier.entrySet()) {
                Produit produit = entry.getKey();
                int quantite = entry.getValue();
                double prixTotal = produit.getPrix() * quantite;

                emailContent.append("<tr>")
                        .append("<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; color: #333;'>").append(produit.getNom()).append("</td>")
                        .append("<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; text-align: center; color: #555;'>").append(quantite).append("</td>")
                        .append("<td style='padding: 12px 15px; border-bottom: 1px solid #e0e0e0; text-align: right; color: #333;'>").append(String.format("%.2f tnd", prixTotal)).append("</td>")
                        .append("</tr>");
            }

            // Ajout du total et du pied de page
            emailContent.append("""
                </tbody>
            </table>
            
            <!-- Total -->
            <div style="text-align: right; margin-top: 20px;">
                <div style="display: inline-block; background-color: #f5f5f5; padding: 15px 25px; border-radius: 6px;">
                    <span style="font-size: 16px; color: #555; margin-right: 15px;">Total</span>
                    <span style="font-size: 20px; font-weight: 600; color: #2E7D32;">""")
                    .append(String.format("%.2f tnd", total)).append("""
                    </span>
                </div>
            </div>
            
            <!-- Message de remerciement -->
            <div style="margin-top: 40px; padding: 20px; background-color: #f9f9f9; border-radius: 6px; text-align: center;">
                <p style="font-size: 15px; color: #555; margin-bottom: 15px;">Votre commande est en cours de préparation. Vous recevrez un email lorsque celle-ci sera expédiée.</p>
                <p style="font-size: 15px; color: #555;">Pour toute question, contactez-nous à <a href="mailto:contact@datafarm.com" style="color: #4CAF50; text-decoration: none;">contact@datafarm.com</a></p>
            </div>
            
            <!-- Bouton vers le site -->
            <div style="text-align: center; margin-top: 30px;">
                <a href="https://www.datafarm.com" style="display: inline-block; background-color: #4CAF50; color: white; text-decoration: none; padding: 12px 25px; border-radius: 4px; font-weight: 500;">Visiter notre site</a>
            </div>
        </div>
        
        <!-- Footer -->
        <div style="background-color: #f5f5f5; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 13px; color: #777;">
            <p style="margin: 5px 0;">© 2023 DataFarm. Tous droits réservés.</p>
            <p style="margin: 5px 0;">123 Rue de l'Agriculture, 75000 Paris</p>
            <div style="margin-top: 15px;">
                <a href="#" style="margin: 0 10px; color: #4CAF50; text-decoration: none;">Conditions générales</a>
                <a href="#" style="margin: 0 10px; color: #4CAF50; text-decoration: none;">Politique de confidentialité</a>
            </div>
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