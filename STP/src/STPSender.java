import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class STPSender {
    private File folder = new File(System.getProperty("user.dir"));
    private File[] allFiles = folder.listFiles();
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
    private ArrayList<ReadablePacket> filePackets = new ArrayList<ReadablePacket>();
    private ArrayBlockingQueue<ReadablePacket> window;
    private STPTimer timer = new STPTimer();
    private FileInputStream file;
    private int windowIndex = 0;
    private int windowSize;

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
        this.windowSize = Math.floorDiv(MWS, MSS);
        window = new ArrayBlockingQueue<>(windowSize);
        long pDrop = Long.parseLong(args[6]);
        long pDuplicate = Long.parseLong(args[7]);
        long pCorrupt = Long.parseLong(args[8]);
        long pOrder = Long.parseLong(args[9]);
        int maxOrder = Integer.parseInt(args[10]);
        long pDelay = Long.parseLong(args[11]);
        long maxDelay = Long.parseLong(args[12]);
        long seed = Long.parseLong(args[13]);
        this.PLD = new Unreliability(pDrop, pDuplicate, pCorrupt, pOrder, maxOrder, pDelay, maxDelay, seed);
    }

    public void operate() {
        //initiate the 3 way handshake
        prepareFile();
        handshake();
        sendData();
        terminate();
    }

    private void prepareFile() {
        if (!containsFile(fileRequested)) {
            System.out.println("The file requested does not exist in this directory");
            System.exit(1);
        } else {
            //we want to create a list of ready to send packets (since they are file data we want to turn off most flags)
            try {
                file = new FileInputStream(fileRequested);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] packetPayload = new byte[MSS];
            int read = 0;
            while (true) {
                try {
                    read = file.read(packetPayload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (read == 0) {
                    break;
                }
                //now we want to store all these payloads into packet
                header = new STPPacketHeader(checksum(packetPayload), sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, URG);
                sequenceNumber += MSS;
                packet = new STPPacket(header, packetPayload);
                r = new ReadablePacket(packet.getPacket());
                filePackets.add(r);
            }
        }
    }

    private void handshake() {
        SYN = true;
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, URG);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        //now we want for SYN ACK back
        while (true) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isSYN() && r.isACK()) {
                ACK = true;
                break;
            }
        }
        //end handshake with the ACK
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, URG);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
    }

    private void sendData() {
        while (true) {
            if(filePackets.size() == 0){
                break;
            }
            //if there is room inside our window which was calculated from MWS/MSS
            if (window.remainingCapacity() > 0) {

            }
        }
    }

    private void terminate() {
        //send out the FIN
        FIN = true;
        URG = false;
        ACK = false;
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, URG);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        //now wait for the FIN ACK
        while(true){
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isFIN() && r.isACK()) {
                break;
            }
        }
        //now wait for the FIN
        while(true){
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isFIN() && !r.isACK()) {
                break;
            }
        }
        //send back an FIN ACK to the client
        FIN = true;
        ACK = true;
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, URG);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
    }

    private boolean containsFile(String fileName) {
        //scan through directory
        for (File file : allFiles)
            if (file.getName().equals(fileName))
                return true;
        //otherwise return false if no files match
        return false;
    }

    private int checksum(byte[] payload) {
        int sum = 0;
        for (byte byteData : payload) {
            sum += (int) byteData;
        }
        sum = ~sum;
        return sum;
    }

    private void sendPacket(STPPacket p) {
        dataOut = p.getPacket();
        try {
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
