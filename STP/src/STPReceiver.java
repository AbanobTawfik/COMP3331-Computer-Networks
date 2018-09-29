import java.net.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class STPReceiver {
    private STPTimer timer;
    private InetAddress IP;
    private InetAddress senderIP;
    private int senderPort;
    private int portNumber;
    private DatagramSocket socket;
    private String fileRequested;
    private PacketBuffer buffer;
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
    private ArrayList<ReadablePacket> payloads = new ArrayList<ReadablePacket>();
    private OutputStream pdfFile;
    private int payloadSize;
    private FileWriter logFile;
    private boolean firstDataSizeFlag = false;

    public STPReceiver(String args[]) {
        this.portNumber = Integer.parseInt(args[0]);
        this.fileRequested = args[1];
        File dir = new File("created_files");
        dir.mkdir();
        this.fileRequested = dir.getAbsolutePath() + "/"+ this.fileRequested;
        try {
            this.IP = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            this.socket = new DatagramSocket(this.portNumber, this.IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.buffer = new PacketBuffer(50);
        try {
            this.pdfFile = new FileOutputStream(this.fileRequested);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            this.logFile = new FileWriter("Receiver Log.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void operate() {
        //initiate the 3 way handshake
        //timer.run();
        handshake();
        receiveData();
        terminate();
    }

    private void handshake() {
        while (!SYN) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isSYN()) {
                SYN = true;
                ACK = true;
            }
        }
        this.senderIP = r.getSourceIP();
        this.senderPort = r.getSourcePort();
        dataOut.setAddress(r.getSourceIP());
        dataOut.setPort(r.getSourcePort());
        //add 1 for SYN bit
        ackNumber = r.getSequenceNumber() + 1;
        header = new STPPacketHeader(0, r.getSequenceNumber(), ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, URG);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        //now we wait for the reply that our reply has been acknowledged
        while (true) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isACK() && r.isSYN())
                break;
        }
        r.display();
        System.out.println("handshake complete");
    }

    private void receiveData() {
        while (!this.FIN) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (!firstDataSizeFlag) {
                payloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                firstDataSizeFlag = true;
            }
            //extract payload
            //drop packet if corrupted data
            if (!validCheckSum(r))
                ACK = false;
            else
                ACK = true;
            ackNumber = r.getSequenceNumber(); //+ payloadLength();
            sequenceNumber++;
            buffer.addConditionally(payloads);
            if (r.getSequenceNumber() > ackNumber)
                buffer.add(new ReadablePacket(dataIn));
            else {
                if (!payloads.contains(r))
                    payloads.add(new ReadablePacket(dataIn));
            }
            if (r.isFIN())
                return;
            header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                    r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, URG);
            packet = new STPPacket(this.header, new byte[0]);
            sendPacket(packet);
        }
    }

    private void terminate() {
        //4 way handshake closure, since the server will initiate the close
        //first send back the FINACK for the servers FIN
        URG = false;
        FIN = true;
        ACK = true;
        //last packet to send back fin will be R since the while loop terminates
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, URG);
        packet = new STPPacket(this.header, new byte[0]);
        sendPacket(packet);
        //now we want to send back our FIN to initiate our side of the closure
        ACK = false;
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, URG);
        packet = new STPPacket(this.header, new byte[0]);
        sendPacket(packet);
        //now we want to wait for our ack from the server
        while (true) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isACK() && r.isFIN())
                break;
        }
        socket.close();
        writeFile();
    }

    private int checksum(byte[] payload) {
        int sum = 0;
        for (byte byteData : payload) {
            sum += (int) byteData;
        }
        sum = ~sum;
        return sum;
    }

    private boolean validCheckSum(ReadablePacket r) {
        return r.getChecksum() == checksum(r.getPayload());
    }

    private void sendPacket(STPPacket p) {
        dataOut = p.getPacket();
        dataOut.setAddress(senderIP);
        dataOut.setPort(senderPort);
        try {
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile() {
        payloads.remove(payloads.get(payloads.size() - 1));
        try {
            for (ReadablePacket r : payloads) {
                pdfFile.write(unpaddedPayload(r));
                pdfFile.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            pdfFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] unpaddedPayload(ReadablePacket r) {
        return Arrays.copyOf(r.getPayload(), payloadSize);
    }
}