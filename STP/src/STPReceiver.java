import sun.nio.cs.UTF_32;

import java.net.*;
import java.io.*;
import java.nio.charset.Charset;

public class STPReceiver {

    private InetAddress IP;
    private int portNumber;
    private DatagramSocket socket;
    private String fileRequested;
    private PacketBuffer buffer;
    private DatagramPacket dataIn = new DatagramPacket(new byte[1024], 1024);
    private DatagramPacket dataOut = new DatagramPacket(new byte[1024], 1024);

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
    }

    public void handshake() {
        STPPacketHeader header = new STPPacketHeader(2147000000, 3000000, 54443,
                this.IP, this.IP, 8888, 6666, false, true, true, false);
        byte[] payload = new byte[10];
        STPPacket packet = new STPPacket(header, payload);
        dataIn = packet.getPacket();
        System.out.println(dataIn.getData());
        ReadablePacket r = new ReadablePacket(dataIn);
        r.display();
//        while (true) {
//            try {
//                printData(dataIn);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

    }

    public void terminate() {

    }




}
//more debug code here
//packet.getBp().print();
