
import javafx.util.Pair;

import java.net.InetAddress;
import java.util.List;

public class BytePairs {
    private Pair<List<Byte>,Integer> checksum;
    private Pair<List<Byte>,Integer> sequenceNumber;
    private Pair<List<Byte>,Integer> acknowledgemntNumber;
    private Pair<List<Byte>,InetAddress> sourceIP;
    private Pair<List<Byte>,InetAddress> destIP;
    private Pair<List<Byte>,Integer> sourcePort;
    private Pair<List<Byte>,Integer> destPort;
    private Pair<Byte,Boolean> SYN;
    private Pair<Byte,Boolean> ACK;
    private Pair<Byte,Boolean> FIN;
    private Pair<Byte,Boolean> URG;

    public BytePairs(Pair<List<Byte>, Integer> checksum, Pair<List<Byte>, Integer> sequenceNumber, Pair<List<Byte>, Integer> acknowledgemntNumber, Pair<List<Byte>, InetAddress> sourceIP, Pair<List<Byte>, InetAddress> destIP, Pair<List<Byte>, Integer> sourcePort, Pair<List<Byte>, Integer> destPort, Pair<Byte, Boolean> SYN, Pair<Byte, Boolean> ACK, Pair<Byte, Boolean> FIN, Pair<Byte, Boolean> URG) {
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
}
