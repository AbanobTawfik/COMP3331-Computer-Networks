import java.net.*;
import java.io.*;
import java.util.*;

public class STPReceiver {
    private STPTimer timer = new STPTimer();
    private InetAddress IP;
    private InetAddress senderIP;
    private int senderPort;
    private int portNumber;
    private DatagramSocket socket;
    private String fileRequested;
    private PacketBuffer buffer;
    private DatagramPacket dataIn = new DatagramPacket(new byte[1000024], 1000024);
    private DatagramPacket dataOut = new DatagramPacket(new byte[1000024], 1000024);
    //set the initial sequence number to 2^31 - 1000000000 for lee-way, this will also have enough randomness
    private int sequenceNumber = 0;
    private int ackNumber;
    private STPPacketHeader header;
    private STPPacket packet;
    private ReadablePacket r;
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    private boolean DUP = false;
    private PacketSet payloads = new PacketSet();
    private OutputStream pdfFile;
    private int payloadSize;
    private FileWriter logFile;
    private boolean firstDataSizeFlag = false;
    private int lastPayloadSize;
    private ReceiverLogs PLD = new ReceiverLogs();

    public STPReceiver(String args[]) {
        this.portNumber = Integer.parseInt(args[0]);
        this.fileRequested = args[1];
        File dir = new File("created_files");
        dir.mkdir();
        this.fileRequested = dir.getAbsolutePath() + "/" + this.fileRequested;
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
            dir = new File("log_files_" + args[1]);
            if(!dir.exists());
                dir.mkdir();
            this.logFile = new FileWriter(dir.getAbsoluteFile() + "/" + "Receiver Log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s\n", "snd/rcv", "time",
                    "type", "sequence", "payload size", "ack");
            logFile.write(s);
            logFile.flush();
            s = "--------------------------------------------------------------------------\n";
            logFile.write(s);
            logFile.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void operate() {
        timer.run();
        handshake();
        receiveData();
        terminate();
        finishLogFile();
        System.exit(0);
    }

    private void handshake() {
        while (!SYN) {
            try {
                socket.receive(dataIn);
                PLD.addSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isSYN()) {
                logWrite(0, 0, 0, "rcv", "S");
                SYN = true;
                ACK = true;
            }
        }
        this.senderIP = r.getSourceIP();
        this.senderPort = r.getSourcePort();
        dataOut.setAddress(r.getSourceIP());
        dataOut.setPort(r.getSourcePort());
        //add 1 for SYN bit
        ackNumber = 1;
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        logWrite(0, sequenceNumber, ackNumber, "snd", "SA");
        sendPacket(packet);
        //now we wait for the reply that our reply has been acknowledged
        while (true) {
            try {
                socket.receive(dataIn);
                PLD.addSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isACK() && r.isSYN())
                break;
        }
        sequenceNumber++;
        logWrite(0, sequenceNumber, ackNumber, "rcv", "A");
        r.display();
        System.out.println("handshake complete");
    }

    private void receiveData() {
        while (!this.FIN) {
            try {
                socket.receive(dataIn);
                PLD.addBytesReceived(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER);
                PLD.addSegmentsReceived();
                PLD.addDataSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (!firstDataSizeFlag) {
                payloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                lastPayloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                firstDataSizeFlag = true;
            }
            if (dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER > 0 && (dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER != payloadSize)) {
                lastPayloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                if (!validCheckSum(r, lastPayloadSize)) {
                    ACK = false;
                } else {
                    ACK = true;
                }
            } else {
                if (!validCheckSum(r, payloadSize) && r.getChecksum() != 0) {
                    PLD.addCorruptDataSegments();
                    ACK = false;
                } else {
                    ACK = true;
                }
            }
            //extract payload
            //drop packet if corrupted data


            if (payloads.contains(r)) {
                PLD.addDuplicateSegments();
                header = new STPPacketHeader(0, sequenceNumber, r.getSequenceNumber(), IP,
                        r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
                packet = new STPPacket(this.header, new byte[0]);
                sendPacket(packet);
                logWrite(0, sequenceNumber, ackNumber, "snd/DA", "A");
                continue;
            }
            if (ACK && r.getSequenceNumber() > (payloads.last() + payloadSize))
                buffer.add(new ReadablePacket(dataIn));
            else {
                if (ACK)
                    payloads.add(new ReadablePacket(dataIn));
            }
            if(ackNumber == payloads.last())
                PLD.addDupACKS();
            ackNumber = payloads.last();

            if (!ACK)
                logWrite(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER, r.getSequenceNumber() - (payloadSize - lastPayloadSize), r.getAcknowledgemntNumber(), "rcv/corr", "D");
            else {
                if (dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER == 0) {
                    logWrite(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER,  r.getSequenceNumber() - (payloadSize - lastPayloadSize), r.getAcknowledgemntNumber(), "rcv", "F");
                } else {
                    logWrite(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER, r.getSequenceNumber() - (payloadSize - lastPayloadSize), r.getAcknowledgemntNumber(), "rcv", "D");
                }
            }
            for (ReadablePacket r : buffer.getBuffer()) {
                payloads.add(r);
            }
            if (r.isFIN()) {
                PLD.addSegmentsReceived();
                for (ReadablePacket r : buffer.getBuffer()) {
                    payloads.add(r);
                }
                return;
            }
            System.out.println(payloads.size());
            header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                    r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
            packet = new STPPacket(this.header, new byte[0]);
            sendPacket(packet);
            logWrite(0, sequenceNumber, ackNumber, "snd", "A");
        }
    }

    private void terminate() {
        //4 way handshake closure, since the server will initiate the close
        //first send back the FINACK for the servers FIN
        DUP = false;
        FIN = true;
        ACK = true;
        //last packet to send back fin will be R since the while loop terminates
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
        packet = new STPPacket(this.header, new byte[0]);
        sendPacket(packet);
        logWrite(0, sequenceNumber, r.getSequenceNumber() - (payloadSize - lastPayloadSize) + 1, "snd", "A");
        //now we want to send back our FIN to initiate our side of the closure
        ACK = false;
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
        packet = new STPPacket(this.header, new byte[0]);
        sendPacket(packet);
        logWrite(0, sequenceNumber, r.getSequenceNumber() - (payloadSize - lastPayloadSize) + 1, "snd", "F");
        //now we want to wait for our ack from the server
        while (true) {
            try {
                socket.receive(dataIn);
                PLD.addSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isACK() && r.isFIN())
                logWrite(0, r.getSequenceNumber() - (payloadSize - lastPayloadSize) + 1, ++sequenceNumber, "rcv", "A");
            break;
        }
        socket.close();
        writeFile();
    }

    private int checksum(byte[] payload, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (int) payload[i];
        }
        sum = ~sum;
        return sum;
    }

    private boolean validCheckSum(ReadablePacket r, int length) {
        return r.getChecksum() == checksum(r.getPayload(), length);
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
        System.out.println(payloads.size());
        System.out.println("-------");
        if (payloads.size() > 1)
            payloads.remove(payloads.get(payloads.size() - 1));
        try {
            for (ReadablePacket r : payloads.getArrayList()) {
                pdfFile.write(unpaddedPayload(r, r.equals(payloads.get(payloads.size() - 1))));
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

    public byte[] unpaddedPayload(ReadablePacket r, boolean last) {
        if (last)
            return Arrays.copyOf(r.getPayload(), lastPayloadSize);
        return Arrays.copyOf(r.getPayload(), payloadSize);
    }

    public void logWrite(int length, int sequenceNumber, int ackNumber, String sndOrReceive, String status) {
        float timePassed = timer.timePassed() / 1000;
        String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s\n", sndOrReceive
                , timePassed, status, sequenceNumber, length, ackNumber);
        try {
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void finishLogFile() {
        String s = "--------------------------------------------------------------------------\n";
        try {
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Amount of data received (bytes)", PLD.getBytesReceived());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Total Segments Received", PLD.getSegmentsReceived());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Data segments received", PLD.getDataSegmentsReceived());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Data segments with Bit Errors", PLD.getCorruptDataSegments());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Duplicate data segments received", PLD.getDuplicateSegments());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Duplicate ACKs sent", PLD.getDupACKS());
            logFile.write(s);
            logFile.flush();
            s = "--------------------------------------------------------------------------\n";
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}