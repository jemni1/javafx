package services;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Authenticator;

import java.io.IOException;
import java.util.Properties;

public class MailUtil {

    private static final String EMAIL_EXPEDITEUR = "rayenghrairi53@gmail.com"; // Remplacer par ton email
    private static final String MOT_DE_PASSE = "rgfl qnhm qahv lxsr"; // Mot de passe ou app password

    public static void envoyerEmail(String destinataire, String sujet, String contenu) {
        // Configuration des propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Création de la session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });

        try {
            // Création du message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setContent(contenu, "text/html; charset=UTF-8");  // Envoi du contenu en HTML

            // Partie image (logo)
            MimeBodyPart imagePart = new MimeBodyPart();
            String logoPath = "src/main/resources/logo.png";  // Chemin vers l'image sur ton système
            imagePart.attachFile(logoPath);
            imagePart.setContentID("<logoImage>");
            imagePart.setDisposition(MimeBodyPart.INLINE);

            // Envoi du message
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à " + destinataire);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

