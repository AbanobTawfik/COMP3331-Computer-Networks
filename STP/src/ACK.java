public class ACK {
    private int ackNumber;
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    private boolean URG = false;

    public ACK(int ackNumber) {
        this.ackNumber = ackNumber;
    }

    public int getAckNumber() {
        return ackNumber;
    }
}
