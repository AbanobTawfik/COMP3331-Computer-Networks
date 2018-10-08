import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This class STPSender will implement a server side TCP sender over a UDP channel. reliability is implemented in the form
 * of acknowledgements from the receiver. the class guarantees that any packet once placed in the window will guaranteed to be
 * delivered to the receiver before being removed from the window. Initially the file is chunked into MSS packets and a packet
 * that contains remainder bytes from MSS. and is stored into a list. these packets are sent through a UDP socket, whilst at the same
 * time receiving acknowledgements from the  receiver through the use of multi-threading. since most transfers will occur over
 * a reliable channel, unreliability was also implemented as per specification through the use of a PLD module.
 */
public class STPSender {
    //this will be the initial value from RTT (RTT = timercv - timesnd)
    private int sendTime;
    //these will be used to check if the requested file is contained within the directory
    private File folder = new File(System.getProperty("user.dir"));
    private File[] allFiles = folder.listFiles();
    //variable for ip and port used for the sender side UDP socket
    private InetAddress IP;
    private int portNumber;
    //UDP SOCKET (NOT TCP)
    private DatagramSocket socket;
    private String fileRequested;
    //the two different UDP receive/send packets for input/output streams
    private DatagramPacket dataIn = new DatagramPacket(new byte[1000024], 1000024);
    private DatagramPacket dataOut = new DatagramPacket(new byte[1000024], 1000024);
    //initialising sequence number (ACK NUMBER COMES FROM RECEIVER SIDE)
    private int sequenceNumber = 0;
    private int ackNumber;
    //creating general packet to avoid re-initialising and allow easy use with try catch
    private STPPacketHeader header;
    private STPPacket packet;
    private ReadablePacket r;
    //flags for stage of transmissions (DUP was in early development however is redundant)
    private boolean SYN = false;
    private boolean ACK = false;
    private boolean FIN = false;
    private boolean DUP = false;
    //receiver UDP socket information
    private InetAddress receiverIP;
    private int receiverPort;
    //PLD MODULE
    private Unreliability PLD;
    //below are the paramaters for the window and timer
    private int MWS;
    private int MSS;
    private float gamma;
    //creating a new array to store file packet when file is initially processed
    private ArrayList<ReadablePacket> filePackets = new ArrayList<ReadablePacket>();
    //creating a threadsafe blocking queue that allows storage/removal from multi-threaded proccess
    private volatile ArrayBlockingQueue<ReadablePacket> window;
    //creating a counting timer for time since execution of code used in log files
    private STPTimer timer = new STPTimer();
    //file input stream for reading requested formatted file
    private FileInputStream file;
    //creating a thread safe variable since it is accessed from multiple threads
    //this variable will point to current index in our list of packets we are sending
    private volatile int windowIndex = 0;
    private int windowSize;
    //log output purposes
    private FileWriter logFile;
    //initialising our RTT variables which change throughout runtime
    private int estimatedRTT = 500;
    private int devRTT = 250;
    private int initialEstimatedRTT = 500;
    private int initialDevRTT = 250;
    //creating a queue for our duplicate acks that limits to 3, on 3 we clear for fast retransmit
    private PriorityQueue<Integer> dupAcks = new PriorityQueue<Integer>(3);
    //random number generator for PLD module
    private Random rand;
    //below are extra variables used as flags or temporary place holding in the PLD module
    private int count;
    private boolean reOrder = false;
    private int windowreOrder;
    private STPPacket reOrderPacket;
    //this variable will hold the size of the final packet, since we cannot just split all packets into MSS
    private int finalPacketSize = 0;

    /**
     * This method will be used to create an instance of the STPSender, it will pass in the variables from the program
     * arguments passed through from the user, and will create a sender (server) that will match the behaviour passed
     * in through user input
     *
     * @param args program arguements passed through
     */
    public STPSender(String args[]) {
        //first we want to initialise the IP address as the address of the current machine (cant use 127.0.0.1) need
        //canonical ip to bind to
        try {
            this.IP = InetAddress.getByName(InetAddress.getLocalHost().getCanonicalHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //randomise current port number to 2000 + a random number between 0-60000 (since 1-1024 taken and max 65535)
        this.portNumber = 2000 + new Random().nextInt(60000);
        try {
            //create our sender side socket with the port + ip
            this.socket = new DatagramSocket(this.portNumber, this.IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //if the receiver ip is local host or 127.0.0.1 since we cannot bind to 127.0.0.1
            if (args[0].equals("localhost") || args[0].equals("127.0.0.1")) {
                //set the receiver ip address to host address of the machine
                this.receiverIP = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
            } else {
                //otherwise we use the ip address passed through
                this.receiverIP = InetAddress.getByName(args[0]);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        //receiver port is the second arguement passed through
        this.receiverPort = Integer.parseInt(args[1]);
        //set the location for our output  to the receivers ip and port
        dataOut.setAddress(this.receiverIP);
        dataOut.setPort(this.receiverPort);
        //extract file requested from program arguement 3
        this.fileRequested = args[2];
        //next three variables from program arguements will be the window and timer variables
        this.MWS = Integer.parseInt(args[3]);
        this.MSS = Integer.parseInt(args[4]);
        this.gamma = Float.parseFloat(args[5]);
        //set our maximum window size to be the floor division of the maximum
        // window size divided by maximum segment size
        this.windowSize = Math.floorDiv(MWS, MSS);
        //if the window size is less than or equal to 1 due to botched arguements passed through
        if (windowSize <= 1) {
            //set maximum window size as 1
            this.windowSize = 1;
            //set the maximum segment size as the window size (will be stop and wait like this however so dont pass
            //a MWS greater than MSS
            this.MSS = this.MWS;
        }
        //create our window to have a maximum capacity of the max window size
        window = new ArrayBlockingQueue<>(windowSize);
        //extract PLD module variables from the rest of program arguements
        float pDrop = Float.parseFloat(args[6]);
        float pDuplicate = Float.parseFloat(args[7]);
        float pCorrupt = Float.parseFloat(args[8]);
        float pOrder = Float.parseFloat(args[9]);
        int maxOrder = Integer.parseInt(args[10]);
        float pDelay = Float.parseFloat(args[11]);
        int maxDelay = Integer.parseInt(args[12]);
        long seed = Long.parseLong(args[13]);
        //create a new PLD module that contains multiple functionallity, see class unreliability
        this.PLD = new Unreliability(pDrop, pDuplicate, pCorrupt, pOrder, maxOrder, pDelay, maxDelay, seed);
        //now we want to initialise our log file
        // with the name sender_log.txt" inside a directory for our files log file
        try {
            //we want to initialise a directory for the requested file's log file
            File dir = new File("log_files_" + fileRequested);
            //if the directory does not exist, we want to create it (else we ignore it)
            if (!dir.exists())
                dir.mkdir();
            //we want to now put the sender side log file inside the directory
            this.logFile = new FileWriter(dir.getAbsoluteFile() + "/" + "Sender Log.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //now we want to iniitalise the log files with labels for what each column represents
            String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s\n", "snd/rcv", "time",
                    "type", "sequence", "payload size", "ack");
            //write the string to the log file
            logFile.write(s);
            //flush to allow for more writing
            logFile.flush();
            //we want to create seperator now to indicate beginning of execution now
            s = "--------------------------------------------------------------------------\n";
            //write and flush to log file
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //create our random generator to use the seed passed into the PLD module
        this.rand = new Random(this.PLD.getSeed());
    }

    /**
     * this method will be called from the Sender where the entire program will run
     * this method will first begin the timer, perform 3 way handshake, send the data to the receiver,
     * terminate the connection and finally print the statistics to the log file in a summary.
     * at the end of this function
     * an exit with status 0 is called to indicate file was sent correctly!
     */
    public void operate() {
        //start the counting timer running on a seperate thread
        timer.start();
        //now we want to convert our file into packet chunks
        prepareFile();
        //perform the 3 way handshake
        handshake();
        //send the data from the sender to receiver
        sendData();
        //terminate the connection between both sender/receiver
        terminate();
        //print the summary statistics to the log file
        finishLogFile();
        //exit successfully if all previosu steps were successful
        System.exit(0);
    }

    /**
     * this method will be called from the Sender where the entire program will run
     * this method will first begin the timer, perform 3 way handshake, send the data to the receiver,
     * terminate the connection and finally print the statistics to the log file in a summary.
     * at the end of this function an exit with status 0 is called to indicate file was sent correctly!
     */
    private void prepareFile() {
        //first we want to check if the file exists within the current directory
        //if the file doesn't exist in current directory,
        // we exit with error status and a easy to understand error message
        if (!containsFile(fileRequested)) {
            System.out.println("The file requested does not exist in this directory");
            System.exit(1);
        } else {
            //we want to create a list of ready to send packets
            //(since they are file data we want to turn off most flags)
            try {
                //now we want to create our file input stream to read the file in
                file = new FileInputStream(fileRequested);
                //if the file is smaller than the maximum segment size of the packet
                //we want to modify our maximum segment size to be the file size
                if (MSS > file.available()) {
                    MSS = file.available();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //we want to create our data payload that stores MSS amount of bytes
            byte[] packetPayload = new byte[MSS];
            //create a variable to see how many bytes are read as a check
            int read = 0;
            while (true) {
                try {
                    //if the MSS is larger than remaining bytes in file and the remaining bytes is not 0
                    //that means we have remainder bytes inside the last packet
                    if (file.available() < MSS && file.available() != 0) {
                        //set the final packet size to be the remaining bytes in file
                        finalPacketSize = file.available();
                        //refactor the payload to store the remaining bytes in file instead of MSS
                        packetPayload = new byte[file.available()];
                    }
                    //now we want to read from the file into the byte array
                    read = file.read(packetPayload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //if the number of bytes read is less than or equal to 0, it means reached end of file
                //exit from the while loop
                if (read <= 0) {
                    break;
                }
                //now we want to store the payload into a packet creating its header first
                header = new STPPacketHeader(checksum(packetPayload), sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
                //increment sequence number to be stored into packet
                sequenceNumber += MSS;
                //create our packet from the payload and header
                packet = new STPPacket(header, packetPayload);
                //and also create a readable version of the packet that can go between readable and packet
                r = new ReadablePacket(packet.getPacket());
                //add the readable version of the packet to the file packets
                filePackets.add(r);
            }
        }
    }

    /**
     * this method will perform a 3-way handshake with the receiver, this begins by the sender sending an
     * SYN packet. if no response is received it will keep re-transmitting the SYN packet, after sending the SYN
     * the sender will wait in a while true loop waiting for the SYNACK. upon receiving the SYNACK from the sender
     * it will then finish the handshake by sending an ACK to the receiver confirming the handshake was successful
     */
    private void handshake() {
        //set SYN to be true for first packet sent
        SYN = true;
        //create a header with no checksum, initial sequence number as 0, and ack number as 0
        header = new STPPacketHeader(0, filePackets.get(0).getSequenceNumber(), 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        //create a packet with empty payload to send to receiver
        packet = new STPPacket(header, new byte[0]);
        //send the packet to the receiver
        sendPacket(packet);
        //write the output to log file
        logWrite(0, filePackets.get(0).getSequenceNumber(), 0, "snd", "S",
                (estimatedRTT + (int) (gamma) * devRTT));
        //now we want to wait for the SYN ACK back, so we wait in a while true loop
        while (true) {
            try {
                //set the timeout to be the initial timeout value
                socket.setSoTimeout(estimatedRTT + (int) (gamma) * devRTT);
                //set the address from where we receiver our data to be from the receiver
                dataIn.setAddress(receiverIP);
                dataIn.setPort(receiverPort);
                //attempt to receive packet from the receiver
                socket.receive(dataIn);
                //if we have received data before the timeout
                //we want to convert the byte packet into a readable packet
                r = new ReadablePacket(dataIn);
                //if we received a SYN and ACK (SYNACK)
                if (r.isSYN() && r.isACK()) {
                    //set ACK as true
                    ACK = true;
                    //get the acknumber from the receiver's sequence number
                    ackNumber = r.getSequenceNumber();
                    //write output to log file
                    logWrite(0, r.getSequenceNumber(), 1, "rcv", "SA",
                            (estimatedRTT + (int) (gamma) * devRTT));
                    //break from the while true loop
                    break;
                }
            }
            //if the we do not receive a reply from the receiver within timeout
            catch (SocketTimeoutException e) {
                //we want to send over the exact same SYN packet as above
                SYN = true;
                header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
                packet = new STPPacket(header, new byte[0]);
                sendPacket(packet);
                //write the output to the log file
                logWrite(0, filePackets.get(0).getSequenceNumber(), 0, "snd", "S",
                        (estimatedRTT + (int) (gamma) * devRTT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //end handshake with the ACK, send back packet with header containing updated flags (ACK from SYNACK)
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        //write output to log file
        logWrite(0, filePackets.get(0).getSequenceNumber() + 1, ackNumber + 1,
                "snd", "A", (estimatedRTT + (int) (gamma) * devRTT));
        //display packet info to show successful handshake debug reasons
        r.display();
    }

    /**
     * this method will reliably send each packet in the filepacket list to the sender. the sending/receiving of acks is
     * seperated by multi-threading where we perform both simultaneous to replicate sliding window. since each packet
     * placed in the window will be guaranteed to be sent before removal, we will simply count number of packets put in
     * the window. if we have put the filepackets size worth of packets in the window, we will return since that means
     * all packets will have sent. once the window is empty, and all packets were put int he window we will begin termination
     */
    private void sendData() {
        //initialising the count
        count = filePackets.size() + 1;
        //start a new thread for sending packets in the window THIS IS THE SENDING THREAD
        new Thread(new Runnable() {
            @Override
            public void run() {
                //start an infinite loop which we will break on our exit condition
                while (true) {
                    //exit condition, when window is empty and all packets sent
                    if ((count <= 1 && window.size() == 0)) {
                        break;
                    }
                    //since multi-threading is used and windowIndex is a shared variable, we want to make sure
                    //we do not proccess past last packet
                    if (windowIndex >= filePackets.size()) {
                        continue;
                    }
                    //if there is room inside our window we will transmit packets based on current index
                    //each time a packet is put in window it will be guaranteed to be sent, so we want to point to the
                    //next packet to send once one is put in
                    if (window.remainingCapacity() > 0) {
                        //if we have re-ordering we want to check if we passed the max-reorder size
                        if (reOrder && windowIndex == windowreOrder) {
                            //if so we want to send our packet we held onto
                            sendPacket(reOrderPacket);
                            //turn the flag on that re-ordering is no longer on to allow for more re-ordering
                            reOrder = false;
                        }
                        //we want to get create a new packet from the current window index we are pointing to
                        packet = new STPPacket(filePackets.get(windowIndex));
                        try {
                            //now we want to use our thread-safe method to add that packet into the window
                            //to keep track of the packet
                            window.put(filePackets.get(windowIndex));
                            //decrement our packets remaining counter
                            count--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //increment the index to point to the next packet
                        windowIndex++;
                        //now we want to pass the packet through the PLD module
                        PLDSend(packet);

                    }
                }
            }
        }).start();

        //start a new thread for receiving ACKS from our packet, and clear the window
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //same exit condition as the sender thread, once we have acked every packet
                    if ((count <= 1 && window.size() == 0)) {
                        break;
                    }
                    try {
                        //set the send time as time before we receive packet for RTT calculation
                        sendTime = (int) System.currentTimeMillis();
                        //we want to set the address we are receiving data from, to be the receiver port + IP
                        dataIn.setAddress(receiverIP);
                        dataIn.setPort(receiverPort);
                        //attempt to receive data from receiver within timeout range
                        socket.receive(dataIn);
                        //now we want to create a readable version of the packet
                        r = new ReadablePacket(dataIn);
                        //if the packet is ACK = no corruption successfully received
                        if (r.isACK()) {
                            //fast retransmit checks
                            //if there is nothing in the dupacks list, we want to add the current ack number
                            //to the dupacks list
                            if (dupAcks.size() == 0) {
                                dupAcks.add(r.getAcknowledgemntNumber());
                            }
                            //if the size of dupacks is 3, we want to perform a fast-retransmit
                            //and clear the dupacks list
                            if (dupAcks.size() == 3) {
                                fastRetransmit();
                                dupAcks.clear();
                            }
                            //otherwise we want to check if we have a duplicate ack
                            // by comparing it to the current list
                            if (dupAcks.size() == 1 || dupAcks.size() == 2) {
                                //set a flag for checking if we have a duplicate, upon unique we break and reset
                                boolean flag = true;
                                //scan through list of dupack numbers
                                for (Integer i : dupAcks) {
                                    //if the acknowledgment number is not the same to any of the index's
                                    if (r.getAcknowledgemntNumber() != i) {
                                        //we want to set flag to false and exit
                                        flag = false;
                                        break;
                                    }
                                }
                                //if the flag was unchanged (same as all other values in dupacks list)
                                if (flag) {
                                    //add to the statistics a dupack
                                    PLD.addDuplicateACKS();
                                    //add the current ack number to the list increasing the size
                                    dupAcks.add(r.getAcknowledgemntNumber());
                                }
                                //otherwise we want to reset the dupAcks as we have a unique number now
                                else {
                                    dupAcks.clear();
                                    //and we also want to initialise our new dupack list with the current ack number
                                    dupAcks.add(r.getAcknowledgemntNumber());
                                }
                            }
                            //now that fast re-transmit is taken care of, since we had an ACK
                            //we want to scan through the window to see which packet in the window we are removing
                            for (ReadablePacket read : window) {
                                //if the ack number is the same as the sequence number of the packet in the window
                                if (r.getAcknowledgemntNumber() == read.getSequenceNumber()) {
                                    //we want to update our RTT from the calculation
                                    socket.setSoTimeout(calculateRTT());
                                    //we want to update the acknumber to be the sequence number of the packet we
                                    //are removing
                                    ackNumber = r.getSequenceNumber();
                                    //remove the packet from the window
                                    window.remove(read);
                                    //if we have a duplicate ACK we want to do a special log print
                                    //otherwise we print normally to the logfile
                                    if (dupAcks.size() > 1) {
                                        logWrite(0, ackNumber, r.getAcknowledgemntNumber(), "rcv/DA",
                                                "A", estimatedRTT);
                                    } else {
                                        logWrite(0, ackNumber, r.getAcknowledgemntNumber(), "rcv",
                                                "A", estimatedRTT);
                                    }
                                }
                            }
                            //otherwise continue to go to next to keep scanning
                            continue;
                        }
                        //otherwise this implies ACK = false so we want to re-transmit
                        else {
                            //first we want to scan through the window to find the sequence number of our NAK
                            for (ReadablePacket read : window) {
                                //if the acknowledgement number is equal to the sequence number of the
                                //packet in the window
                                if (r.getAcknowledgemntNumber() == read.getSequenceNumber()) {
                                    //we want to create the packet in binary form from our readable version
                                    packet = new STPPacket(read);
                                    //send the packet through the PLD module for re-transmit
                                    PLDSend(packet);
                                    //we want to print to log file a re-transmit and break from the loop
                                    logWrite(MSS, read.getSequenceNumber(), read.getAcknowledgemntNumber(),
                                            "snd/RXT", "D", calculateRTTWithNoChange());
                                    break;
                                }
                            }
                        }
                    }
                    //in the case of timeout from our socket, we want to perform a re-transmission
                    //of the first packet inside the window
                    catch (SocketTimeoutException e) {
                        //first we want to add to our counter of timeouts
                        PLD.addPacketsTimedOut();
                        //we want to get the first packet from the window to re-transmit
                        ReadablePacket retransmit = window.peek();
                        //since we are multi-threading we want to see if the packet was acked within this time
                        //to avoid a null re-transmit packet so we go next
                        if (null == retransmit)
                            continue;
                        //create the binary packet from the readable packet form
                        packet = new STPPacket(retransmit);
                        //send the packet through the PLD module
                        PLDSend(packet);
                        //write the retransmit to the log file
                        logWrite(MSS, retransmit.getSequenceNumber(), retransmit.getAcknowledgemntNumber(),
                                "snd/RXT", "D", calculateRTTWithNoChange());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //in order to not return before the threads finish execution, we want to have a loop that waits for the
        //same exit condition
        while (true) {
            //same exit condition as both threads
            if ((count <= 1 && window.size() == 0)) {
                break;
            }
            //otherwise sleep for 1s
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The termination procedure will be a 4-way closure, initially we send our fin to the sender side to
     * inform intent of closure, after we wait for an ACK in a while loop from the receiver and then wait
     * for the FIN from the receiver side. after we send back and ACK for the FIN.
     */
    private void terminate() {
        //send out the FIN with no ACK and dont mind the silly DUP flag
        FIN = true;
        DUP = false;
        ACK = false;
        //create the header and empty payload for our packet
        header = new STPPacketHeader(0, sequenceNumber, 1, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        //send the FIN packet to the receiver
        sendPacket(packet);
        //write the FIN to the log file
        logWrite(0, sequenceNumber - (MSS - finalPacketSize), 1, "snd",
                "F", calculateRTTWithNoChange());
        //now wait for the ACK from the receiver side
        while (true) {
            //set a timeout of 10ms waiting for the ack, otherwise we keep re-sending our FIN
            //one timeout re-transmit exact same packet
            try {
                //set timeout of 10ms and attempt to receive data from the socket
                socket.setSoTimeout(10);
                socket.receive(dataIn);
            }
            //on timeout we want to re-transmit our FIN to the receiver
            catch (SocketTimeoutException e1) {
                //create exact same packet + send it to the receiver
                FIN = true;
                DUP = false;
                ACK = false;
                header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
                packet = new STPPacket(header, new byte[0]);
                sendPacket(packet);
                //print resent packet to the logfile
                logWrite(0, sequenceNumber - (MSS - finalPacketSize) + 1, 1,
                        "snd", "F", calculateRTTWithNoChange());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //otherwise when we receive our ACK from our fin
            r = new ReadablePacket(dataIn);
            if (r.isFIN() && r.isACK()) {
                //we want to write to our log file, and break
                logWrite(0, r.getSequenceNumber(), sequenceNumber - (MSS - finalPacketSize) + 1,
                        "rcv", "A", calculateRTTWithNoChange());
                break;
            }
        }
        //now we want to remove our timeout for the rest of process
        try {
            socket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //now wait for the FIN from the receiver side
        while (true) {
            //attempt to receive the FIN from the receiver side as the 3rd stage in our 4 way closure
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //turn the received packet in a readable form
            r = new ReadablePacket(dataIn);
            //if the packet received is a FIN from the receiver
            if (r.isFIN() && !r.isACK()) {
                //we want to write to log file and break
                logWrite(0, r.getSequenceNumber(), sequenceNumber - (MSS - finalPacketSize) + 1,
                        "rcv", "F", calculateRTTWithNoChange());
                break;
            }
        }
        //now finally we want to send back our last ACK packet
        //create our header + empty payload for our packet to send to the receiver
        FIN = true;
        ACK = true;
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        //send the packet to the receiver
        sendPacket(packet);
        //write to the log file our final ack
        logWrite(0, sequenceNumber - (MSS - finalPacketSize) + 1, 2, "snd",
                "A", calculateRTTWithNoChange());
    }

    /**
     * This method will be used to check if the file requested exists within the directory
     *
     * @param fileName the file we are checking
     * @return true if the file exists in the current directory, false if not
     */
    private boolean containsFile(String fileName) {
        //scan through directory
        for (File file : allFiles)
            //if the current file has the same name as the one requested we return true
            if (file.getName().equals(fileName))
                return true;
        //otherwise return false if no files match
        return false;
    }

    /**
     * This method will be a simple one's complement checksum, it will take the current payload
     * sum all the bytes within the checksum, and finally return the one's complement of that sum
     *
     * @param payload the payload (byte array) we are transmitting
     * @return the checksum value of our payload
     */
    private int checksum(byte[] payload) {
        //start a running count
        int sum = 0;
        //for each byte in the payload array
        for (byte byteData : payload) {
            //cumulatively add to the sum
            sum += (int) byteData;
        }
        //take the one's complement of that sum
        sum = ~sum;
        //return the one's complement of our sum
        return sum;
    }

    /**
     * This method will be a safe send where it will deliver the packet to the receiver with no
     * PLD effect. (this will not simulate any errors in sending procedure)
     *
     * @param p the packet we are sending to the receiver
     */
    private void sendPacket(STPPacket p) {
        //we want to add 1 to the number of packets transferred
        PLD.addPacketsTransferred();
        //set the datagram for output to be the formatted packet we are sending
        dataOut = p.getPacket();
        //set the address for the receiver that we are sending to
        dataOut.setAddress(receiverIP);
        dataOut.setPort(receiverPort);
        //send the packet through our UDP socket
        try {
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will be used to send a packet through a channel whilst simulating unreliability in the forms of
     * corruption, delays, drop, duplicates and re-ordering. we will use our PLD module to simulate the unreliability
     * this will simply emulate sending a packet through an unreliable channel
     *
     * @param p the packet we are attempting to send through an unreliable channel
     */
    private void PLDSend(STPPacket p) {
        //add to our PLD statistic counter
        PLD.addPLDTransferred();
        PLD.addPacketsTransferred();
        //now we want to create a readable version of our packet for PLD parsing
        ReadablePacket read = new ReadablePacket(p.getPacket());
        //create a temporary value for our size to be 0 in order to workout the size of the payload
        //in the packet
        int size = 0;
        //if this is the last packet in the list
        if (read.getSequenceNumber() == filePackets.get(filePackets.size() - 1).getSequenceNumber()) {
            //set the size to be the final packet size
            size = finalPacketSize;
        } else {
            //otherwise we use the MSS
            size = MSS;
        }
        //set a flag to see if any PLD module has been checked
        boolean flag = false;
        //if we have a case of drop
        if (rand.nextDouble() < PLD.getpDrop()) {
            //we want some output from the sender to see feedback and activity
            System.out.println("Drop - " + count + "window index" + windowIndex + "window size - " + window.size());
            //write the drop to the lof file
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "drop", "D",
                    calculateRTTWithNoChange());
            //add 1 to the number of packets dropped and return without sending (simulating packet drop)
            PLD.addPacketDropped();
            return;
        }
        //otherwise if we have not dropped the packet, and we have the case of duplicate
        else if (rand.nextDouble() < PLD.getpDuplicate() && !flag) {
            //we want to add 1 to number of packets duplicated
            PLD.addPacketDuplicated();
            //we want some output from the sender to see feedback and activity
            System.out.println("Duplicate " + count + "window index" + windowIndex + "window size - " + window.size());
            //write the duplicate to the log file
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/dup", "D",
                    calculateRTTWithNoChange());
            //add 1 to the number of packets transferred aswell since we are transmitting another packet
            PLD.addPLDTransferred();
            //send the packet through our safe method to emulate duplication
            sendPacket(p);
            //set our flag to be true since we dont want to perform any other PLD errors
            flag = true;
        }
        //if we have a case of corruption and we didn't get any other PLD checks above
        else if (rand.nextDouble() < PLD.getpCorrupt() && !flag) {
            //add 1 to the number of packets corrupted
            PLD.addPacketCorrupted();
            //we want some output from the sender to see feedback and activity
            System.out.println("corrupt " + count + "window index" + windowIndex + "window size - " + window.size());
            //write the corruption to the log file
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/corr",
                    "D", calculateRTTWithNoChange());
            //now we want to create a new payload to send that is corrupted by fliyying a bit
            byte[] copy = new byte[p.getPayload().length];
            //first we want to copy the payload
            for (int i = 0; i < p.getPayload().length; i++) {
                copy[i] = p.getPayload()[i];
            }
            //add 1 to the first payload byte
            copy[0]++;
            //now we want to make the packet we are sending to be the corrupted packet
            p = new STPPacket(p.getHeader(), copy);
            //set flag true that we have processed through PLD
            flag = true;
        }
        //if we have re-ordering and still havent been processed by the PLD
        else if (rand.nextDouble() < PLD.getpOrder() && !flag && !reOrder) {
            //we want to add 1 to number of packets re-ordered
            PLD.addPacketReOrdered();
            //we want some output from the sender to see feedback and activity
            System.out.println("reorder " + count + "window index" + windowIndex + "window size - " + window.size());
            //output the re-ordering to our log file
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/rord",
                    "D", calculateRTTWithNoChange());
            //say we have a packet being re-ordered so we cannot re-order till our re-ordered packet is delivered
            reOrder = true;
            //now we want to make a break point where we transmit this packet to resume order to be the
            //max-reorder size passed through
            windowreOrder = windowIndex + PLD.getMaxOrder();
            //set the packet we are re-ordering to the current packet to transmit later and return
            reOrderPacket = p;
            return;
        }
        //finally if we have delayed packet, AND we havent gone through any of the other checks
        else if (rand.nextDouble() < PLD.getpDelay() && !flag) {
            //add 1 to the numebr of delayed packets
            PLD.addPacketDelayed();
            //we want some output from the sender to see feedback and activity
            System.out.println("delay " + count + "window index" + windowIndex + "window size - " + window.size());
            //write to the log file our delay
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/dely",
                    "D", calculateRTTWithNoChange());
            try {
                //wait for a time between 0-max delay before we resume sending
                Thread.sleep(rand.nextInt(PLD.getMaxDelay()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //now finally we can deliver our packet  through the same method as sendPacket
        //set the datagram data to be our formatted packet
        dataOut = p.getPacket();
        //set the destination for our packet to be the receiver
        dataOut.setAddress(receiverIP);
        dataOut.setPort(receiverPort);
        //send the packet through our UDP socket
        try {
            socket.send(dataOut);
            //write to the log file we have sent a packet
            logWrite(size, read.getSequenceNumber(), ackNumber, "snd", "D", calculateRTTWithNoChange());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log write.
     *
     * @param length         the length
     * @param sequenceNumber the sequence number
     * @param ackNumber      the ack number
     * @param sndOrReceive   the snd or receive
     * @param status         the status
     * @param timeOut        the time out
     */
    private void logWrite(int length, int sequenceNumber, int ackNumber,
                          String sndOrReceive, String status, int timeOut) {
        float timePassed = timer.timePassed() / 1000;
        try {
            timeOut = socket.getSoTimeout();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s\n", sndOrReceive
                , timePassed, status, sequenceNumber, length, ackNumber);
        try {
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Packet index int.
     *
     * @param r the r
     * @return the int
     */
    public int packetIndex(ReadablePacket r) {
        return filePackets.indexOf(r);
    }

    /**
     * Gets nack packet.
     *
     * @param r the r
     * @return the nack packet
     */
    public ReadablePacket getNACKPacket(ReadablePacket r) {
        for (ReadablePacket read : filePackets) {
            if (read.getSequenceNumber() == r.getAcknowledgemntNumber()) {
                return read;
            }
        }
        return null;
    }

    /**
     * Clear window before last ack.
     *
     * @param ack the ack
     */
    public void clearWindowBeforeLastAck(int ack) {
        for (ReadablePacket r : window) {
            if (r.getAcknowledgemntNumber() < ack + MSS) {
                //logWrite(0,1,r.getSequenceNumber(),"rcv","A");
                window.remove(r);
            }
        }
    }

    /**
     * Calculate rtt int.
     *
     * @return the int
     */
    private int calculateRTT() {
        int tmpEstimatedRTT = estimatedRTT;
        int tmpDevRTT = devRTT;
        estimatedRTT = (int) ((1 - 0.25) * estimatedRTT);
        estimatedRTT += (int) (0.25) * ((System.currentTimeMillis() - sendTime));
        devRTT = (int) ((1 - 0.25) * devRTT);
        int subtract = (int) ((System.currentTimeMillis() - sendTime));
        devRTT += (int) (0.25 * (Math.abs(subtract - estimatedRTT)));
        if ((estimatedRTT + (int) this.gamma * devRTT) > 60000) {
            estimatedRTT = tmpEstimatedRTT;
            devRTT = tmpDevRTT;
            return 59999;
        }
        if ((estimatedRTT + (int) this.gamma * devRTT) <= 0) {
            //reset estimated and devRTT and set sensible 50ms timeout
            estimatedRTT = 50;
            devRTT = 25;
            return 10;
        }
        return (estimatedRTT + (int) this.gamma * devRTT);
    }

    /**
     * Fast retransmit.
     */
    private void fastRetransmit() {
        PLD.addFastRetransmissions();
        windowIndex -= windowSize;
        if (windowIndex <= 0) {
            windowIndex = 0;
            count = filePackets.size();
            return;
        }
        count += windowSize;
    }

    /**
     * Calculate rtt with no change int.
     *
     * @return the int
     */
    private int calculateRTTWithNoChange() {
        int tmpEstimatedRTT = estimatedRTT;
        tmpEstimatedRTT = (int) ((1 - 0.25) * estimatedRTT);
        tmpEstimatedRTT += (int) (0.25) * ((System.currentTimeMillis() - sendTime));
        int tmpDevRTT = devRTT;
        tmpDevRTT = (int) ((1 - 0.25) * devRTT);
        int subtract = (int) ((System.currentTimeMillis() - sendTime));
        tmpDevRTT += (int) (0.25 * (Math.abs(subtract - estimatedRTT)));
        if ((tmpEstimatedRTT + (int) this.gamma * tmpDevRTT) > 60000)
            return 59999;
        if ((tmpEstimatedRTT + (int) this.gamma * tmpDevRTT) <= 0)
            return 10;
        return (tmpEstimatedRTT + (int) this.gamma * tmpDevRTT);
    }

    /**
     * Finish log file.
     */
    private void finishLogFile() {
        String s = "--------------------------------------------------------------------------\n";
        try {
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Size of the file (in Bytes)", sequenceNumber - (MSS - finalPacketSize));
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Segments transmitted (including drop & RXT)", PLD.getPacketsTransferred());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Segments handled by PLD", PLD.getPLDTransferredPackets());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Segments dropped", PLD.getPacketsDropped());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Segments Corrupted", PLD.getPacketsCorrupted());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Segments Re-ordered", PLD.getPacketsReordered());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Segments Duplicated", PLD.getPacketsDuplicated());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Segments Delayed", PLD.getPacketsDelayed());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of Retransmissions due to TIMEOUT", PLD.getPacketsTimedOut());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of FAST RETRANSMISSION", PLD.getFastRetransmissions());
            logFile.write(s);
            logFile.flush();
            s = String.format("%-50s %-20s\n", "Number of  DUP ACKS received", PLD.getDuplicateACKS());
            logFile.write(s);
            logFile.flush();
            s = "--------------------------------------------------------------------------\n";
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
