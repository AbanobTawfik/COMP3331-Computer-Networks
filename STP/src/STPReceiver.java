import java.net.*;
import java.io.*;
public class STPReceiver {

    private InetAddress IP;
    private int portNumber;
    private DatagramSocket socket;
    private String fileRequested;
    private PacketBuffer buffer;
    private DatagramPacket dataIn;
    private DatagramPacket dataOut;

    public STPReceiver(String args[]){
        this.portNumber = Integer.parseInt(args[0]);
        this.fileRequested = args[1];
        try {
            this.IP = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try{
            this.socket = new DatagramSocket(this.portNumber, this.IP);
        }catch(Exception e){
            e.printStackTrace();
        }
        this.buffer = new PacketBuffer(50);
    }

    public void operate(){

    }

    public void handshake(){

    }

    public void terminate(){

    }
}
