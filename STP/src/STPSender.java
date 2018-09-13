import java.io.*;
import java.net.*;
import java.util.*;

public class STPSender {
    private  File folder = new File(System.getProperty("user.dir"));
    private  File[] allfiles = folder.listFiles();
    private InetAddress IP;
    private int portNumber;
    private DatagramSocket socket;
    private String fileRequested;
    private DatagramPacket dataIn = new DatagramPacket(new byte[1024], 1024);
    private DatagramPacket dataOut = new DatagramPacket(new byte[1024], 1024);
    //set the initial sequence number to 2^31 - 1000000000 for lee-way, this will also have enough randomness
    private int sequenceNumber = new Random().nextInt(1147483648);
    private int ackNumber;
    private STPPacketHeader header;
    private STPPacket packet;
    private ReadablePacket r;
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    private boolean URG = false;
    private InetAddress receiverIP;
    private int receiverPort;
    private Unreliability PLD;
    private int MWS;
    private int MSS;
    private long gamma;
    private STPTimer timer = new STPTimer();

    public STPSender(String args[]) {
        try {
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataOut.setAddress(this.receiverIP);
        dataOut.setPort(this.receiverPort);
        this.IP = socket.getInetAddress();
        this.portNumber = socket.getPort();
        try {
            this.receiverIP = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.receiverPort = Integer.parseInt(args[1]);
        this.fileRequested = args[2];
        this.MWS = Integer.parseInt(args[3]);
        this.MSS = Integer.parseInt(args[4]);
        this.gamma = Long.parseLong(args[5]);
        long pDrop = Long.parseLong(args[6]);
        long pDuplicate = Long.parseLong(args[7]);
        long pCorrupt = Long.parseLong(args[8]);
        long pOrder = Long.parseLong(args[9]);
        int maxOrder = Integer.parseInt(args[10]);
        long pDelay = Long.parseLong(args[11]);
        long maxDelay = Long.parseLong(args[12]);
        long seed = Long.parseLong(args[13]);
        this.PLD = new Unreliability(pDrop,pDuplicate,pCorrupt,pOrder,maxOrder,pDelay,maxDelay,seed);
    }

    public void operate() {
        //initiate the 3 way handshake
        prepareFile();
        handshake();
        sendData();
        terminate();
    }

    private void prepareFile(){

    }

    private void handshake(){

    }

    private void sendData(){

    }

    private void terminate(){

    }

    private boolean containsFile(String fileName) {
        //scan through directory
        for (int i = 0; i < allfiles.length; i++)
            //if the file name matches one in the directory we return true
            if (allfiles[i].getName().equals(fileName))
                return true;
        //otherwise return false if no files match
        return false;
    }


}
