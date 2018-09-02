import java.net.*;

public class STPPacketHeader {
    private int checksum;
    private int sequenceNumber;
    private int acknowledgemntNumber;
    private InetAddress sourceIP;
    private InetAddress destIP;
    private int sourcePort;
    private int destPort;
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    private boolean URG = false;

    public STPPacketHeader(int checksum, int sequenceNumber, int acknowledgemntNumber, InetAddress sourceIP,
                           InetAddress destIP, int sourcePort, int destPort) {
        this.checksum = checksum;
        this.sequenceNumber = sequenceNumber;
        this.acknowledgemntNumber = acknowledgemntNumber;
        this.sourceIP = sourceIP;
        this.destIP = destIP;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
    }

    public int getChecksum() {
        return checksum;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgemntNumber() {
        return acknowledgemntNumber;
    }

    public InetAddress getSourceIP() {
        return sourceIP;
    }

    public InetAddress getDestIP() {
        return destIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestPort() {
        return destPort;
    }

    public boolean isSYN() {
        return SYN;
    }

    public boolean isACK() {
        return ACK;
    }

    public boolean isFIN() {
        return FIN;
    }

    public boolean isURG() {
        return URG;
    }
}
