import javafx.util.Pair;

import java.net.*;
import java.util.*;
public class STPPacketHeader {
    private byte checksum;
    private byte sequenceNumber;
    private byte acknowledgemntNumber;
    private byte[] sourceIP;
    private byte[] destIP;
    private byte sourcePort;
    private byte destPort;
    private byte[] SYN;
    private byte[] ACK;
    private byte[] FIN;
    private byte[] URG;
    private BytePairs bp;

    public STPPacketHeader(Integer checksum, Integer sequenceNumber, Integer acknowledgemntNumber, InetAddress sourceIP,
                           InetAddress destIP, Integer sourcePort, Integer destPort,Boolean SYN, Boolean ACK, Boolean FIN, Boolean URG) {
        this.checksum = checksum.byteValue();
        this.sequenceNumber = sequenceNumber.byteValue();
        this.acknowledgemntNumber = acknowledgemntNumber.byteValue();
        this.sourceIP = sourceIP.getAddress();
        this.destIP = destIP.getAddress();
        this.sourcePort = sourcePort.byteValue();
        this.destPort = destPort.byteValue();
        this.SYN = SYN.toString().getBytes();
        this.ACK = ACK.toString().getBytes();
        this.FIN = FIN.toString().getBytes();
        this.URG = URG.toString().getBytes();

        List<Byte> sourceIPList = new ArrayList<>();
        for(int i = 0; i < this.sourceIP.length;i++)
            sourceIPList.add(this.sourceIP[0]);

        List<Byte> destIPList = new ArrayList<>();
        for(int i = 0; i < this.destIP.length;i++)
            destIPList.add(this.destIP[0]);

        List<Byte> SYNList = new ArrayList<>();
        for(int i = 0; i < this.SYN.length;i++)
            SYNList.add(this.SYN[0]);

        List<Byte> ACKList = new ArrayList<>();
        for(int i = 0; i < this.ACK.length;i++)
            ACKList.add(this.ACK[0]);

        List<Byte> FINList = new ArrayList<>();
        for(int i = 0; i < this.FIN.length;i++)
            FINList.add(this.FIN[0]);

        List<Byte> URGList = new ArrayList<>();
        for(int i = 0; i < this.URG.length;i++)
            URGList.add(this.URG[0]);

        Pair<Byte,Integer> checksum1 = new Pair<Byte,Integer>(this.checksum,checksum);
        Pair<Byte,Integer> sequenceNumber1 = new Pair<Byte,Integer>(this.sequenceNumber,sequenceNumber);
        Pair<Byte,Integer> acknowledgemntNumber1 = new Pair<Byte,Integer>(this.acknowledgemntNumber, acknowledgemntNumber);
        Pair<List<Byte>,InetAddress> sourceIP1 = new Pair<List<Byte>,InetAddress>(sourceIPList, sourceIP);
        Pair<List<Byte>,InetAddress> destIP1 = new Pair<List<Byte>,InetAddress>(destIPList, destIP);
        Pair<Byte,Integer> sourcePort1 = new Pair<Byte,Integer>(this.sourcePort,sourcePort);
        Pair<Byte,Integer> destPort1 = new Pair<Byte,Integer>(this.destPort,destPort);
        Pair<List<Byte>,Boolean> SYN1 = new Pair<List<Byte>,Boolean>(SYNList, SYN);
        Pair<List<Byte>,Boolean> ACK1 = new Pair<List<Byte>,Boolean>(ACKList, ACK);
        Pair<List<Byte>,Boolean> FIN1 = new Pair<List<Byte>,Boolean>(FINList, FIN);
        Pair<List<Byte>,Boolean> URG1 = new Pair<List<Byte>,Boolean>(URGList, URG);

        bp = new BytePairs(checksum1,sequenceNumber1,acknowledgemntNumber1,sourceIP1,destIP1,sourcePort1,destPort1,SYN1,ACK1,FIN1,URG1);
    }

    public byte getChecksum() {
        return checksum;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    public byte getAcknowledgemntNumber() {
        return acknowledgemntNumber;
    }

    public byte[] getSourceIP() {
        return sourceIP;
    }

    public byte[] getDestIP() {
        return destIP;
    }

    public byte getSourcePort() {
        return sourcePort;
    }

    public byte getDestPort() {
        return destPort;
    }

    public byte[] isSYN() {
        return SYN;
    }

    public byte[] isACK() {
        return ACK;
    }

    public byte[] isFIN() {
        return FIN;
    }

    public byte[] isURG() {
        return URG;
    }

    public BytePairs getBp() {
        return bp;
    }
}
