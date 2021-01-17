//Grevenitis Ioannis icsd13045
//Papaloukas Thomas icsd14155

package privchatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class PrivChatServer {
    protected static ArrayList <ServerThread> clients = new ArrayList();    //Keeps threads corresponding to clients
    protected static ArrayList <String> curse = new ArrayList();    //Curse lexicon
    private static ArrayList <Alias> sudo = new ArrayList();    //Pseudonym list
    protected static ArrayList <String> bannedIP = new ArrayList();     //IP addresses that are banned
    protected static ArrayList <Calendar> bannedTime = new ArrayList();     //Duration for the bans corresponding to the bannedIP ArrayList
    protected static int[] par = new int[4];    //Parameters from the server admin panel that are needed for the ban are kept here
    
    public static void main(String[] args) {
        initAliases();
        initCurses();
        ServerUI ui = new ServerUI();
        
        Keystore ks = new Keystore(); //Create Keystore
        Truststore ts = new Truststore();      //Create Truststore
        CA CAcert = new CA(); //Create CA object
        
        if(!ks.CheckRootCert()||!ts.CheckRootCert()){
            new File("Truststore.jks").delete();
            new File("Keystore.jks").delete();
            deleteFolder(new File("Certificates"));
            ks = new Keystore();
            ts = new Truststore();
            KeyPair keypair = CAcert.GenKeyPair();
            X509Certificate CA = CAcert.BuildCA(keypair);   //Create CA certificate
            CAcert.ExportCert(CA, ks, ts, keypair);       //Export CA certificate
        }
        if(!new File("Certificates").exists()){
            makeCert(CAcert, "Server", ks);
        }
        
        
        try{
            
            ServerSocket serverSock = new ServerSocket(9999);
            
            while(true){
                System.out.println("Accepting connection...");
                Socket clientSock = serverSock.accept();    //Client socket
                DataInputStream in = new DataInputStream(clientSock.getInputStream());  //Client input stream
                DataOutputStream out = new DataOutputStream(clientSock.getOutputStream());  //Client output stream
                String username = chooseAlias();    //Client username
                ServerThread thread = new ServerThread(clientSock, in, out, username);  //Create thread for client
                clients.add(thread);    //Add client's thread to our "clients" ArrayList
                thread.start();     //Start client's thread
            }
            
        }
        catch(IOException ex){
            System.out.println("Server error. Server shuting down.");
            System.exit(-1);
        }
    }
    
    //Method to initialize the sudo ArrayList
    public static void initAliases(){
        Alias al1 = new Alias("Haxor");
        sudo.add(al1);
        Alias al2 = new Alias("PapaJohn");
        sudo.add(al2);
        Alias al3 = new Alias("PappaTom");
        sudo.add(al3);
        Alias al4 = new Alias("Alexa");
        sudo.add(al4);
        Alias al5 = new Alias("Despacito");
        sudo.add(al5);
        Alias al6 = new Alias("Senpai");
        sudo.add(al6);
        Alias al7 = new Alias("GrassHOPPA");
        sudo.add(al7);
        Alias al8 = new Alias("SuppaHotFire");
        sudo.add(al8);
        Alias al9 = new Alias("PewDiePie");
        sudo.add(al9);
        Alias al10 = new Alias("Ligma");
        sudo.add(al10);
        Alias al11 = new Alias("Slugma");
        sudo.add(al11);
        Alias al12 = new Alias("Pikachu");
        sudo.add(al12);
    }
    
    //Method to initialize the curse ArrayList. Used as a lexicon for the profanity filter
    public static void initCurses(){
        curse.add("fuck");
        curse.add("bitch");
        curse.add("pussy");
        curse.add("nigger");
        curse.add("anal");
        curse.add("asshole");
        curse.add("cunt");
        curse.add("cocksucker");
    }
    
    //Method for random pseudonym selection
    public static String chooseAlias(){
        int choice;
        Random rand = new Random();
        
        do{
            choice = rand.nextInt(sudo.size()-1);
        }while(sudo.get(choice).getInUse() == true);
        
        sudo.get(choice).changeAvailability(true);
        return sudo.get(choice).getAlias();
    }
    
    //Method that changes the user's pseudonym at his request.
    public static void changeAlias(String username){
        int choice;
        Random rand = new Random();
        String currUser = username; //Keep the current username
        int index=0;
        
        //Search the clients ArrayList for the thread that corresponds to the username and keep the index
        for(int i=0;i<clients.size();i++){
            if(clients.get(i).getUsername().equals(currUser)){
                index = i;
            }
        }
        
        //Choose a random pseudonym until we find one that is not being used.
        do{
            choice = rand.nextInt(sudo.size()-1);
        }while(sudo.get(choice).getInUse() == true);
        
        //Change availability of previous username to false(available)
        for(int i=0; i<sudo.size(); i++){
            if(sudo.get(i).getAlias().equals(currUser)){
                sudo.get(i).changeAvailability(false);
            }
        }
        
        sudo.get(choice).changeAvailability(true);  //Change availability of new username to true(not available)
        clients.get(index).setUsername(sudo.get(choice).getAlias());    //Set new pseudonym as username to the corresponding thread
        clients.get(index).sendMessage(clients.get(index).getUsername());   //Send the new pseudonym to the client that requested the change as a message, to make the necessary changes on the client side       
    }
    
    //Prints the ArrayList clients. Debugging purposes
    public void displayClients(){
        System.out.println("\nClients ArrayList:");
        for(int i=0;i<clients.size();i++){
            System.out.println(clients.get(i).toString());
        }
        System.out.println();
    }
    
    //Prints the ArrayList sudo. Debugging purposes
    public void displaySudo(){
        System.out.println("\nSudo ArrayList:");
        for(int i=0;i<sudo.size();i++){
            System.out.println(sudo.get(i).toString());
        }
        System.out.println();
    }
    
    public static boolean fileExists(String file){
        return Files.exists(Paths.get(file), LinkOption.NOFOLLOW_LINKS);
    }
    
    public static void makeCert(CA CACert, String username, Keystore ks){
        KeyPair keypair=CACert.GenKeyPair();
        CACert.RegisterEntries(username, keypair, ks);
    }
    
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    
}
