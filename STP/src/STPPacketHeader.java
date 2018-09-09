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
    private BytePairs bp;

    public STPPacketHeader(Integer checksum, Integer sequenceNumber, Integer acknowledgemntNumber, InetAddress sourceIP,
                           InetAddress destIP, Integer sourcePort, Integer destPort,Boolean SYN, Boolean ACK, Boolean FIN, Boolean URG) {
        this.checksum = HeaderValues.b.putInt(checksum).array();
        HeaderValues.b.clear();
        //System.out.println(HeaderValues.b.put(this.checksum).getInt());
        this.sequenceNumber = new byte[8];
        this.acknowledgemntNumber = new byte[8];
        this.sourceIP = sourceIP.getAddress();
        this.destIP = destIP.getAddress();
        this.sourcePort = new byte[8];
        this.destPort = new byte[8];

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

        List<Byte> sourceIPList = new ArrayList<>();
        for(int i = 0; i < this.sourceIP.length;i++)
            sourceIPList.add(this.sourceIP[0]);

        List<Byte> destIPList = new ArrayList<>();
        for(int i = 0; i < this.destIP.length;i++)
            destIPList.add(this.destIP[0]);

        List<Byte> checksumList = new ArrayList<>();
        for(int i = 0; i < this.checksum.length;i++)
            checksumList.add(this.checksum[0]);

        List<Byte> sequenceList = new ArrayList<>();
        for(int i = 0; i < this.sequenceNumber.length;i++)
            sequenceList.add(this.sequenceNumber[0]);

        List<Byte> ackList = new ArrayList<>();
        for(int i = 0; i < this.acknowledgemntNumber.length;i++)
            ackList.add(this.acknowledgemntNumber[0]);

        List<Byte> srcportList = new ArrayList<>();
        for(int i = 0; i < this.sourcePort.length;i++)
            srcportList.add(this.sourcePort[0]);

        List<Byte> dstportList = new ArrayList<>();
        for(int i = 0; i < this.destPort.length;i++)
            dstportList.add(this.destPort[0]);

        Pair<List<Byte>,Integer> checksum1 = new Pair<List<Byte>,Integer>(checksumList,checksum);
        Pair<List<Byte>,Integer> sequenceNumber1 = new Pair<List<Byte>,Integer>(sequenceList,sequenceNumber);
        Pair<List<Byte>,Integer> acknowledgemntNumber1 = new Pair<List<Byte>,Integer>(ackList, acknowledgemntNumber);
        Pair<List<Byte>,InetAddress> sourceIP1 = new Pair<List<Byte>,InetAddress>(sourceIPList, sourceIP);
        Pair<List<Byte>,InetAddress> destIP1 = new Pair<List<Byte>,InetAddress>(destIPList, destIP);
        Pair<List<Byte>,Integer> sourcePort1 = new Pair<List<Byte>,Integer>(srcportList,sourcePort);
        Pair<List<Byte>,Integer> destPort1 = new Pair<List<Byte>,Integer>(dstportList,destPort);
        Pair<Byte,Boolean> SYN1 = new Pair<Byte,Boolean>(this.SYN, SYN);
        Pair<Byte,Boolean> ACK1 = new Pair<Byte,Boolean>(this.ACK, ACK);
        Pair<Byte,Boolean> FIN1 = new Pair<Byte,Boolean>(this.FIN, FIN);
        Pair<Byte,Boolean> URG1 = new Pair<Byte,Boolean>(this.URG, URG);

        bp = new BytePairs(checksum1,sequenceNumber1,acknowledgemntNumber1,sourceIP1,destIP1,sourcePort1,destPort1,SYN1,ACK1,FIN1,URG1);
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

    public BytePairs getBp() {
        return bp;
    }
}
