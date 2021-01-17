//Grevenitis Ioannis icsd13045
//Papaloukas Thomas icsd14155

package privchatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Scanner;
import static privchatserver.PrivChatServer.bannedIP;
import static privchatserver.PrivChatServer.bannedTime;
import static privchatserver.PrivChatServer.changeAlias;
import static privchatserver.PrivChatServer.clients;
import static privchatserver.PrivChatServer.curse;
import static privchatserver.PrivChatServer.par;

public class ServerThread extends Thread{
    private Socket sock;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private int banCount;
    
    ServerThread(Socket sock, DataInputStream in, DataOutputStream out, String username){
        this.sock = sock;
        this.in = in;
        this.out = out;
        this.username = username;
        this.banCount = 3;
    }
    
    public String getUsername(){
        return username;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    @Override
    public void run(){
        String received;
        sendMessage(username);
        
        while(true){
            try{
                received = receiveMessage();
                if(received.trim().equals("changeUsernameRequest")){
                    sendMessage("newUsername");
                    changeAlias(getUsername());
                }
                else{
                    updatePUCChat(received);
                    System.out.println(received);
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }
    
    public void sendMessage(String msg){
        try{
            out.writeUTF(msg);
            out.flush();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public String receiveMessage() throws IOException{
        String msg = filterCurse(in.readUTF());
        return msg;
    }
    
    public void updatePUCChat(String msg){
        for(int i=0; i<clients.size(); i++){
            clients.get(i).sendMessage(msg);
        }
    }
    
    public String filterCurse(String msg){
        boolean flag = false;
        String tmp = "";
        StringBuilder bob = new StringBuilder();
        String[] tmp2 = msg.split("\\s+");
        
        for(int i=0; i<tmp2.length; i++){
            if(curse.contains(tmp2[i].toLowerCase())){
                tmp2[i] = "****";
                flag = true;
            }
        }
        
        for(int i=0; i<tmp2.length; i++){
            bob.append(tmp2[i]).append(" ");
        }
        tmp = bob.toString();
        
        if(flag == true){
            banCount -=1;
        }
        if(banCount <= 0){
            chatBan();
        }
        
        return tmp;
    }
    
    public void chatBan(){
        bannedIP.add(sock.getInetAddress().getHostAddress());
        
        if(par[3] == 1){
            bannedTime.add(new GregorianCalendar(2999,12,31));            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
            //System.out.println( sdf.format(bannedTime.get(0).getTime()) );            
            sendMessage("You just got Banned until "+sdf.format( new GregorianCalendar(2999,12,31).getTime() ) +", LMAO");
            while(true){} //He wont be able to write anymore in chat
        }
        else if(par[3] == 0){
            bannedTime.add(new GregorianCalendar(par[0],par[1],par[2]));            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
            //System.out.println( sdf.format(bannedTime.get(0).getTime()) );               
            sendMessage("You just got Banned until "+sdf.format( new GregorianCalendar(par[0],par[1],par[2]).getTime() ) +", LMAO");
            while(true){}
        }         
    }
    
    @Override
    public String toString(){
        return username + " " + banCount;
    }
    
}
