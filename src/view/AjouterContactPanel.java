/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author XPS
 */
import javax.activation.DataSource;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import controller.ClientFrame;
import java.awt.Color;

public class AjouterContactPanel extends JFrame {
   private final JTextField nomTextField;
   
   private final JTextField telephoneTextField;

  /* public AjouterContactPanel() {
      setTitle("Ajouter un contact");

      JLabel nomLabel = new JLabel("Nom :");
      nomTextField = new JTextField(20);
    JLabel emailLabel = new JLabel("email :");
      telephoneTextField = new JTextField(20);

      JButton ajouterButton = new JButton("Ajouter");
      ajouterButton.addActionListener((ActionEvent e) -> {
          ajouterContact();
      });

      JPanel panel = new JPanel(new GridLayout(4, 2));
      panel.add(nomLabel);
      panel.add(nomTextField);
      panel.add(emailLabel );
      panel.add(telephoneTextField);
      panel.add(new JLabel());
      panel.add(ajouterButton);

      setContentPane(panel);
      pack();
      setVisible(true);
   }*/
   
   public AjouterContactPanel() {
    setTitle("Ajouter un contact");

    // Set the background color to white
    getContentPane().setBackground(Color.GREEN);

    JLabel nomLabel = new JLabel("Nom :");
    nomTextField = new JTextField(20);
    JLabel emailLabel = new JLabel("Email :");
    telephoneTextField = new JTextField(20);

    JButton ajouterButton = new JButton("Ajouter");
    ajouterButton.addActionListener((ActionEvent e) -> {
        ajouterContact();
    });

    JPanel panel = new JPanel(new GridLayout(4, 2));
    panel.add(nomLabel);
    panel.add(nomTextField);
    panel.add(emailLabel);
    panel.add(telephoneTextField);
    panel.add(new JLabel());
    panel.add(ajouterButton);

    setContentPane(panel);
    pack();
    setLocationRelativeTo(null); // Center the frame on the screen
    setVisible(true);
}


  private void ajouterContact() {
   // Récupération des valeurs des champs
   String nom = nomTextField.getText();
   String email = telephoneTextField.getText();

   // Vérification si l'email existe déjà dans la base de données
   try {
      Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/chat_db", "root", "");
      PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM user_tb WHERE email = ?");
      statement.setString(1, email);
      java.sql.ResultSet result = statement.executeQuery();
      result.next();
      int count = result.getInt(1);
      if(count == 0){
         // Envoyer une invitation par email
         sendInvitationByEmail(email);
         JOptionPane.showMessageDialog(this, "Une invitation a été envoyée à ce contact par email.", "Invitation envoyée", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
   } catch (SQLException e) {
      JOptionPane.showMessageDialog(this, "Erreur lors de la vérification de l'email : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
      return;
   }

   // Création du contact
   Contact contact = new Contact(nom,email);

   // Insertion dans la base de données
   try {
      Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/chat_db", "root", "");
      PreparedStatement statement = connection.prepareStatement("INSERT INTO contact(nom_contact, email_contact,name) VALUES (?,?,?)");
      statement.setString(1, contact.getNom());
      statement.setString(2, contact.getEmail());
      statement.setString(3, ClientFrame.nickname);
      statement.executeUpdate();
      JOptionPane.showMessageDialog(this, "Le contact a été ajouté avec succès !");
      dispose();
   } catch (SQLException e) {
      JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du contact : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
   }
}

private void sendInvitationByEmail(String email) {
// Paramètres de connexion SMTP

   String smtpUsername = "Send Email";
   String smtpPassword = "reseauapp12";

   // Adresse email de l'expéditeur
   String fromEmail = "chatlyapp12@gmail.com";

   // Adresse email du destinataire
   String toEmail = email;

   // Sujet de l'email
   String subject = "Invitation à rejoindre Chatly";

   // Corps de l'email
   String body = "Bonjour,\n\nVous êtes invité à rejoindre Chatly.\n\nCliquez sur ce lien pour vous inscrire : http://chatly.example.com/register\n\nCordialement,\nL'équipe Chatly";

   // Création de la session SMTP
   Properties props = new Properties();
   props.put("mail.smtp.auth", "true");
   props.put("mail.smtp.starttls.enable", "true");
   props.put("mail.smtp.host", "smtp.gmail.com");
   props.put("mail.smtp.port", "587");

Session session = Session.getInstance(props, new Authenticator() {
   @Override
   protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication("chatlyapp12@gmail.com", "mhviuuuweuzjslqp");
   }
});
 try {
      // Création du message
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(fromEmail));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
      message.setSubject(subject);
      message.setText(body);

      // Envoi du message
      Transport.send(message);
   } catch (MessagingException e) {
      JOptionPane.showMessageDialog(this, "Erreur lors de l'envoi de l'invitation par email : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
   }
} 

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
          AjouterContactPanel ajouterContactFrame = new AjouterContactPanel();
          ajouterContactFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      });
   }
   
}