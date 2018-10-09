/**
 * This class similair to unreliability will be used to collect statistics
 * for the receiver log file upon completeing transmission
 */
public class ReceiverLogs {
    //these will be counters to count events that occur on the receiver side
    private int bytesReceived = 0;
    private int segmentsReceived = 0;
    private int dataSegmentsReceived = 0;
    private int corruptDataSegments = 0;
    private int duplicateSegments = 0;
    private int dupACKS = 0;

    /**
     * This method will return the number of segments received from the sender
     *
     * @return the number of segments received from the sender
     */
    public int getSegmentsReceived() {
        //return the number of segments received from the sender
        return segmentsReceived;
    }

    /**
     * This method will return the number of bytes received from the sender
     *
     * @return the number of bytes received from the sender
     */
    public int getBytesReceived() {
        //return the number of bytes received from the sender
        return bytesReceived;
    }

    /**
     * This method when called will add the number of bytes to the running counter
     * of total number of bytes received
     *
     * @param bytesReceived the number of bytes we are adding to our counter
     */
    public void addBytesReceived(int bytesReceived) {
        //add the number of bytes received to our running counter
        this.bytesReceived += bytesReceived;
    }

    /**
     * This method when called will increment the counter for Segments Received by 1
     */
    public void addSegmentsReceived() {
        //increment the Segments Received counter by 1
        this.segmentsReceived++;
    }

    /**
     * This method will return the number of Data Segments received from the sender
     *
     * @return the number of Data Segments received from the sender
     */
    public int getDataSegmentsReceived() {
        //return the number of Data Segments received from the sender
        return dataSegmentsReceived;
    }

    /**
     * This method when called will increment the counter for Data Segments Received by 1
     */
    public void addDataSegmentsReceived() {
        //increment the Data Segments Received counter by 1
        this.dataSegmentsReceived++;
    }

    /**
     * This method will return the number of Corrupted Data Segments received from the sender
     *
     * @return the number of Corrupted Data Segments received from the sender
     */
    public int getCorruptDataSegments() {
        //return the number of Corrupted Data Segments received from the sender
        return corruptDataSegments;
    }

    /**
     * This method when called will increment the counter for corrupted Data Segments Received by 1
     */
    public void addCorruptDataSegments() {
        //increment the Corrupted Data Segments Received counter by 1
        this.corruptDataSegments++;
    }

    /**
     * This method will return the number of Duplicate Segments received from the sender
     *
     * @return the number of Duplicate Segments received from the sender
     */
    public int getDuplicateSegments() {
        //return the number of Duplicate Segments received from the sender
        return duplicateSegments;
    }

    /**
     * This method when called will increment the counter for Duplicate Data Segments Received by 1
     */
    public void addDuplicateSegments() {
        //increment the Duplicate Data Segments Received counter by 1
        this.duplicateSegments++;
    }

    /**
     * This method will return the number of Duplicate ACKS sent to the sender
     *
     * @return the number of Duplicate ACKS sent to the sender
     */
    public int getDupACKS() {
        //return the number of Duplicate ACKS sent to the sender
        return dupACKS;
    }

    /**
     * This method when called will increment the counter for Duplicate ACKS sent by 1
     */
    public void addDupACKS() {
        //increment the Duplicate ACKS counter by 1
        this.dupACKS++;
    }
}
