//Grevenitis Ioannis icsd13045
//Papaloukas Thomas icsd14155

package privchatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import net.i2p.I2PException;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocket;

public class I2PServerThread implements Runnable{
    I2PServerSocket socket;
    
    public I2PServerThread(I2PServerSocket socket){
        this.socket = socket;
    }
    
    public void run(){
        while(true){
            try{
                I2PSocket sock = this.socket.accept();
                if(sock != null){
                    DataInputStream in = new DataInputStream(sock.getInputStream());  //Client input stream
                    DataOutputStream out = new DataOutputStream(sock.getOutputStream());  //Client output stream                    
                }
            } 
            catch (I2PException ex) {
                System.out.println("General I2P exception!");
            } 
            catch (ConnectException ex) {
                System.out.println("Error connecting!");
            } 
            catch (SocketTimeoutException ex) {
                System.out.println("Timeout!");
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    }
}
