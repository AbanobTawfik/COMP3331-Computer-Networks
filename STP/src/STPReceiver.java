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
        STPPacketHeader header = new STPPacketHeader(127000,2,3,
                this.IP,this.IP,1,2,true,true,true,false);
        byte[] payload = new byte[10];
        STPPacket packet = new STPPacket(header,payload);
        packet.getBp().print();
        byte[] test = new byte[4];
        test[0] = 0;
        test[1] = 1;
        test[2] = -16;
        test[3] = 24;
        byte[] test2 = new byte[4];
        test2[0] = 1;
        test2[1] = 2;
        test2[2] = 3;
        test2[3] = 4;
        int int1 = readHeaderValues(header.getChecksum());
        int int2 = readHeaderValues(header.getAcknowledgemntNumber());
        System.out.println("after first run - " + int1 + "after second run - " + int2);

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

    /*
     * Print ping data to the standard output stream.
     */
    public void printData(DatagramPacket request) throws Exception {
        // Obtain references to the STPPacket's array of bytes.
        byte[] buf = request.getData();

        // Wrap the bytes in a byte array input stream,
        // so that you can read the data as a stream of bytes.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);

        // Wrap the byte array output stream in an input stream reader,
        // so you can read the data as a stream of characters.
        InputStreamReader isr = new InputStreamReader(bais);

        // Wrap the input stream reader in a bufferred reader,
        // so you can read the character data a line at a time.
        // (A line is a sequence of chars terminated by any combination of \r and \n.)
        BufferedReader br = new BufferedReader(isr);

        // The message data is contained in a single line, so read this line.
        String line = br.readLine();

        // Print host address and data received from it.
        System.out.println(
                "Received from " +
                        request.getAddress().getHostAddress() +
                        ": " +
                        line);
    }

    public int readHeaderValues(byte[] src){
        HeaderValues.b.clear();
        HeaderValues.b.put(src);
        HeaderValues.b.position(0);
        int val = new Integer(HeaderValues.b.getInt());
        System.out.println(val);
        return val;
    }
}
