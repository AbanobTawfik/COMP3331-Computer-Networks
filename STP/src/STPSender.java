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
    private DatagramPacket dataIn = new DatagramPacket(new byte[10024], 10024);
    private DatagramPacket dataOut = new DatagramPacket(new byte[10024], 10024);
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
    private float gamma;
    private ArrayList<ReadablePacket> filePackets = new ArrayList<ReadablePacket>();
    private ArrayBlockingQueue<ReadablePacket> window;
    private STPTimer timer = new STPTimer();
    private FileInputStream file;
    private int windowIndex = 0;
    private int windowSize;
    private FileWriter logFile;

    public STPSender(String args[]) {
        try {
            this.IP = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.portNumber = 2000 + new Random().nextInt(60000);
        try {
            this.socket = new DatagramSocket(this.portNumber, this.IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (args[0].equals("localhost") || args[0].equals("127.0.0.1")) {
                this.receiverIP = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
            } else {
                this.receiverIP = InetAddress.getByName(args[0]);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.receiverPort = Integer.parseInt(args[1]);

        dataOut.setAddress(this.receiverIP);
        dataOut.setPort(this.receiverPort);
        this.fileRequested = args[2];
        this.MWS = Integer.parseInt(args[3]);
        this.MSS = Integer.parseInt(args[4]);
        this.gamma = Float.parseFloat(args[5]);
        this.windowSize = Math.floorDiv(MWS, MSS);
        window = new ArrayBlockingQueue<>(windowSize);
        float pDrop = Float.parseFloat(args[6]);
        float pDuplicate = Float.parseFloat(args[7]);
        float pCorrupt = Float.parseFloat(args[8]);
        float pOrder = Float.parseFloat(args[9]);
        int maxOrder = Integer.parseInt(args[10]);
        float pDelay = Float.parseFloat(args[11]);
        float maxDelay = Float.parseFloat(args[12]);
        float seed = Float.parseFloat(args[13]);
        this.PLD = new Unreliability(pDrop, pDuplicate, pCorrupt, pOrder, maxOrder, pDelay, maxDelay, seed);
        try {
            this.logFile = new FileWriter("Sender Log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void operate() {
        //initiate the 3 way handshake
        timer.start();
        prepareFile();
        handshake();
        sendData();
        terminate();
        System.exit(0);
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
                if (read <= 0) {
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
            receivePacket();
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
        r.display();
    }

    private void sendData() {
        while (true) {
            if (filePackets.size() == windowIndex) {
                break;
            }
            //if there is room inside our window we will transmit a window size from current index (based off last ACK)
            if (window.remainingCapacity() > 0) {
                packet = new STPPacket(filePackets.get(windowIndex));
                window.add(filePackets.get(windowIndex));
                windowIndex++;
                sendPacket(packet);
            } else {
                receivePacket();
                r = new ReadablePacket(dataIn);
                if (r.isACK()) {
                    for (ReadablePacket read : window) {
                        if(r.getAcknowledgemntNumber() == read.getSequenceNumber()){
                            window.remove(read);
                            //filePackets.remove(read);
                        }
                    }
                    continue;
                }
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
        while (true) {
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
        while (true) {
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

    private void transmit() {
        for (int i = windowIndex; i < windowSize + windowIndex; i++) {
            packet = new STPPacket(filePackets.get(i));
            window.add(filePackets.get(i));
            sendPacket(packet);
        }
    }

    private void sendPacket(STPPacket p) {
        dataOut = p.getPacket();
        dataOut.setAddress(receiverIP);
        dataOut.setPort(receiverPort);
        try {
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receivePacket() {
        try {
            dataIn.setAddress(receiverIP);
            dataIn.setPort(receiverPort);
            socket.receive(dataIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logWrite(DatagramPacket p, int sequenceNumber, int ackNumber, String sndOrReceive, String status){
        System.out.println(timer.timePassed());
        float timePassed = timer.timePassed()/1000;
        String s = String.format(sndOrReceive + "\t\t\t\t" + "%2f" + "\t\t" + status + "\t\t\t"
                + sequenceNumber + "\t\t" + (p.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER) + "\t\t"
                + ackNumber + "\n",timePassed);
        System.out.println(s);
        try {
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
