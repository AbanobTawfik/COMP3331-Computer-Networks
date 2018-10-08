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
     * arguments passed through from the user, and will create a sender (server) that will match the behaviour passed in through
     * user input
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
        //set our maximum window size to be the floor division of the maximum window size divided by maximum segment size
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
        //now we want to initialise our log file with the name sender_log.txt" inside a directory for our files log file
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
            String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s %-15s %-15s\n", "snd/rcv", "time",
                    "type", "sequence", "payload size", "ack", "Timeout", "window size");
            //write the string to the log file
            logFile.write(s);
            //flush to allow for more writing
            logFile.flush();
            //we want to create seperator now to indicate beginning of execution now
            s = "------------------------------------------------------------------------------------------------------------------\n";
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
     * terminate the connection and finally print the statistics to the log file in a summary. at the end of this function
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

    private void prepareFile() {
        //first we want to check if the file exists within the current directory
        //if the file doesn't exist in current directory, we exit with error status and a easy to understand error message
        if (!containsFile(fileRequested)) {
            System.out.println("The file requested does not exist in this directory");
            System.exit(1);
        } else {
            //we want to create a list of ready to send packets (since they are file data we want to turn off most flags)
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
                //now we want to store all these payloads into packet
                header = new STPPacketHeader(checksum(packetPayload), sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
                sequenceNumber += MSS;
                packet = new STPPacket(header, packetPayload);
                r = new ReadablePacket(packet.getPacket());
                filePackets.add(r);
            }
        }
    }

    private void handshake() {
        SYN = true;
        header = new STPPacketHeader(0, filePackets.get(0).getSequenceNumber(), 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        logWrite(0, filePackets.get(0).getSequenceNumber(), 0, "snd", "S", (estimatedRTT + (int) (gamma) * devRTT));
        //now we want for SYN ACK back
        while (true) {
            try {
                socket.setSoTimeout(estimatedRTT + (int) (gamma) * devRTT);
                dataIn.setAddress(receiverIP);
                dataIn.setPort(receiverPort);
                socket.receive(dataIn);
                r = new ReadablePacket(dataIn);
                if (r.isSYN() && r.isACK()) {
                    ACK = true;
                    ackNumber = r.getSequenceNumber();
                    logWrite(0, r.getSequenceNumber(), 1, "rcv", "SA", (estimatedRTT + (int) (gamma) * devRTT));
                    break;
                }
            } catch (SocketTimeoutException e) {
                SYN = true;
                header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
                packet = new STPPacket(header, new byte[0]);
                sendPacket(packet);
                logWrite(0, filePackets.get(0).getSequenceNumber(), 0, "snd", "S", (estimatedRTT + (int) (gamma) * devRTT));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //end handshake with the ACK
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        logWrite(0, filePackets.get(0).getSequenceNumber() + 1, ackNumber + 1, "snd", "A", (estimatedRTT + (int) (gamma) * devRTT));
        r.display();
    }

    private void sendData() {
        count = filePackets.size() + 1;
        //sender thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if ((count == 1 && window.size() == 0) || count == 0) {
                        break;
                    }
                    if (windowIndex >= filePackets.size()) {
                        continue;
                    }
                    //if there is room inside our window we will transmit a window size from current index (based off last ACK)
                    if (window.remainingCapacity() > 0) {
                        if (reOrder && windowIndex == windowreOrder) {
                            sendPacket(reOrderPacket);
                            reOrder = false;
                        }
                        packet = new STPPacket(filePackets.get(windowIndex));
                        try {
                            window.put(filePackets.get(windowIndex));
                            count--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendTime = (int) System.currentTimeMillis();
                        windowIndex++;
                        //if we get a drop then we want to just go to next packet and not send --> drop packet
                        PLDSend(packet);
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if ((count == 1 && window.size() == 0) || count == 0) {
                        break;
                    }
                    try {
                        dataIn.setAddress(receiverIP);
                        dataIn.setPort(receiverPort);
                        socket.receive(dataIn);
                        r = new ReadablePacket(dataIn);
                        //System.out.println("ACK - " + r.isACK() + " ack number - " + r.getAcknowledgemntNumber());
                        if (r.isACK()) {
                            if (dupAcks.size() == 0) {
                                dupAcks.add(r.getAcknowledgemntNumber());
                            }
                            if (dupAcks.size() == 3) {
                                fastRetransmit();
                                dupAcks.clear();
                            }
                            if (dupAcks.size() == 1 || dupAcks.size() == 2) {
                                boolean flag = true;
                                for (Integer i : dupAcks) {
                                    if (r.getAcknowledgemntNumber() != i) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    PLD.addDuplicateACKS();
                                    dupAcks.add(r.getAcknowledgemntNumber());
                                } else {
                                    dupAcks.clear();
                                    dupAcks.add(r.getAcknowledgemntNumber());
                                }
                            }
                            for (ReadablePacket read : window) {
                                if (r.getAcknowledgemntNumber() == read.getSequenceNumber()) {
                                    estimatedRTT = (int) System.currentTimeMillis() - sendTime;
                                    socket.setSoTimeout(calculateRTT());
                                    ackNumber = r.getSequenceNumber();
                                    window.remove(read);
                                    if (dupAcks.size() > 1) {
                                        logWrite(0, ackNumber, r.getAcknowledgemntNumber(), "rcv/DA", "A", estimatedRTT);
                                    } else {
                                        logWrite(0, ackNumber, r.getAcknowledgemntNumber(), "rcv", "A", estimatedRTT);
                                    }
                                }
                            }
                            continue;
                        } else {
                            for (ReadablePacket read : window) {
                                if (r.getAcknowledgemntNumber() == read.getSequenceNumber()) {
                                    packet = new STPPacket(read);
                                    PLDSend(packet);
                                    logWrite(MSS, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/RXT", "D", calculateRTTWithNoChange());
                                    //System.out.println("time-out: Retransmission -- " + read.getSequenceNumber());
                                    break;
                                }
                            }

                            //System.out.println("time-out: NAK");

                        }
                    } catch (SocketTimeoutException e) {
                        PLD.addPacketsTimedOut();
                        ReadablePacket retransmit = window.peek();
                        if (null == retransmit)
                            continue;
                        packet = new STPPacket(retransmit);
                        PLDSend(packet);
                        logWrite(MSS, retransmit.getSequenceNumber(), retransmit.getAcknowledgemntNumber(), "tout/RXT", "D", calculateRTTWithNoChange());
                        //System.out.println("time-out: Retransmission -- " + retransmit.getSequenceNumber());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        while (true) {
            if ((count == 1 && window.size() == 0) || count == 0) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void terminate() {
        //send out the FIN
        //sequenceNumber = sequenceNumber - (MSS - finalPacketSize);
        FIN = true;
        DUP = false;
        ACK = false;
        header = new STPPacketHeader(0, sequenceNumber, 1, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        logWrite(0, sequenceNumber - (MSS - finalPacketSize), 1, "snd", "F", calculateRTTWithNoChange());
        //now wait for the FIN ACK
        while (true) {
            try {
                socket.setSoTimeout(10);
                socket.receive(dataIn);
            } catch (SocketTimeoutException e1) {
                FIN = true;
                DUP = false;
                ACK = false;
                header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                        receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
                packet = new STPPacket(header, new byte[0]);
                sendPacket(packet);
                logWrite(0, sequenceNumber - (MSS - finalPacketSize) + 1, 1, "snd", "F", calculateRTTWithNoChange());
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isFIN() && r.isACK()) {
                logWrite(0, r.getSequenceNumber(), sequenceNumber - (MSS - finalPacketSize) + 1, "rcv", "A", calculateRTTWithNoChange());
                break;
            }
        }
        try {
            socket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //now wait for the FIN
        while (true) {
            try {
                socket.receive(dataIn);
            } catch (IOException e) {
                e.printStackTrace();
            }
            r = new ReadablePacket(dataIn);
            if (r.isFIN() && !r.isACK()) {
                logWrite(0, r.getSequenceNumber(), sequenceNumber - (MSS - finalPacketSize) + 1, "rcv", "F", calculateRTTWithNoChange());
                break;
            }
        }
        //send back an FIN ACK to the client
        FIN = true;
        ACK = true;
        header = new STPPacketHeader(0, sequenceNumber, 0, IP,
                receiverIP, portNumber, receiverPort, SYN, ACK, FIN, DUP);
        packet = new STPPacket(header, new byte[0]);
        sendPacket(packet);
        logWrite(0, sequenceNumber - (MSS - finalPacketSize) + 1, 2, "snd", "A", calculateRTTWithNoChange());
    }

    private boolean containsFile(String fileName) {
        //scan through directory
        for (File file : allFiles)
            if (file.getName().equals(fileName))
                return true;
        //otherwise return false if no files match
        return false;
    }

    private int checksum(byte[] payload) {
        int sum = 0;
        for (byte byteData : payload) {
            sum += (int) byteData;
        }
        sum = ~sum;
        return sum;
    }


    private void sendPacket(STPPacket p) {
        PLD.addPacketsTransferred();
        dataOut = p.getPacket();
        dataOut.setAddress(receiverIP);
        dataOut.setPort(receiverPort);
        try {
            socket.send(dataOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void PLDSend(STPPacket p) {
        PLD.addPLDTransferred();
        PLD.addPacketsTransferred();
        ReadablePacket read = new ReadablePacket(p.getPacket());
        int size = 0;
        if (read.getSequenceNumber() == filePackets.get(filePackets.size() - 1).getSequenceNumber()) {
            size = finalPacketSize;
        } else {
            size = MSS;
        }
        boolean flag = false;
        if (rand.nextDouble() < PLD.getpDrop()) {
//            System.out.println("DROPPED " + count + "window index" + windowIndex + "window size - " + window.size());
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "drop", "D", calculateRTTWithNoChange());
            PLD.addPacketDropped();
            //logWrite(dataOut.getLength() - HeaderValues.PAYLOAD_POSITION_IN_HEADER, filePackets.get(windowIndex).getSequenceNumber(), ackNumber, "drop", "D", calculateRTTWithNoChange());
            return;
        } else if (rand.nextDouble() < PLD.getpDuplicate() && !flag) {
            PLD.addPacketDuplicated();
//            System.out.println("DUPLICATED " + count + "window index" + windowIndex + "window size - " + window.size());
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/dup", "D", calculateRTTWithNoChange());
            PLD.addPLDTransferred();
            sendPacket(p);
            flag = true;
        } else if (rand.nextDouble() < PLD.getpCorrupt() && !flag) {
            PLD.addPacketCorrupted();
//            System.out.println("corrupted " + count + "window index" + windowIndex + "window size - " + window.size());
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/corr", "D", calculateRTTWithNoChange());
            byte[] copy = new byte[p.getPayload().length];
            for (int i = 0; i < p.getPayload().length; i++) {
                copy[i] = p.getPayload()[i];
            }
            copy[0]++;
            p = new STPPacket(p.getHeader(), copy);
            flag = true;
        } else if (rand.nextDouble() < PLD.getpOrder() && !flag && !reOrder) {
            PLD.addPacketReOrdered();
//            System.out.println("REORDERED " + count + "window index" + windowIndex + "window size - " + window.size());
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/rord", "D", calculateRTTWithNoChange());
            //say we have a packet being re-ordered
            reOrder = true;
            windowreOrder = windowIndex + PLD.getMaxOrder();
            reOrderPacket = p;
            return;

        } else if (rand.nextDouble() < PLD.getpDelay() && !flag) {
            PLD.addPacketDelayed();
//            System.out.println("DELAYED " + count + "window index" + windowIndex + "window size - " + window.size());
            logWrite(size, read.getSequenceNumber(), read.getAcknowledgemntNumber(), "snd/dely", "D", calculateRTTWithNoChange());
            try {
                Thread.sleep(rand.nextInt(PLD.getMaxDelay()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        dataOut = p.getPacket();
        dataOut.setAddress(receiverIP);
        dataOut.setPort(receiverPort);
        try {
            socket.send(dataOut);
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
    public void logWrite(int length, int sequenceNumber, int ackNumber, String sndOrReceive, String status, int timeOut) {
        float timePassed = timer.timePassed() / 1000;
        String s = String.format("%-15s %-10s %-10s %-15s %-15s %-15s %-15s %-15s\n", sndOrReceive
                , timePassed, status, sequenceNumber, length, ackNumber, timeOut, window.size());
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
    public int calculateRTT() {
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
        return (estimatedRTT + (int) this.gamma * devRTT);
    }

    /**
     * Fast retransmit.
     */
    public void fastRetransmit() {
        PLD.addFastRetransmissions();
        windowIndex -= windowSize;
        count += windowSize;
    }

    /**
     * Calculate rtt with no change int.
     *
     * @return the int
     */
    public int calculateRTTWithNoChange() {
        int tmpEstimatedRTT = estimatedRTT;
        tmpEstimatedRTT = (int) ((1 - 0.25) * estimatedRTT);
        tmpEstimatedRTT += (int) (0.25) * ((System.currentTimeMillis() - sendTime));
        int tmpDevRTT = devRTT;
        tmpDevRTT = (int) ((1 - 0.25) * devRTT);
        int subtract = (int) ((System.currentTimeMillis() - sendTime));
        tmpDevRTT += (int) (0.25 * (Math.abs(subtract - estimatedRTT)));
        return (tmpEstimatedRTT + (int) this.gamma * tmpDevRTT);
    }

    /**
     * Finish log file.
     */
    public void finishLogFile() {
        String s = "------------------------------------------------------------------------------------------------------------------\n";
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
            s = "------------------------------------------------------------------------------------------------------------------\n";
            logFile.write(s);
            logFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
