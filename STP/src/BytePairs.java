
import javafx.util.Pair;

import java.net.InetAddress;
import java.util.List;

public class BytePairs {
    private Pair<Byte,Integer> checksum;
    private Pair<Byte,Integer> sequenceNumber;
    private Pair<Byte,Integer> acknowledgemntNumber;
    private Pair<List<Byte>,InetAddress> sourceIP;
    private Pair<List<Byte>,InetAddress> destIP;
    private Pair<Byte,Integer> sourcePort;
    private Pair<Byte,Integer> destPort;
    private Pair<List<Byte>,Boolean> SYN;
    private Pair<List<Byte>,Boolean> ACK;
    private Pair<List<Byte>,Boolean> FIN;
    private Pair<List<Byte>,Boolean> URG;

    public BytePairs(Pair<Byte, Integer> checksum, Pair<Byte, Integer> sequenceNumber, Pair<Byte, Integer> acknowledgemntNumber, Pair<List<Byte>, InetAddress> sourceIP, Pair<List<Byte>, InetAddress> destIP, Pair<Byte, Integer> sourcePort, Pair<Byte, Integer> destPort, Pair<List<Byte>, Boolean> SYN, Pair<List<Byte>, Boolean> ACK, Pair<List<Byte>, Boolean> FIN, Pair<List<Byte>, Boolean> URG) {
        this.checksum = checksum;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgemntNumber = acknowledgemntNumber;
        this.sourceIP = sourceIP;
        this.destIP = destIP;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
        this.SYN = SYN;
        this.ACK = ACK;
        this.FIN = FIN;
        this.URG = URG;
    }

    public void print(){
        System.out.println("=========================================================");
        System.out.println("1. " + checksum.getValue() + "<=>" + checksum.getKey());
        System.out.println("2. " + sequenceNumber.getValue() + "<=>" + sequenceNumber.getKey());
        System.out.println("3. " + acknowledgemntNumber.getValue() + "<=>" + acknowledgemntNumber.getKey());
        System.out.println("4. " + sourceIP.getValue() + "<=>" + sourceIP.getKey());
        System.out.println("5. " + destIP.getValue() + "<=>" + destIP.getKey());
        System.out.println("6. " + sourcePort.getValue() + "<=>" + sourcePort.getKey());
        System.out.println("7. " + destPort.getValue() + "<=>" + destPort.getKey());
        System.out.println("8. " + SYN.getValue() + "<=>" + SYN.getKey());
        System.out.println("9. " + ACK.getValue() + "<=>" + ACK.getKey());
        System.out.println("10. " + FIN.getValue() + "<=>" + FIN.getKey());
        System.out.println("11. " + URG.getValue() + "<=>" + URG.getKey());
        System.out.println("=========================================================");
    }

    public Pair<Byte, Integer> getChecksum() {
        return checksum;
    }

    public Pair<Byte, Integer> getSequenceNumber() {
        return sequenceNumber;
    }

    public Pair<Byte, Integer> getAcknowledgemntNumber() {
        return acknowledgemntNumber;
    }

    public Pair<List<Byte>, InetAddress> getSourceIP() {
        return sourceIP;
    }

    public Pair<List<Byte>, InetAddress> getDestIP() {
        return destIP;
    }

    public Pair<Byte, Integer> getSourcePort() {
        return sourcePort;
    }

    public Pair<Byte, Integer> getDestPort() {
        return destPort;
    }

    public Pair<List<Byte>, Boolean> getSYN() {
        return SYN;
    }

    public Pair<List<Byte>, Boolean> getACK() {
        return ACK;
    }

    public Pair<List<Byte>, Boolean> getFIN() {
        return FIN;
    }

    public Pair<List<Byte>, Boolean> getURG() {
        return URG;
    }
}
