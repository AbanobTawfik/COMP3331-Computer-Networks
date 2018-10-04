import javafx.util.Pair;

import java.io.BufferedReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
public class STPPacketHeader {
    private byte SYN;
    private byte ACK;
    private byte FIN;
    private byte URG;
    private byte[] checksum;
    private byte[] sequenceNumber;
    private byte[] acknowledgemntNumber;
    private byte[] sourceIP;
    private byte[] destIP;
    private byte[] sourcePort;
    private byte[] destPort;
//    private BytePairs bp;

    public STPPacketHeader(Integer checksum, Integer sequenceNumber, Integer acknowledgemntNumber, InetAddress sourceIP,
                           InetAddress destIP, Integer sourcePort, Integer destPort,Boolean SYN, Boolean ACK, Boolean FIN, Boolean URG) {
        this.checksum = assignIntegerToByte(this.getChecksum(), checksum);
        this.sequenceNumber = assignIntegerToByte(this.getSequenceNumber(),sequenceNumber);
        this.acknowledgemntNumber = assignIntegerToByte(this.getAcknowledgemntNumber(), acknowledgemntNumber);
        this.sourceIP = sourceIP.getAddress();
        this.destIP = destIP.getAddress();
        this.sourcePort = assignIntegerToByte(this.sourcePort, sourcePort);
        this.destPort = assignIntegerToByte(this.destPort, destPort);

        if(SYN == true)
            this.SYN = HeaderValues.TRUE_HEADER;
        else
            this.SYN = HeaderValues.FALSE_HEADER;
        if(ACK == true)
            this.ACK = HeaderValues.TRUE_HEADER;
        else
            this.ACK = HeaderValues.FALSE_HEADER;
        if(FIN == true)
            this.FIN = HeaderValues.TRUE_HEADER;
        else
            this.FIN = HeaderValues.FALSE_HEADER;
        if(URG == true)
            this.URG = HeaderValues.TRUE_HEADER;
        else
            this.URG = HeaderValues.FALSE_HEADER;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public byte[] getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getAcknowledgemntNumber() {
        return acknowledgemntNumber;
    }

    public byte[] getSourceIP() {
        return sourceIP;
    }

    public byte[] getDestIP() {
        return destIP;
    }

    public byte[] getSourcePort() {
        return sourcePort;
    }

    public byte[] getDestPort() {
        return destPort;
    }

    public byte isSYN() {
        return SYN;
    }

    public byte isACK() {
        return ACK;
    }

    public byte isFIN() {
        return FIN;
    }

    public byte isURG() {
        return URG;
    }


    public byte[] assignIntegerToByte(byte[] source, Integer integer){
        HeaderValues.b.clear();
        byte[] original = HeaderValues.b.putInt(integer).array();
        source = new byte[original.length];
        for(int i = 0; i <original.length; i++){
            source[i] = new Byte(original[i]);
        }
        return source;
    }
}

