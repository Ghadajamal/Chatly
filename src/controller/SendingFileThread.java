package controller;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;


public class SendingFileThread extends Thread {

    String sender, receiver;
    String filePath;
    Socket socketOfSender;
    BufferedWriter bw;
    BufferedReader br;
    JProgressBar progressBar;
    SendFileFrame frameToDisplayDialog;

    private final int BUFFER_SIZE = 1024;
    
    public SendingFileThread(String sender, String receiver, String filePath, Socket socket, SendFileFrame frameToDisplayDialog, JProgressBar progressBar) {
        this.sender = sender;
        this.receiver = receiver;
        this.filePath = filePath;
        this.socketOfSender = socket;
        this.frameToDisplayDialog = frameToDisplayDialog;
        this.progressBar = progressBar;
        
        try {
            bw = new BufferedWriter(new OutputStreamWriter(socketOfSender.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfSender.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(SendingFileThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void sendToServer(String line) {
        try {
            this.bw.write(line);
            this.bw.newLine();   
            this.bw.flush();
        } catch (java.net.SocketException e) {
            JOptionPane.showMessageDialog(null, "Serveur pas encore lancé, impossible d'envoyer un message", "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ClientFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    public void run() {
        FileInputStream fis = null;
        BufferedOutputStream bos = null;
        try {
            File file = new File(filePath);     
            int leng = (int) file.length();     
            
            //this.sendToServer("CMD_SENDFILETOSERVER|"+sender+"|"+receiver+"|"+file.getName()+"|"+leng);
            this.sendToServer("CMD_SENDFILETOSERVER|" + sender + "|" + receiver + "|" + file.getName() + "|" + leng + "|" + "image/png");

            
            System.out.println("[SendingFileThread.java] CMD_SENDFILETOSERVER|"+sender+"|"+receiver+"|"+file.getName()+"|" +leng + "|" + "image/png");   
            
           /* BufferedImage image = ImageIO.read(file);
            ImageIO.write(image, "jpg", socketOfSender.getOutputStream());
            */
            
            fis = new FileInputStream(file);
            bos = new BufferedOutputStream(socketOfSender.getOutputStream());
            
            byte []buffer = new byte[BUFFER_SIZE];
            int count=0, percent=0;
            /*
            while ((count = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
                percent = percent + count;
                progressBar.setValue(percent / leng);
             }*/
            /*while((count = fis.read(buffer)) > 0) {
                //percent = percent + count;
                bos.write(buffer, 0, count);    
                //progressBar.setValue(percent/leng);
            }*/
            
            
            //while loop
            
            while ((count = fis.read(buffer)) > 0) {
           bos.write(buffer, 0, count);

                // Send the image data to the server
                ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, count);
                BufferedImage image = ImageIO.read(bais);
                ImageIO.write(image, "png", socketOfSender.getOutputStream());

                // Update the progress bar
                percent = percent + count;
                progressBar.setValue(percent / leng);
                }
            
        } catch (IOException ex) {
            Logger.getLogger(SendingFileThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if(fis != null) fis.close();
                if(bos != null) bos.close();
                socketOfSender.close();
            } catch (IOException ex) {
                Logger.getLogger(SendingFileThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        JOptionPane.showMessageDialog(frameToDisplayDialog, "Fichier envoyé avec succès", "Envoie réussi", JOptionPane.INFORMATION_MESSAGE);  
    }
    
}