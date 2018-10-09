import java.net.*;
import java.io.*;
import java.util.*;

/**
 * This class STPReceiver will implement a client side TCP receiver over a UDP channel.
 * The receiver is simple, it will first initiate the connection performing a 3-way handshake, it will
 * sit in a loop waiting for for the first SYN packet. upon receiving a SYN packet it will send back
 * a SYNACK. it will then wait for an ack to finish the handshake. after the connection is established the
 * receiver begins to receive packets with payload data from the sender. first the receiver will check if the
 * sequence number of the packet already exists in the list of successful payloads, if so it will just send back
 * a duplicate ACK assuming the checksum is correct. the receiver will also perform the checksum calculation to verify
 * if the header checksum matches the checksum of the payload for corruption.
 * it will send back a NAK on data corruption. Packets will always maintain a state of order through the use
 * of a set, which will not add duplicates and sort the list of packets in order of sequence number. a buffer is used
 * to return accurate ACK cumulative numbers. out of order packets are stored in buffer, and the ack number returned
 * is the last packet in the payload list. After receiving the FIN packet from the sender which will be sent on
 * termination from sender, we begin the 4 way termination closure, we first send the ACK for the FIN, then we
 * send our own FIN, and wait for an ACK back before closing the connection and finishing the program.
 */
public class STPReceiver {
    //this will be the timer counting time since the start of execution
    private STPTimer timer = new STPTimer();
    //we will now create variables to hold connection addresses and ports for both ends
    private InetAddress IP;
    private InetAddress senderIP;
    private int senderPort;
    private int portNumber;
    private DatagramSocket socket;
    //string for file we are requesting from sender
    private String fileRequested;
    //the buffer to hold out of order packet
    private PacketBuffer buffer;
    //the input and output stream of the socket
    private DatagramPacket dataIn = new DatagramPacket(new byte[1000024], 1000024);
    private DatagramPacket dataOut = new DatagramPacket(new byte[1000024], 1000024);
    //set the initial sequence number to 0
    private int sequenceNumber = 0;
    //create variable for holding the ack number
    private int ackNumber;
    //generic packet to avoid re-initialising on each try catch block
    private STPPacketHeader header;
    private STPPacket packet;
    private ReadablePacket r;
    //flags for receiver state
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    //ignore this 1 lol
    private boolean DUP = false;
    //a set that uses array list, will order packets correctly based on sequence number, and
    //will not add duplicates, see PacketSet.java
    private PacketSet payloads = new PacketSet();
    //create an output stream for our pdf file requested (creates the file in this variable)
    private OutputStream pdfFile;
    //this will hold the size of MSS payloads from sender
    private int payloadSize;
    //log file for output
    private FileWriter logFile;
    //flag to check if we received first piece of data
    private boolean firstDataSizeFlag = false;
    //the size of the remainder payload from MSS on last packet
    private int lastPayloadSize;
    //statistic tracking module for end of log file statistics
    private ReceiverLogs PLD = new ReceiverLogs();

    /**
     * This method will be used to create an instance of the STPReceiver,
     * it will pass in the variables from the program
     * arguments passed through from the user, and will create a receiver (client)
     * that will match the behaviour passed in through user input
     *
     * @param args program arguements passed through
     */
    public STPReceiver(String args[]) {
        //parse the program arguements
        this.portNumber = Integer.parseInt(args[0]);
        this.fileRequested = args[1];
        //create a new directory if it doesnt exist called created_files
        //all created files will be stored into this directory
        File dir = new File("created_files");
        dir.mkdir();
        //rename the file to take into account the file path in created_files
        this.fileRequested = dir.getAbsolutePath() + "/" + this.fileRequested;
        //IP address will be the host address of the local machine, (cant use 127.0.0.1)
        //since we cannot bind to that address
        try {
            this.IP = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //creating a UDP socket with the ip and port designated
        try {
            this.socket = new DatagramSocket(this.portNumber, this.IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //create a buffer that at max holds 10000 packets (will be unlikely to surpass this limit)
        //since we have reliability
        this.buffer = new PacketBuffer(10000);
        //create our outputstream for the payloads to be the file in the new directory
        try {
            this.pdfFile = new FileOutputStream(this.fileRequested);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //now we want to create a directory for the log files for each file requested to avoid
        //losing previous log files
        try {
            //create the directory for the file's log files
            dir = new File("log_files_" + args[1]);
            if (!dir.exists()) ;
            dir.mkdir();
            this.logFile = new FileWriter(dir.getAbsoluteFile() + "/" + "Receiver Log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //now we want to do as done in sender, label each column of our log files
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

    /**
     * this will be the function called to initiate the receiver. it will first
     * call the handshake, then receive data, then terminate connection and finally
     * output the statistics to the log files.
     */
    public void operate() {
        timer.run();
        handshake();
        receiveData();
        terminate();
        finishLogFile();
        System.exit(0);
    }

    /**
     * this method will perform a 3-way handshake with the Sender, this begins waiting in a while loop for the SYN
     * packet from the sender, upon receiving the SYN packet we will instantly send back an SYN ACK. finally we
     * will wait in a while loop for the acknowledgement for the SYNACK
     */
    private void handshake() {
        //we will wait in a loop for the SYN packet
        while (!SYN) {
            //receive data through socket
            try {
                socket.receive(dataIn);
                //add 1 to number of segments received
                PLD.addSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //create a new readable version of the packet to be able to convert binary packet to
            //understandable values
            r = new ReadablePacket(dataIn);
            //if we have received a SYN, we want to write the SYN to the log file and set SYNACK to be true
            if (r.isSYN()) {
                logWrite(0, 0, 0, "rcv", "S");
                SYN = true;
                ACK = true;
            }
        }
        //we want to bind our output to the receiving datagrams source ip + port
        this.senderIP = r.getSourceIP();
        this.senderPort = r.getSourcePort();
        dataOut.setAddress(r.getSourceIP());
        dataOut.setPort(r.getSourcePort());
        //add 1 to the ack number since we ACK'd the syn bit
        ackNumber = 1;
        //create our SYNACK response to send to the sender
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        logWrite(0, sequenceNumber, ackNumber, "snd", "SA");
        sendPacket(packet);
        //now we wait for the reply that our SYNACK has been acknowledged
        while (true) {
            try {
                //receive data from the sender
                socket.receive(dataIn);
                PLD.addSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //convert the packet into a readable form
            r = new ReadablePacket(dataIn);
            //if we received an ACK for our SYNACK we want to exit
            if (r.isACK() && r.isSYN())
                break;
        }
        //add 1 to current sequence number since we sent the ACK
        sequenceNumber++;
        //write the ack to the log file
        logWrite(0, sequenceNumber, ackNumber, "rcv", "A");
        //output handshake final packet for debug purposes
        r.display();
        System.out.println("handshake complete");
    }

    /**
     * This method will be a reliable receiver operation that receives datagram packets from the sender side;
     * converts these datagrams into a readable form for analysing, and will send back a response. If a packet
     * is discovered to be a duplicate, we will send back a duplicate acknowledgement. If the packet is
     * out of order, we will store in the buffer, and only return the ack of the last packet in the final list
     * of payloads. if the checksum of the packet does not match the one in the header, we will return a NAK
     * indicating data corruption. upon receiving the FIN packet from the sender, which is sent on sender side's
     * termination, we will exit and begin termination. this can be done simply on one thread as there is only
     * one responsibility.
     */
    private void receiveData() {
        //create an infinite loop that breaks on condition
        while (true) {
            //if there are packets within the payload set
            if(payloads.size() > 5) {
                //we want to start clearing junk memory since packet allocation
                //using a huge amount of memory
                System.gc();
            }
            //we want output to show status of heap (so it doesnt look like the program is just crashed)
            System.out.println("free heap size - " + Runtime.getRuntime().freeMemory());
            //attempt to receive a packet from the sender
            try {
                socket.receive(dataIn);
                //add 1 to the data segments, segments received and add number of bytes in the payload to the
                //statistics
                PLD.addBytesReceived(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER);
                PLD.addSegmentsReceived();
                PLD.addDataSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //create a readable version of the packet from the packet received
            r = new ReadablePacket(dataIn);
            //if we havent received a packet before
            if (!firstDataSizeFlag) {
                //we want to set our payload size (MSS) as the length of the packet - 32 bits from header
                payloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                //initialise our lastPayloadSize incase the file evenly divided by MSS bytes
                lastPayloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                //set flag to true so this is never called again
                firstDataSizeFlag = true;
                //now we want to remove the padding 1000000 bytes from the payload by resizing the byte array
                r.setPayload(unpaddedPayload(r, false));
            }
            //if the size of the packet is different to our MSS and its larger than 0, this indicates last packet
            if (dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER > 0 &&
                    (dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER != payloadSize)) {
                //so we want to set the last packet size to the unqiue size in the last packet
                lastPayloadSize = dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER;
                //if the last packet has a faulty checksum we send a NAK
                if (!validCheckSum(r, lastPayloadSize)) {
                    PLD.addCorruptDataSegments();
                    ACK = false;
                } else {
                    //otherwise we send back an ACK
                    ACK = true;
                }
            }
            //otherwise we will use MSS to check for checksum (since we scan through 0-input bytes)
            else {
                //if the checksum is not equivalent and its not 0 (empty)
                if (!validCheckSum(r, payloadSize) && r.getChecksum() != 0) {
                    //add 1 to corrupted packets and return a NAK
                    PLD.addCorruptDataSegments();
                    ACK = false;
                } else {
                    //otherwise send back an ack
                    ACK = true;
                }
            }
            //now we want to reduce any padding the payload could have in the packet before we store it
            minimizedPayload(r);
            //if the packet received already exists (duplicate)
            if (payloads.contains(r)) {
                //we want to add 1 to our duplicate segments received
                PLD.addDuplicateSegments();
                //create our ACK response however we log down that it is a duplicate ACK
                header = new STPPacketHeader(0, sequenceNumber, r.getSequenceNumber(), IP,
                        r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
                packet = new STPPacket(this.header, new byte[0]);
                sendPacket(packet);
                logWrite(0, sequenceNumber, ackNumber, "snd/DA", "A");
                continue;
            }
            //if we have an ACK and the sequence number jumps past MSS
            //we want to add the packet to the buffer for out of order packets
            if (ACK && r.getSequenceNumber() > (payloads.last() + payloadSize)) {
                buffer.add(r);
            }
            //otherwise we want to add the packet to the list of packets for writing
            else {
                if (ACK) {
                    payloads.add(r);
                }
            }
            //if the ack number is equivalent to the last ack in the list of packets for writing
            //add 1 to the number of duplicate acks
            if (ackNumber == payloads.last())
                PLD.addDupACKS();
            //set the ack number to be the last sequence number inside the list of packets
            ackNumber = payloads.last();
            //if we have a NAK
            if (!ACK) {
                //we want to write received a corruption
                logWrite(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER,
                        r.getSequenceNumber() - (payloadSize - lastPayloadSize),
                        r.getAcknowledgemntNumber(), "rcv/corr", "D");
            }
            //otherwise we want to write rcv succesfful
            else {
                //if we receive an empty payload which indicates FIN, we want to write a FIN to the log file
                if (dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER == 0) {
                    logWrite(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER,
                            r.getSequenceNumber() - (payloadSize - lastPayloadSize),
                            r.getAcknowledgemntNumber(), "rcv", "F");
                }
                //otherwise we will write acknowledge data
                else {
                    logWrite(dataIn.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER,
                            r.getSequenceNumber() - (payloadSize - lastPayloadSize),
                            r.getAcknowledgemntNumber(), "rcv", "D");
                }
            }
            //now since our payloads are stored in an ordered set, we can add them from the buffer into
            //the list without effecting the ordering of the packets (inserted in order)
            for (ReadablePacket r : buffer.getBuffer()) {
                payloads.add(r);
            }
            //if we have received a FIN
            if (r.isFIN()) {
                //we want to add 1 to segments received
                PLD.addSegmentsReceived();
                //add whatever might be left in the buffer just incase
                for (ReadablePacket r : buffer.getBuffer()) {
                    payloads.add(r);
                    //buffer.remove(r);
                }
                //return from the procedure (this is our break from the loop)
                return;
            }
            //otherwise, remove any duplicates from the buffer that already exist
            buffer.removeDuplicates(payloads);
            //more status information of number of packets successfully received
            System.out.println("number of  packets - " + payloads.size());
            //now we want to create our header with either our ACK/NAK and send the acknowledgement
            //to the sender
            header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                    r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
            packet = new STPPacket(this.header, new byte[0]);
            sendPacket(packet);
            //write to the log file that we are sending  with out ack/nak
            logWrite(0, sequenceNumber, ackNumber, "snd", "A");
        }
    }

    /**
     * This method will simulate the TCP 4 way closure, upon receiving the FIN during the receiveData procedure
     * we will then send back an ACK to acknowledge the close of the sender, afterwards we will send a FIN to the
     * sender, and then wait in a while true loop for an ACK for our FIN. upon receiving our ACK we will then terminate
     * the connection and the sender side will already have terminated.
     */
    private void terminate() {
        //4 way handshake closure, since the senders will initiate the close
        //first send back the ACK for the senders FIN
        DUP = false;
        FIN = true;
        ACK = true;
        //now we want to create our ACK for the FIN to send to the sender
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
        packet = new STPPacket(this.header, new byte[0]);
        sendPacket(packet);
        //write to the log file that we transmitted to the sender
        logWrite(0, sequenceNumber, r.getSequenceNumber() - (payloadSize - lastPayloadSize) + 1,
                "snd", "A");
        //now we want to send back our FIN to initiate our side of the closure
        ACK = false;
        //create our FIN packet with an empty payload
        header = new STPPacketHeader(0, sequenceNumber, ackNumber, IP,
                r.getSourceIP(), portNumber, r.getSourcePort(), SYN, ACK, FIN, DUP);
        packet = new STPPacket(this.header, new byte[0]);
        //send the FIN to the sender
        sendPacket(packet);
        //write to the log file that we sent our FIN
        logWrite(0, sequenceNumber, r.getSequenceNumber() - (payloadSize - lastPayloadSize) + 1,
                "snd", "F");
        //now we want to wait for our ack from the sender
        while (true) {
            try {
                //try receiving a response from the sender
                socket.receive(dataIn);
                //add 1 to segments received
                PLD.addSegmentsReceived();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //when we receive a packet we will want to convert it to be in readable form
            r = new ReadablePacket(dataIn);
            //if the packet is an ACK and still FIN we want to terminate conncection
            //note sender side connection is already terminated at this point
            if (r.isACK() && r.isFIN()) {
                logWrite(0, r.getSequenceNumber() - (payloadSize - lastPayloadSize) + 1,
                        ++sequenceNumber, "rcv", "A");
                //break from the loop once we receive our ack
                break;
            }
        }
        //upon completion of our termination close the socket
        socket.close();
        //and write to the log file the statistics of the receiver
        writeFile();
    }

    /**
     * This method will compute the checksum of the payload in the datagram, it will first sum all the bytes together
     * and then return the one's complement of that sum
     *
     * @param payload the payload received from the sender
     * @param length  the length of the payload to avoid padding issues with the datagram
     * @return the checksum value from the computation
     */
    private int checksum(byte[] payload, int length) {
        //start a running sum
        int sum = 0;
        //for each byte in the payload
        for (int i = 0; i < length; i++) {
            //add to the sum the current value of the byte
            sum += (int) payload[i];
        }
        //return the one's complement of the calculation
        sum = ~sum;
        return sum;
    }

    /**
     * @param r      the packet we are validating our checksum for
     * @param length the length of the packet in bytes
     * @return true if the packet checksum matches the header checksum, false if not
     */
    private boolean validCheckSum(ReadablePacket r, int length) {
        //return the boolean that the checksum in the header, is equal to the payload checksum
        return r.getChecksum() == checksum(r.getPayload(), length);
    }

    /**
     * This method will send a datagram packet to the sender with 0 loss
     *
     * @param p the packet we are sending to the sender
     */
    private void sendPacket(STPPacket p) {
        //set our data to be the data in the packet
        dataOut = p.getPacket();
        //set the destination address to the sender
        dataOut.setAddress(senderIP);
        dataOut.setPort(senderPort);
        try {
            //send the datagram to the sender
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method will write all the gathered payloads into our file output stream. before we write we want
     * to perform a saftey sort, to sort all packets by sequence number. after we want to remove the final FIN
     * packet we received from the receivedata method which will always be the last packet in the set. finally
     * we want to write all the data in the payloads into the fileoutpuststream
     */
    private void writeFile() {
        //saftey sort incase somehow we have any out of order packets
        payloads.sortSet();
        //more status output so it doesnt just look frozen
        System.out.println("--- payload sizes ------");
        System.out.println(" ==> " + payloads.size() + " <== ");
        System.out.println("-------");
        //if there is more than 1 payload, we want to remove the final FIN 0 payload packet
        if (payloads.size() > 1)
            payloads.remove(payloads.get(payloads.size() - 1));
        //now we want to write each packet in the payload array to the file output stream
        try {
            //for each packet in the payload list
            for (ReadablePacket r : payloads.getArrayList()) {
                //write the byte payload to the output stream
                pdfFile.write(unpaddedPayload(r, r.equals(payloads.get(payloads.size() - 1))));
                //flush to allow for further writing
                pdfFile.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //finally close the PDF file
            pdfFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will write the length of the payload removing the extra padding from the datagram packet
     * this will make sure we only write data from the payload and nothing else to the file output stream
     *
     * @param r    the packet that has the payload we are writing
     * @param last if this is the last packet or not
     * @return the byte array that contains only payload data
     */
    public byte[] unpaddedPayload(ReadablePacket r, boolean last) {
        //if its the last packet
        if (last) {
            //return a copy of the array from 0 -> index last payload size
            return Arrays.copyOf(r.getPayload(), lastPayloadSize);
        }
        //otherwise return a coupy of the array from 0 -> MSS
        return Arrays.copyOf(r.getPayload(), payloadSize);
    }

    /**
     * this method will remove extra padding bytes from the datagram packet itself leaving only the payload
     *
     * @param r the packet we are removing the padding from the payload from
     */
    public void minimizedPayload(ReadablePacket r) {
        //we want to create a new byte array of size payload size
        byte[] set = new byte[payloadSize];
        //for each byte from 0 -> payload create a copy in our new set byte array
        for (int i = 0; i < payloadSize; i++) {
            set[i] = r.getPayload()[i];
        }
        //set the payload for the packet to be our set byte array
        r.setPayload(set);
    }

    /**
     * this method will write to the log file based on the paramters passed through, it is used to
     * print formatted data correctly to the log file.
     *
     * @param length         the length of the payload we are sending/receiving
     * @param sequenceNumber the sequence number of the packet we are sending/receiving
     * @param ackNumber      the ack number of the packet we are sending/receiving
     * @param sndOrReceive   the snd or receive to indicate if we are sending or receiving data
     * @param status         the status if we are acknowledging or delivering data, or SYN/FIN
     */
    public void logWrite(int length, int sequenceNumber, int ackNumber, String sndOrReceive, String status) {
        //first we want to get the time passed from the timer, in seconds by dividing the time passed by 1000
        //since timepassed is in milliseconds
        float timePassed = timer.timePassed() / 1000;
        //create our formatted string to seperate in spaced columns to print out all paramters passed through
        String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s\n", sndOrReceive
                , timePassed, status, sequenceNumber, length, ackNumber);
        try {
            //write the formatted string to the log file
            logFile.write(s);
            //flush to allow for further writing
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method will print the statistics of the receiver side to the log file after successful termination
     */
    public void finishLogFile() {
        //seperate to indicate statistic section
        String s = "--------------------------------------------------------------------------\n";
        try {
            //write the seperator to the log file
            logFile.write(s);
            logFile.flush();
            //print the amount of data received in bytes to the log file
            s = String.format("%-50s %-20s\n", "Amount of data received (bytes)", PLD.getBytesReceived());
            logFile.write(s);
            logFile.flush();
            //print the amount of segments received to the log file
            s = String.format("%-50s %-20s\n", "Total Segments Received", PLD.getSegmentsReceived());
            logFile.write(s);
            logFile.flush();
            //print the amount of data segments  received to the log file
            s = String.format("%-50s %-20s\n", "Data segments received", PLD.getDataSegmentsReceived());
            logFile.write(s);
            logFile.flush();
            //print the amount of data segments with bit errors  received to the log file
            s = String.format("%-50s %-20s\n", "Data segments with Bit Errors", PLD.getCorruptDataSegments());
            logFile.write(s);
            logFile.flush();
            //print the amount of duplicate data segments received to the log file
            s = String.format("%-50s %-20s\n", "Duplicate data segments received", PLD.getDuplicateSegments());
            logFile.write(s);
            logFile.flush();
            //print the amount of duplicate ACKs sent to the log file
            s = String.format("%-50s %-20s\n", "Duplicate ACKs sent", PLD.getDupACKS());
            logFile.write(s);
            logFile.flush();
            //print the final seperator to indicate end of file to the log file
            s = "--------------------------------------------------------------------------\n";
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}