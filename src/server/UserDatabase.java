package server;
import controller.ClientFrame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UserDatabase {
    private Connection conn;
    public final String DATABASE_NAME = "chat_db";
    public final String USERNAME = "root";
    public final String PASSWORD = "";
    public final String URL_MYSQL = "jdbc:mysql://localhost:3306/"+DATABASE_NAME;
    
    public final String USER_TABLE = "user_tb";
    
    private PreparedStatement pst;
    private ResultSet rs;
    private Statement st;
    
    
    public Connection connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");     //Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
//            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL_MYSQL, USERNAME, PASSWORD);
            System.out.println("Connect successfull");
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error connection!");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    
    public ResultSet getData() {
        try {
            st = conn.createStatement(); 
            rs = st.executeQuery("SELECT * FROM "+USER_TABLE);
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return rs;
    }
    
    private void showData() {
        rs = getData();
        try {
            while(rs.next()) {
                System.out.printf("%-15s %-4s", rs.getString(1), rs.getString(2));
                System.out.println("");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int insertUser(User u) {
        System.out.println("Before: name = "+u.name+" - pass = "+u.pass);
        try {
            pst = conn.prepareCall("INSERT INTO "+USER_TABLE+" VALUES ('"+u.name+"', '"+u.pass+"')");
            int kq = pst.executeUpdate();
            if(kq > 0) System.out.println("Insert successful!");
            System.out.println("After: name = "+u.name+" - pass = "+u.pass);
            return kq;
//        
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    
    //Cryptage
   public int createUser(User u) {
    try {
        pst = conn.prepareStatement("INSERT INTO "+USER_TABLE+" VALUE(?,?,?);");
        pst.setString(1, u.name);
        String encryptedPassword = (u.pass); // chiffrement du mot de passe
        pst.setString(2, encryptedPassword);
        pst.setString(3, u.mail);

        return pst.executeUpdate();
    } catch (SQLException ex) {
        Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception ex) {
        Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
    }
    return -1;
}


    //Décryptage
    public int checkUser(String name, String pass) {
    try {
        pst = conn.prepareStatement("SELECT * FROM "+USER_TABLE+" WHERE name = ?");
        pst.setString(1, name);
        rs = pst.executeQuery();
        
        if(rs.first()) {
            //decrypt password from database
            String Pass = rs.getString("pass");
            
            //compare decrypted password with input password
            if(Pass.equals(pass)) {
                //user and pass is correct:
                return 1;
            }
        }
    } catch (Exception ex) {
        Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
    }
    return 0;
}

    
    
    private List<String> retrieveContactsFromDatabase() {
    List<String> contacts = new ArrayList<>();

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase", "username", "password")) {
        String query = "SELECT nom_contact FROM contacts WHERE nom_user = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, ClientFrame.nickname);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            contacts.add(rs.getString("nom_contact"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return contacts;
}
    
    public void closeConnection() {
        try {
            if(rs!=null) rs.close();
            if(pst!=null) pst.close();
            if(st!=null) st.close();
            if(conn!=null) conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("[UserDatabase.java]  close connection");
        }
    }
    
    
    
    public int insertMessage(String Message, String Sender, String Reciever) {

        try {
            
            pst = conn.prepareCall("INSERT INTO message (Message,Sender,Reciever) VALUES ('"+Message+"', '"+Sender+"' , '"+Reciever+"' )");
            int kq = pst.executeUpdate();
            if(kq > 0) System.out.println("Insert successful!");
            return kq;
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
      public String[] selectMessages(String Sender, String Reciever) {
        String[] Messages = new String[100] ;
        System.out.println("selecting messages");
       
             System.out.println("In the try ??");
             System.out.println("The sender : ??");
             System.out.println(Sender);
             System.out.println("The reciever : ??");
             System.out.println(Reciever);
             try {
        
            String query= "Select Message from message  where sender = '"+Sender+"' and reciever =  '"+Reciever+"' order by IDMessage ASC";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(query);
                int i = 0 ;
                System.out.println("before while ??");
                while (rs.next())
                {
                    System.out.println("why ??????");
                    Messages[i] = rs.getString("Message");
                    System.out.println("on the query  : ");
                    System.out.println(Messages[i]);
                    i++ ;
                    
                }
            }
            catch (SQLException e) {
            System.out.println("SOme Exception2");
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, e);

        } 

         return Messages;
        
    }
      
    public int deleteMessgaes(String Reciever) {

        try {
            pst = conn.prepareCall("delete from  message  where reciever = '"+Reciever+"'");
            int kq = pst.executeUpdate();
            if(kq > 0) System.out.println("Supression effectué!");
            return kq;
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    public static void main(String[] args) {
        UserDatabase ud = new UserDatabase();
        ud.connect();
        ud.showData();
        ud.closeConnection();
        
        System.out.println("============");
    }}
