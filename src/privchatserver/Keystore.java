
package privchatserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class Keystore {
    private static final char[] PW = "Sec2018".toCharArray(); //keystore password to be used
    public KeyStore ks;
    
    public Keystore(){ //KeyStore Constructor
        try{
            ks=KeyStore.getInstance("JKS");    //We request a Keystore object of JKS type
            
            if (PrivChatServer.fileExists("Keystore.jks"))
                try(FileInputStream fis=new FileInputStream("Keystore.jks")){    //Try to open Keystore.jks
                    System.out.println("Keystore already exists.");
                    ks.load(fis,PW);    //load keystore using fis and password
                    System.out.println("Keystore loaded.");
                }catch(IOException ex){ //If IOexception, file could not be opened
                    System.out.println("Error opening keystore file (read).");
                }
            else
                try(FileOutputStream fos=new FileOutputStream("Keystore.jks")){    //Try to open Keystore.jks
                    System.out.println("Keystore does not exist.");
                    ks.load(null,PW);   //Create empty keystore with empty input stream and the password
                    ks.store(fos,PW);   //Write keystore to the fos and protect its integrity using the password
                    System.out.println("A new Keystore was created.");
                }catch(IOException ex){ //If IOexception, file could not be opened
                    System.out.println("Error opening keystore file (write).");
                }
        }catch(KeyStoreException | NoSuchAlgorithmException | CertificateException ex){
            System.out.println("Error creating keystore.");
        }
    }
    
    public Key getKey(String str) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException{
        return ks.getKey(str, PW);
    }
    
    public void AddEntry(X509Certificate trustedCert, KeyPair keyPair){
        try(FileOutputStream fos=new FileOutputStream("KeyStore.jks")){
            ks.setCertificateEntry("Root CA", trustedCert);    //Add a certificate entry to the keystore
            System.out.println("Certificate loaded.");
            
            ks.setKeyEntry("PrivateCA", keyPair.getPrivate(), PW, new Certificate[]{trustedCert});  //Add CA's private key to the keystore, passing a certificate chain that includes our root CA cert
            System.out.println(keyPair.getPrivate());   //TODO
            
            ks.store(fos,PW);
            System.out.println("Keystore stored away");
        }catch(IOException ex){
            System.out.println("Error opening keystore file (write).");
        }catch(KeyStoreException | NoSuchAlgorithmException | CertificateException ex){
            System.out.println("Error saving keystore.");
        }
    }
    
    public boolean CheckRootCert(){
        try {
            return(ks.containsAlias("Root CA"));    //Check if the root certificate's alias exists and returns boolean
        } catch (KeyStoreException ex) {
            System.out.println("Error checking root certificate.");
            return false;
        }
    }
}
