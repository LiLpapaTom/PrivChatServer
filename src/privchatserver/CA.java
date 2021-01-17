
package privchatserver;

import java.util.Date;
import java.math.BigInteger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;

public class CA {
    public X509Certificate BuildCA(KeyPair keypair){
        Security.addProvider(new BouncyCastleProvider());                   //Add provider
        X509v1CertificateBuilder cert = new JcaX509v1CertificateBuilder(    //Build x509 Self signed CA certificate
            new X500Name("CN = CA Certificate"),                                // Certificate Issuer
            BigInteger.ONE,                                                     // Serial Number
            new Date(System.currentTimeMillis()),                               // Starting validity date
            new Date(System.currentTimeMillis()+TimeUnit.DAYS.toMillis(365)),   //Expiration Date (now + 360days)
            new X500Name("CN = CA Certificate"),                                // Certificate Subject
            keypair.getPublic()                                                 // Public Key
        );
        try{
            // Build content signer, provider BC, private key
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(keypair.getPrivate());
            // Build CA cert by converting the certificate using the signer
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(cert.build(signer));
        }catch (OperatorCreationException | CertificateException ex) {
            System.out.println("Error building CA cert.");
            return null;
        }
    }
    
    public void ExportCert(X509Certificate CA,Keystore ks, Truststore ts,KeyPair keyPair){
        try(FileOutputStream fos=new FileOutputStream("CACert.der")){
            byte[] encodedCert=CA.getEncoded();
            fos.write(encodedCert,0,encodedCert.length);
            ks.AddEntry(CA, keyPair);
            ts.AddEntry(CA, keyPair);
        }catch(CertificateEncodingException ex){
            System.out.println("Couldn't encode certificate.");
        }catch (IOException ex){
            System.out.println("Error writing certificate to file.");
        }
    }
    
    public KeyPair GenKeyPair(){
        Security.addProvider(new BouncyCastleProvider());
        
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA","BC");
            SecureRandom srand=SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(2048,srand);
            return keyGen.generateKeyPair();
        }catch (NoSuchAlgorithmException | NoSuchProviderException ex){
            System.out.println("Error generating key pair.");
            return null;
        }
    }
    
    
    public void buildCert(String username, KeyPair keypair, Keystore ks){
        Security.addProvider(new BouncyCastleProvider());
        new File("Certificates/"+username).mkdirs();
        System.out.println("Folder created for the user");
        
        X509v1CertificateBuilder userCert = new JcaX509v1CertificateBuilder(    // Build x509 Self signed CA certificate
            new X500Name("CN = CA Certificate"),                                // Certificate Issuer
            rndSerial(),                                                        // Serial Number
            new Date(System.currentTimeMillis()),                               // Starting validity date
            new Date(System.currentTimeMillis()+TimeUnit.DAYS.toMillis(365)),   // Expiration Date (now + 360days)
            new X500Name("CN = "+username),                // Certificate Subject
            keypair.getPublic()                                                 // Public Key
        );
        try(FileOutputStream fos=new FileOutputStream("Certificates/"+username+"/"+username+".der")){
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build((PrivateKey) ks.getKey("PrivateCA"));
            X509Certificate Cert=new JcaX509CertificateConverter().setProvider("BC").getCertificate(userCert.build(signer));
            System.out.println("Certificate created");
            
            byte[] encodedCert=Cert.getEncoded();
            fos.write(encodedCert,0,encodedCert.length);
            System.out.println("Certificate exported");
        }catch (OperatorCreationException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException ex){
            System.out.println("Error creating certificate.");
        }catch (IOException ex){
            System.out.println("Error writing certificate to file.");
        }
    }
    
    public void RegisterEntries(String username, KeyPair keypair, Keystore ks){
        if(!new File("Certificates/"+username).exists()){
            System.out.println(username+"'s keys:");    //TODO
            System.out.println(keypair.getPrivate());   //TODO
            System.out.println(keypair.getPublic());    //TODO
            
            buildCert(username, keypair, ks);
            
            try(FileOutputStream fospr = new FileOutputStream("Certificates/"+username+"/Private Key.pem"); FileOutputStream fospb = new FileOutputStream("Certificates/"+username+"/Public Key.asc")){
                fospr.write((new PKCS8EncodedKeySpec(keypair.getPrivate().getEncoded())).getEncoded()); //Export Private Key
                fospb.write((new X509EncodedKeySpec(keypair.getPublic().getEncoded())).getEncoded());  //Export Public Key
            }catch (IOException ex){
                System.out.println("Error writing key to file.");
            }
        }
        else{
            System.err.println("Certificates already exist!");
        }
    }
    
    private BigInteger rndSerial(){     //generate random serial number
        Random rnd = new Random();
        return BigInteger.valueOf(rnd.nextInt(10000)+1000);
    }
}
