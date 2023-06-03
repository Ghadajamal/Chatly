/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * 
 */
//Remarque : l'envoi de fichiers ne peut toujours pas envoyer de fichiers .pdf, uniquement des fichiers .txt, des fichiers image. Je ne comprends pas pourquoi !
//ce thread crée un socket pour communiquer avec un client, disons qu'un client et un serveur n'ont qu'un seul socket les connectant
// donc le serveur veut communiquer avec n'importe quel client, il a besoin d'un socket entre lui et ce client
public class ServerThread extends Thread {
   //note : les différents clients sont distingués par l'attribut socketOfServer, ce qui signifie que chaque nouvel objet client qui se connecte créera
    // 1 nouvel objet ServerThread. Ces objets ServerThread se distinguent par socketOfServer
    Socket socketOfServer;      //socket để nối với socket của client kết nối tới
    BufferedWriter bw;
    BufferedReader br;
    String clientName, clientPass, clientRoom;
    public static Hashtable<String, ServerThread> listUser = new Hashtable<>();
    // le premier paramètre est le nom du client, qui est clientName
    // Le 2ème paramètre est un objet de cette classe. Chaque fois qu'un client demande à se connecter au serveur, le serveur crée un nouvel objet de type ServerThread pour gérer ce client.
    // vous devez donc mettre ce ServerThread dans la liste afin que chaque fois que vous souhaitez envoyer un message à un autre client ou envoyer un message à chaque client, obtenez le ServerThread dans le listUser, et à partir de ce ServerThread
    //on obtient 2 paramètres importants bw et br. bw pour envoyer des données au client et br pour lire les données du client
    public static final String NICKNAME_EXIST = "Cet email est déjà utilisé, veuillez entrer un email correct !";
    public static final String NICKNAME_VALID = "Email accepté";
    public static final String NICKNAME_INVALID = "Email ou nom d'utilisateur incorrecte";
    public static final String SIGNUP_SUCCESS = "Connexion réussie";
    public static final String ACCOUNT_EXIST = "Ce compte existe déjà !";
    
    public JTextArea taServer;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    
    StringTokenizer tokenizer;
    private final int BUFFER_SIZE = 1024;
    
    String senderName, receiverName;    //Noms de 2 gars qui envoient et reçoivent des fichiers
    static Socket senderSocket, receiverSocket;     //Ne recevoir et envoyer temporairement que des fichiers sur 2 clients
    /*
   Remarque : il existe 3 types de sockets serveur :
    - 1 type est créé lorsqu'un client arrive à une connexion normale, maintenant la propriété socketOfServer de cette classe est initialisée, et 2 propriétés senderSocket = receiverSocket = null
    - Un type est créé lorsque l'expéditeur client crée un nouveau socket vers le serveur, à ce moment la propriété socketOfServer de cette classe est également initialisée. mais la propriété senderSocket est également initialisée et receiverSocket = null
    - Un type est créé lorsque le récepteur client crée un nouveau socket vers le serveur, à ce moment la propriété socketOfServer de cette classe est également initialisée. mais la propriété receiverSocket est également initialisée et senderSocket = null
    
    donc les propriétés senderSocket et receiverSocket doivent être statiques pour que pour tous les objets créés, ces deux types ne changent pas
    S'ils ne sont pas statiques, en supposant que le socket de l'expéditeur arrive, un objet de cette classe est créé et a senderSocket = socket de l'expéditeur, mais receiverSocket=null, ce qui signifie que le socket du destinataire n'est pas là, donc le fichier ne peut aller nulle part
    De même, le destinataire a receiverSocket !=null, donc il ne sait pas qui est l'expéditeur
    */
    
    UserDatabase userDB;
    
    static boolean isBusy = false;     //Utilisé pour vérifier si le serveur envoie et reçoit des fichiers
    
    public ServerThread(Socket socketOfServer) {
        this.socketOfServer = socketOfServer;
        this.bw = null;
        this.br = null;
        
        clientName = "";
        clientPass = "";
        clientRoom = "";
        
        userDB = new UserDatabase();
        userDB.connect();
    }
    
    public void appendMessage(String message) {
        taServer.append(message);
        taServer.setCaretPosition(taServer.getText().length() - 1);    
    }
    
    public String recieveFromClient() {
        try {
            return br.readLine();
        } catch (IOException ex) {
            System.out.println(clientName+" s'est déconnecté");
        }
        return null;
    }
    
    public void sendToClient(String response) {    
        try {
            bw.write(response);
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToSpecificClient(ServerThread socketOfClient, String response) {     
        try {
            BufferedWriter writer = socketOfClient.bw;
            writer.write(response);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToSpecificClient(Socket socket, String response) {     
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(response);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void notifyToAllUsers(String message) {
        // principe de fonctionnement : supposons que le client A envoie un message au serveur et que d'autres clients B, C et D se connectent également au serveur
        // le premier serveur obtient socketOfClient dans listUser, socketOfClient obtient le nom correspondant A
        // le serveur obtiendra le nom et le message du client A, puis enverra un message avec le contenu : "A : message" à tous les autres clients via
        //via leur socketOfServer
        //en résumé, le serveur envoie le message "A: message" à A,B,C,D via 4 sockets : socketOfServer de A, socketOfServer de B, socketOfServer de C, socketOfServer de D
        //socketsOfServer stocké dans listUser
        
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
        BufferedWriter writer;
        
        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            writer = st.bw;

            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void notifyToUsersInRoom(String message) {
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
        BufferedWriter writer;
        
        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            if(st.clientRoom.equals(this.clientRoom)) {    
                writer = st.bw;

                try {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void notifyToUsersInRoom(String room, String message) {      
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;
        BufferedWriter writer;
        
        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            if(st.clientRoom.equals(room)) {
                writer = st.bw;

                try {
                    writer.write(message);
                    writer.newLine();
                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void closeServerThread() {
        try {
            br.close();
            bw.close();
            socketOfServer.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public String getAllContacts() {
    StringBuilder kq = new StringBuilder();
    String temp = null;
    
    try {
        // Connect to the database
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_db", "root", "");
        
        // Retrieve all contacts from the "contacts" table
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM contact");
        
        // Append the first contact to the result string
        if(rs.next()) {
            kq.append(rs.getString("nom_contact"));
        }
        
        // Append the remaining contacts to the result string, separated by "|"
        while(rs.next()) {
            temp = rs.getString("nom_contact");
            kq.append("|").append(temp);
        }
        
        // Close the database connection
        conn.close();
    } catch(SQLException e) {
        e.printStackTrace();
    }
    
    return kq.toString();
}*/
     public String getAllUsers() {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        
        Enumeration<String> keys = listUser.keys();
        if(keys.hasMoreElements()) {
            String str = keys.nextElement();
            kq.append(str);
        }
        
        while(keys.hasMoreElements()) {
            temp = keys.nextElement();
            kq.append("|").append(temp);
        }
        
        return kq.toString();
    }
    

    
    public String getUsersThisRoom() {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        ServerThread st;
        Enumeration<String> keys = listUser.keys();
        
        while(keys.hasMoreElements()) {
            temp = keys.nextElement();
            st = listUser.get(temp);
            if(st.clientRoom.equals(this.clientRoom))  kq.append("|").append(temp);
        }
        
        if(kq.equals("")) return "|";
        return kq.toString();   
    }
    
    public String getUsersAtRoom(String room) {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        ServerThread st;
        Enumeration<String> keys = listUser.keys();
        
        while(keys.hasMoreElements()) {
            temp = keys.nextElement();
            st = listUser.get(temp);
            if(st.clientRoom.equals(room))  kq.append("|").append(temp);
        }
        
        if(kq.equals("")) return "|";
        return kq.toString();   
    }
    
    public void clientQuit() {
       
        if(clientName != null) {
            this.appendMessage("\n["+sdf.format(new Date())+"] Client \""+clientName+"\" s'est déconnecté");
            listUser.remove(clientName);
            if(listUser.isEmpty()) this.appendMessage("\n["+sdf.format(new Date())+"] Aucun utilisateur n'est connecté au serveur\n");
            notifyToAllUsers("CMD_ONLINE_USERS|"+getAllUsers());
            notifyToUsersInRoom("CMD_ONLINE_THIS_ROOM"+getUsersThisRoom());
            notifyToUsersInRoom(clientName+" a quitté l'application !");
        }
    }
    
    public void changeUserRoom() {      
        ServerThread st = listUser.get(this.clientName);
        st.clientRoom = this.clientRoom;
        listUser.put(this.clientName, st);   
    }
    
    public void removeUserRoom() {
        ServerThread st = listUser.get(this.clientName);
        st.clientRoom = this.clientRoom;
        listUser.put(this.clientName, st);
    }
    
    @Override
    public void run() {
        try {
            bw = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            
            boolean isUserExist = true;
            String message, sender, receiver, fileName;
            StringBuffer str;
            String cmd, icon;
            while(true) {   
                try {
                    message = recieveFromClient();
                    tokenizer = new StringTokenizer(message, "|");
                    cmd = tokenizer.nextToken();
                    
                    switch (cmd) {
                        case "CMD_CHAT":
                            str = new StringBuffer(message);
                            System.out.println(message);
                            //AES.encrypt(message);
                            str = str.delete(0, 9);
                            notifyToUsersInRoom("CMD_CHAT|" + this.clientName+"|"+str.toString());   
                            break;
                            
                            
                            //Cryptage et décryptage
                        case "CMD_PRIVATECHAT":
                            String privateSender = tokenizer.nextToken();
                            String privateReceiver = tokenizer.nextToken();
                            String messageContent = message.substring(cmd.length()+privateSender.length()+privateReceiver.length()+3, message.length());
                            
                            
                            //ServerThread st_sender = listUser.get(privateSender);
                            ServerThread st_receiver = listUser.get(privateReceiver);
                            //sendToSpecificClient(st_sender, "CMD_PRIVATECHAT|" + privateSender + "|" + messageContent);
                            sendToSpecificClient(st_receiver, "CMD_PRIVATECHAT|" + privateSender + "|" + messageContent);
                            
                            
                            break;
                            
                        case "CMD_ROOM":
                            clientRoom = tokenizer.nextToken();
                            changeUserRoom();
                            notifyToAllUsers("CMD_ONLINE_USERS|"+getAllUsers());
                            notifyToUsersInRoom("CMD_ONLINE_THIS_ROOM"+getUsersThisRoom());
                            notifyToUsersInRoom(clientName+" has just entered!");
                            break;
                            
                        case "CMD_LEAVE_ROOM":
                            String room = clientRoom;
                            clientRoom = "";    
                            removeUserRoom();
                            notifyToUsersInRoom(room, "CMD_ONLINE_THIS_ROOM"+getUsersAtRoom(room));
                            notifyToUsersInRoom(room, clientName+" a quitté le groupe");                               
                            break;
                            
                        case "CMD_CHECK_NAME":
                            clientName = tokenizer.nextToken();
                            clientPass = tokenizer.nextToken();
                            isUserExist = listUser.containsKey(clientName);
                            
                            if(isUserExist) {  
                                sendToClient(NICKNAME_EXIST);
                            }
                            else {  
                                int kq = userDB.checkUser(clientName, clientPass);
                                if(kq == 1) {
                                    sendToClient(NICKNAME_VALID);
                                    
                                    this.appendMessage("\n["+sdf.format(new Date())+"] Client \""+clientName+"\" is connecting to server");
                                    listUser.put(clientName, this);   
                                } else sendToClient(NICKNAME_INVALID);
                            }
                            break;
                            
                        case "CMD_SIGN_UP":
                            String name = tokenizer.nextToken();
                            String pass = tokenizer.nextToken();
                            String email = tokenizer.nextToken();
                            System.out.println("Nom d utilisateur, mot de passe = "+name+" - "+pass);
                            isUserExist = listUser.containsKey(name);
                            
                            if(isUserExist) {
                                sendToClient(NICKNAME_EXIST);
                            } else {
                                int kq = userDB.createUser(new User(name, pass,email));
                                if(kq > 0) {
                                    sendToClient(SIGNUP_SUCCESS);
                                } else sendToClient(ACCOUNT_EXIST);
                            }
                            break;
                            
                        case "CMD_ONLINE_USERS":
                            sendToClient("CMD_ONLINE_USERS|"+getAllUsers());
                            notifyToUsersInRoom("CMD_ONLINE_THIS_ROOM"+getUsersThisRoom());
                            break;
                        
                        case "CMD_SENDFILETOSERVER":  
                            sender = tokenizer.nextToken();
                            receiver = tokenizer.nextToken();
                            fileName = tokenizer.nextToken();
                            int len = Integer.valueOf(tokenizer.nextToken());
                            
                            String path = System.getProperty("user.dir") + "\\sendfile\\" +fileName;
                            

                            BufferedInputStream bis = new BufferedInputStream(socketOfServer.getInputStream());   
                            FileOutputStream fos = new FileOutputStream(path);   
                            
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int count = -1;
                            while((count = bis.read(buffer)) > 0) {  
                                fos.write(buffer, 0, count);         
                            }

                            Thread.sleep(300);
                            bis.close();
                            fos.close();
                            socketOfServer.close();
                      
                            ServerThread stSender = listUser.get(sender);      
                            ServerThread stReceiver = listUser.get(receiver);
                            
                            sendToSpecificClient(stSender, "CMD_FILEAVAILABLE|"+fileName+"|"+receiver+"|"+sender);
                            sendToSpecificClient(stReceiver, "CMD_FILEAVAILABLE|"+fileName+"|"+sender+"|"+sender);
                            
                            isBusy = false;
                            break;
                            
                        case "CMD_DOWNLOADFILE":   
                            fileName = tokenizer.nextToken();
                            path = System.getProperty("user.dir") + "\\sendfile\\" + fileName;
                            FileInputStream fis = new FileInputStream(path);
                            BufferedOutputStream bos = new BufferedOutputStream(socketOfServer.getOutputStream());
                            
                            byte []buffer2 = new byte[BUFFER_SIZE];
                            int count2=0;
                            
                            while((count2 = fis.read(buffer2)) > 0) {
                                bos.write(buffer2, 0, count2);   
                            }

                            bos.close();
                            fis.close();
                            socketOfServer.close();
                            
                            break;
                            
                            
                        case "CMD_ICON":
                            icon = tokenizer.nextToken();
                            notifyToUsersInRoom("CMD_ICON|"+icon+"|"+this.clientName);
                            break;
                            
                        default:
                            notifyToAllUsers(message);
                            break;
                    }
                    
                } catch (Exception e) {
                    clientQuit();
                    break;
                }
            }
        } catch (IOException ex) {
            clientQuit();
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        //this.closeServerThread();
    }
}