package com.healthlink.Services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class EmailService {

    private final String fromEmail = "amenichakroun62@gmail.com";
    private final String appPassword = "njgr etcs umzb najh";

    public void sendWelcomeEmail(String toEmail, String password, String nom, String prenom) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "HealthLink"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Bienvenue sur HealthLink");

            String htmlContent = generateHtml(toEmail, password, nom, prenom);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendApprovalEmail(String toEmail, String nom, String prenom) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "HealthLink"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Votre compte a été approuvé - HealthLink");

            String htmlContent = generateApprovalHtml(nom, prenom);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email d'approbation envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResetCodeEmail(String toEmail, String nom, String prenom, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "HealthLink"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Code de réinitialisation - HealthLink");

            String htmlContent = generateResetCodeHtml(nom, prenom, code);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email de réinitialisation envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPrescriptionEmail(String toEmail, String nom, String prenom, byte[] pdfBytes) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "HealthLink"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Votre Prescription Médicale - HealthLink");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            String htmlContent = generatePrescriptionHtml(nom, prenom);
            messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            // Create the attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(pdfBytes, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("Prescription.pdf");

            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            // Set the multipart message to the email
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email avec prescription envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateHtml(String email, String password, String nom, String prenom) {
        String logoUrl = "https://i.imgur.com/RpVsOcX.png";
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Bienvenue sur HealthLink</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #eef5f9; margin: 0; padding: 0; text-align: center; }\n" +
                "        .container { background-color: #ffffff; max-width: 600px; margin: 20px auto; padding: 40px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }\n" +
                "        .logo { max-width: 180px; margin: 20px auto; display: block; }\n" +
                "        h1 { color: #1B4F72; font-size: 30px; margin-bottom: 20px; }\n" +
                "        p { color: #333; font-size: 16px; line-height: 1.6; }\n" +
                "        .info { background-color: #f0f8ff; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: left; border-left: 5px solid #1B4F72; }\n" +
                "        .info p { margin: 5px 0; font-size: 16px; color: #333; }\n" +
                "        .btn { display: inline-block; background-color: #1B4F72; color: white !important; padding: 14px 40px; text-decoration: none; border-radius: 8px; font-size: 18px; margin-top: 20px; font-weight: bold; }\n" +
                "        .footer { margin-top: 30px; font-size: 14px; color: #666; }\n" +
                "        .footer a { color: #1B4F72; text-decoration: none; font-weight: bold; }\n" +
                "        .highlight { color: #1B4F72; font-weight: bold; }\n" +
                "        .header { background-color: #1B4F72; padding: 20px; border-radius: 10px 10px 0 0; color: white; }\n" +
                "        .header h1 { color: white; margin: 0; }\n" +
                "        .alert { background-color: #ffeded; color: #d9534f; padding: 15px; border-radius: 8px; margin-top: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Bienvenue sur HealthLink</h1>\n" +
                "        </div>\n" +
                "\n" +
                "        <img src=\"" + logoUrl + "\" alt=\"HealthLink Logo\" class=\"logo\">\n" +
                "        <p><strong>Bonjour " + prenom + " " + nom + ",</strong></p>\n" +
                "        <p>Nous sommes ravis de vous accueillir sur <span class=\"highlight\">HealthLink</span>, votre plateforme de santé en ligne.</p>\n" +
                "        <p>Votre compte a été créé avec succès. Voici vos informations de connexion :</p>\n" +
                "\n" +
                "        <div class=\"info\">\n" +
                "            <p><strong>Email :</strong> " + email + "</p>\n" +
                "            <p><strong>Mot de passe :</strong> " + password + "</p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"alert\">\n" +
                "            Pour des raisons de sécurité, veuillez modifier votre mot de passe lors de votre première connexion.\n" +
                "        </div>\n" +
                "\n" +
                "        <a href=\"#\" class=\"btn\">Se connecter via l'application</a>\n" +
                "\n" +
                "        <p class=\"footer\">\n" +
                "            <strong>Conseil de sécurité :</strong> Ne partagez jamais votre mot de passe avec qui que ce soit.\n" +
                "        </p>\n" +
                "\n" +
                "        <p class=\"footer\">\n" +
                "            Si vous avez des questions, contactez-nous à <a href=\"mailto:contact@healthlink.com\">contact@healthlink.com</a>\n" +
                "            ou appelez-nous au <span class=\"highlight\">+216 XX XXX XXX</span>.\n" +
                "        </p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateApprovalHtml(String nom, String prenom) {
        String logoUrl = "https://i.imgur.com/RpVsOcX.png";
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Votre compte a été approuvé - HealthLink</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #eef5f9; margin: 0; padding: 0; text-align: center; }\n" +
                "        .container { background-color: #ffffff; max-width: 600px; margin: 20px auto; padding: 40px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }\n" +
                "        .logo { max-width: 180px; margin: 20px auto; display: block; }\n" +
                "        h1 { color: #1B4F72; font-size: 30px; margin-bottom: 20px; }\n" +
                "        p { color: #333; font-size: 16px; line-height: 1.6; }\n" +
                "        .info { background-color: #f0f8ff; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: left; border-left: 5px solid #1B4F72; }\n" +
                "        .info p { margin: 5px 0; font-size: 16px; color: #333; }\n" +
                "        .btn { display: inline-block; background-color: #1B4F72; color: white !important; padding: 14px 40px; text-decoration: none; border-radius: 8px; font-size: 18px; margin-top: 20px; font-weight: bold; }\n" +
                "        .footer { margin-top: 30px; font-size: 14px; color: #666; }\n" +
                "        .footer a { color: #1B4F72; text-decoration: none; font-weight: bold; }\n" +
                "        .highlight { color: #1B4F72; font-weight: bold; }\n" +
                "        .header { background-color: #1B4F72; padding: 20px; border-radius: 10px 10px 0 0; color: white; }\n" +
                "        .header h1 { color: white; margin: 0; }\n" +
                "        .alert { background-color: #ffeded; color: #d9534f; padding: 15px; border-radius: 8px; margin-top: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Votre compte a été approuvé</h1>\n" +
                "        </div>\n" +
                "\n" +
                "        <img src=\"" + logoUrl + "\" alt=\"HealthLink Logo\" class=\"logo\">\n" +
                "\n" +
                "        <p>Bonjour <span class=\"highlight\">" + nom + " " + prenom + "</span>,</p>\n" +
                "        <p>Nous sommes ravis de vous informer que votre compte sur <span class=\"highlight\">HealthLink</span> a été approuvé par notre équipe.</p>\n" +
                "        <p>Vous pouvez désormais vous connecter et accéder à toutes les fonctionnalités de la plateforme.</p>\n" +
                "\n" +
                "        <a href=\"#\" class=\"btn\">Se connecter via l'application</a>\n" +
                "\n" +
                "        <p class=\"footer\">\n" +
                "            <strong>Conseil de sécurité :</strong> Ne partagez jamais votre mot de passe avec qui que ce soit.\n" +
                "        </p>\n" +
                "\n" +
                "        <p class=\"footer\">\n" +
                "            Si vous avez des questions ou besoin d'aide, n'hésitez pas à nous contacter à\n" +
                "            <a href=\"mailto:contact@healthlink.com\">contact@healthlink.com</a>\n" +
                "            ou appelez-nous au <span class=\"highlight\">+216 XX XXX XXX</span>.\n" +
                "        </p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateResetCodeHtml(String nom, String prenom, String code) {
        String logoUrl = "https://i.imgur.com/RpVsOcX.png";
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Réinitialisation de mot de passe</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #eef5f9; margin: 0; padding: 0; text-align: center; }\n" +
                "        .container { background-color: #ffffff; max-width: 600px; margin: 20px auto; padding: 40px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }\n" +
                "        .logo { max-width: 180px; margin: 20px auto; display: block; }\n" +
                "        h1 { color: #1B4F72; font-size: 30px; margin-bottom: 20px; }\n" +
                "        p { color: #333; font-size: 16px; line-height: 1.6; }\n" +
                "        .code { font-size: 28px; letter-spacing: 5px; background-color: #f0f8ff; padding: 15px; border-radius: 8px; display: inline-block; margin: 20px 0; color: #1B4F72; font-weight: bold; }\n" +
                "        .btn { display: inline-block; background-color: #1B4F72; color: white !important; padding: 14px 40px; text-decoration: none; border-radius: 8px; font-size: 18px; margin-top: 20px; font-weight: bold; }\n" +
                "        .footer { margin-top: 30px; font-size: 14px; color: #666; }\n" +
                "        .header { background-color: #1B4F72; padding: 20px; border-radius: 10px 10px 0 0; color: white; }\n" +
                "        .header h1 { color: white; margin: 0; }\n" +
                "        .warning { background-color: #fff3cd; color: #856404; padding: 15px; border-radius: 8px; margin: 20px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Réinitialisation de mot de passe</h1>\n" +
                "        </div>\n" +
                "\n" +
                "        <img src=\"" + logoUrl + "\" alt=\"HealthLink Logo\" class=\"logo\">\n" +
                "\n" +
                "        <p>Bonjour " + nom + " " + prenom + ",</p>\n" +
                "        <p>Vous avez demandé à réinitialiser votre mot de passe. Voici votre code de vérification :</p>\n" +
                "\n" +
                "        <div class=\"code\">" + code + "</div>\n" +
                "\n" +
                "        <div class=\"warning\">\n" +
                "            <p>Ce code est valable pendant 24 heures. Ne le partagez avec personne.</p>\n" +
                "        </div>\n" +
                "        <p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>\n" +
                "\n" +
                "        <p class=\"footer\">\n" +
                "            L'équipe HealthLink<br>\n" +
                "            <a href=\"mailto:contact@healthlink.com\">contact@healthlink.com</a>\n" +
                "        </p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generatePrescriptionHtml(String nom, String prenom) {
        String logoUrl = "https://i.imgur.com/RpVsOcX.png";
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Votre Prescription Médicale</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #eef5f9; margin: 0; padding: 0; text-align: center; }\n" +
                "        .container { background-color: #ffffff; max-width: 600px; margin: 20px auto; padding: 40px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); }\n" +
                "        .logo { max-width: 180px; margin: 20px auto; display: block; }\n" +
                "        h1 { color: #1B4F72; font-size: 30px; margin-bottom: 20px; }\n" +
                "        p { color: #333; font-size: 16px; line-height: 1.6; }\n" +
                "        .footer { margin-top: 30px; font-size: 14px; color: #666; }\n" +
                "        .footer a { color: #1B4F72; text-decoration: none; font-weight: bold; }\n" +
                "        .highlight { color: #1B4F72; font-weight: bold; }\n" +
                "        .header { background-color: #1B4F72; padding: 20px; border-radius: 10px 10px 0 0; color: white; }\n" +
                "        .header h1 { color: white; margin: 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Votre Prescription Médicale</h1>\n" +
                "        </div>\n" +
                "\n" +
                "        <img src=\"" + logoUrl + "\" alt=\"HealthLink Logo\" class=\"logo\">\n" +
                "\n" +
                "        <p>Bonjour <span class=\"highlight\">" + nom + " " + prenom + "</span>,</p>\n" +
                "        <p>Veuillez trouver ci-joint votre prescription médicale en format PDF.</p>\n" +
                "        <p>Si vous avez des questions, n'hésitez pas à nous contacter.</p>\n" +
                "\n" +
                "        <p class=\"footer\">\n" +
                "            L'équipe HealthLink<br>\n" +
                "            <a href=\"mailto:contact@healthlink.com\">contact@healthlink.com</a>\n" +
                "            ou appelez-nous au <span class=\"highlight\">+216 24 672 776</span>.\n" +
                "        </p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}