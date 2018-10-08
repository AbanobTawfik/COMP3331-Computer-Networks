public class ReceiverLogs {
    private int bytesReceived = 0;
    private int segmentsReceived = 0;
    private int dataSegmentsReceived = 0;
    private int corruptDataSegments = 0;
    private int duplicateSegments = 0;
    private int dupACKS = 0;

    public int getSegmentsReceived() {
        return segmentsReceived;
    }

    public int getBytesReceived() {
        return bytesReceived;
    }

    public void addBytesReceived(int bytesReceived) {
        this.bytesReceived += bytesReceived;
    }

    public void addSegmentsReceived() {
        this.segmentsReceived++;
    }

    public int getDataSegmentsReceived() {
        return dataSegmentsReceived;
    }

    public void addDataSegmentsReceived() {
        this.dataSegmentsReceived++;
    }

    public int getCorruptDataSegments() {
        return corruptDataSegments;
    }

    public void addCorruptDataSegments() {
        this.corruptDataSegments++;
    }

    public int getDuplicateSegments() {
        return duplicateSegments;
    }

    public void addDuplicateSegments() {
        this.duplicateSegments++;
    }

    public int getDupACKS() {
        return dupACKS;
    }

    public void addDupACKS() {
        this.dupACKS++;
    }
}
