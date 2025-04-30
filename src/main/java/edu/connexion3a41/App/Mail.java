package edu.connexion3a41.App;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;


public class Mail {
    private static final String SMTP_HOST = "smtp.gmail.com"; // Update if using a different SMTP server
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "your-email@gmail.com"; // Replace with your email
    private static final String SENDER_PASSWORD = "your-app-password"; // Replace with your app-specific password

    private static final String EMAIL_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Your Secure Login Code</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { background-color: #f0f4f8; min-height: 100vh; font-family: 'Helvetica', 'Arial', sans-serif; line-height: 1.6; padding: 20px; }
                .container { max-width: 640px; margin: 0 auto; }
                .card { background: #ffffff; padding: 2rem; border-radius: 12px; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.05); border: 1px solid #e5e7eb; }
                .header { text-align: center; padding-bottom: 1.5rem; border-bottom: 1px solid #f0f0f0; }
                .brand-logo { max-width: 90px; height: auto; margin-bottom: 1rem; }
                .text-primary { color: #2c7be5; }
                .code { font-size: 2.5rem; font-weight: 700; color: #1e40af; text-align: center; letter-spacing: 0.5rem; padding: 1rem; background: #f8fafc; border-radius: 8px; margin: 1.5rem 0; border: 1px dashed #dbeafe; }
                .content { padding: 1.5rem 0; color: #4b5563; font-size: 1rem; }
                .footer { margin-top: 2rem; padding-top: 1rem; border-top: 1px solid #f0f0f0; font-size: 0.875rem; color: #6b7280; text-align: center; }
                @media (max-width: 600px) { .card { padding: 1.5rem; } .code { font-size: 2rem; letter-spacing: 0.3rem; } }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="card">
                    <div class="header">
                        <img src="https://i.postimg.cc/XYzmTTv2/logo.png" alt="Company Logo" class="brand-logo">
                        <h2 class="text-primary">Verification Code</h2>
                    </div>
                    <div class="content">
                        <p style="text-align: center">Hello {{ user.username }},</p>
                        <p>Use this code to complete your login:</p>
                        <div class="code">{{ code }}</div>
                        <p>This code expires in <strong>10 minutes</strong>. Please keep it confidential.</p>
                        <p>If you didn't request this code, please secure your account immediately.</p>
                    </div>
                    <div class="footer">
                        <p>Need help? Contact us at support@datafarm.com</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """;

    public boolean sendVerificationEmail(String recipientEmail, String username, String code) {
        // Set up mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Convert SENDER_PASSWORD String to char[]
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Secure Login Code");

            // Replace placeholders in the template
            String emailContent = EMAIL_TEMPLATE
                    .replace("{{ user.username }}", username)
                    .replace("{{ code }}", code);

            // Set the email content as HTML
            message.setContent(emailContent, "text/html; charset=utf-8");

            // Send the email
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}