import java.net.*;

public class STPReceiver {
    private InetAddress IP;
    private int portNumber;
    private DatagramSocket socket;
    private String fileRequested;

    public STPReceiver(String args[]){
        this.portNumber = Integer.parseInt(args[0]);
        this.fileRequested = args[1];
        try{
            this.socket = new DatagramSocket();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void operate(){

    }

    public void handshake(){

    }
}
