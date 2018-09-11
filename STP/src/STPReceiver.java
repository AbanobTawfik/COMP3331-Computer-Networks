import sun.nio.cs.UTF_32;

import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

public class STPReceiver {

    private InetAddress IP;
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
    private ReadablePacket r;
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    private boolean URG = false;
    private ArrayList<ReadablePacket> payloads = new ArrayList<ReadablePacket>();
    public STPReceiver(String args[]) {
        this.portNumber = Integer.parseInt(args[0]);
        this.fileRequested = args[1];
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
    }

    public void operate() {
        //initiate the 3 way handshake
        handshake();
        receiveData();
        terminate();
    }

    public void handshake() {
        while (!this.SYN) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isSYN()) {
                this.SYN = true;
                this.ACK = true;
            }
        }
        dataOut.setAddress(r.getSourceIP());
        dataOut.setPort(r.getSourcePort());
        //add 1 for SYN bit
        this.ackNumber = r.getSequenceNumber() + 1;
        this.header = new STPPacketHeader(0, this.sequenceNumber, this.ackNumber, this.IP,
                r.getSourceIP(), this.portNumber, r.getSourcePort(), true, true, false, false);
        STPPacket handShakeReply = new STPPacket(this.header, new byte[1]);
        dataOut = handShakeReply.getPacket();
        try {
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    public void receiveData() {
        while (!this.FIN) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.r = new ReadablePacket(dataIn);
            //extract payload
            this.ackNumber = this.r.getSequenceNumber() + this.r.getPayload().length;
            this.sequenceNumber++;
            buffer.addConditionally(this.payloads);
            if(this.r.getSequenceNumber() > this.ackNumber)
                buffer.add(new ReadablePacket(dataIn));
            else
                payloads.add(new ReadablePacket(dataIn));
            if (r.isFIN())
                return;
            this.header = new STPPacketHeader(0, this.sequenceNumber, this.ackNumber, this.IP,
                    r.getSourceIP(), this.portNumber, r.getSourcePort(), true, true, false, false);

        }
    }

    public void terminate() {

    }


}
//more debug code here
//packet.getBp().print();

//set the destination reply to be the source where we got our initial packet from


//                STPPacket packet = new STPPacket(header, payload);
//                dataIn = packet.getPacket();
//                System.out.println(dataIn.getData());
//                ReadablePacket r = new ReadablePacket(dataIn);
//                r.display();
//        while (true) {

//        }