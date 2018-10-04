import java.net.InetAddress;
import java.net.UnknownHostException;

public class Receiver {
    public static void main(String args[]){
        if(args.length != 2){
            System.out.println("usage: <port number> <file_r.pdf>");
        }
        Receiver receiver = new Receiver();
        receiver.operate(args);
    }

    public void operate(String args[]){
        STPReceiver receiver = new STPReceiver(args);
        receiver.operate();
    }
}
