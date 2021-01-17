
package privchatserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Truststore {
    private static final char[] PW = "sec2018".toCharArray(); //truststore password to be used
    private KeyStore ts;
    
    Truststore(){        
        try{
            ts=KeyStore.getInstance("JKS");    //We request a Keystore object of JKS type
            
            if(PrivChatServer.fileExists("Truststore.jks"))           //If Truststore.jks doesn't exist or the CA Root cert's alias does not exist in our keystore
                try(FileInputStream fis=new FileInputStream("Truststore.jks")){     //Try to open Truststore.jks
                    System.out.println("Truststore already exists.");
                    ts.load(fis,PW);    //load truststore using fis and password
                    System.out.println("Truststore loaded.");
                }catch(IOException ex){ //If IOexception, file could not be opened
                    System.out.println("Error opening truststore file (read).");
                }
            else
                try(FileOutputStream fos=new FileOutputStream("Truststore.jks")){    //Try to open Truststore.jks
                    System.out.println("Truststore does not exist.");
                    ts.load(null,PW);   //Create empty truststore with empty input stream and the password
                 
                    ts.store(fos,PW);   //Write truststore to the fos and protect its integrity using the password
                    System.out.println("A new Truststore was created.");
                }catch(IOException ex){ //If IOexception, file could not be opened
                    System.out.println("Error opening truststore file (write).");
                }
        }catch(KeyStoreException | NoSuchAlgorithmException | CertificateException ex){
            System.out.println("Error creating truststore.");
        }
    }
    
    public void AddEntry(X509Certificate trustedCert, KeyPair keyPair){
        try(FileOutputStream fos=new FileOutputStream("Truststore.jks")){
            ts.setCertificateEntry("Root CA", trustedCert);
            System.out.println("Certificate loaded.");
                    
            ts.setKeyEntry("PrivateCA", keyPair.getPrivate(), PW, new Certificate[]{trustedCert});
            System.out.println("CA's private key loaded.");
    
            ts.store(fos,PW);
            System.out.println("Truststore stored away");
        }catch(IOException ex){
            System.out.println("Error opening truststore file (write).");
        }catch(KeyStoreException | NoSuchAlgorithmException | CertificateException ex){
            System.out.println("Error saving truststore.");
        }
    }
        
    public boolean CheckRootCert(){
        try {
            return(ts.containsAlias("Root CA"));    //Check if the root certificate's alias exists and returns boolean
        } catch (KeyStoreException ex) {
            System.out.println("Error checking root certificate.");
            return false;
        }
    }
    
    public boolean isValid(String username){
        System.out.print("Is certificate for \""+username+"\" valid?: ");
        if( Files.exists(Paths.get("Users/"+username+"/"+username+".der"), NOFOLLOW_LINKS) ){
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                FileInputStream fis=new FileInputStream("Users/"+username+"/"+username+".der");
                X509Certificate userCert = (X509Certificate) certFactory.generateCertificate(fis);
                X509Certificate CACert = (X509Certificate) ts.getCertificate("Root CA");
                userCert.verify(CACert.getPublicKey());
                System.out.println("true");
                return true;
            } catch (CertificateException | FileNotFoundException | KeyStoreException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
                System.out.println("false");
                System.out.println("Error validating certificate.");
                return false;
            }
        }
        else{
            System.out.println("false");
            System.out.println("User not found.");
            return false;
        } 
    }
}
