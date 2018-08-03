import java.net.*;
import java.util.*;

public class PingClient {
    private static final int REQUEST_TIME_OUT = 1000;  // milliseconds

    public static void main(String[] args) throws Exception {
        // Get command line argument.
        if (args.length != 2) {
            System.out.println("Required arguments: (Host) + (Port)");
            return;
        }
        //the server we are pinging
        InetAddress server = InetAddress.getByName(args[0]);
        // Create a datagram socket for receiving and sending UDP packets
        // through the port specified on the command line.
        DatagramSocket port = new DatagramSocket();
        port.setSoTimeout(REQUEST_TIME_OUT);
        //initalising values for end analysis
        long averageRTT = 0;
        long maximumRTT = 0;
        long minimumRTT = 5000;
        int validRTT = 0;
        for (int i = 0; i < 10; i++) {
            //get the current time for  time-stamp-start
            Date time = new Date();
            String s = time.toString();
            long timeStampStart = time.getTime();
            //created the message for the ping
            String message = "PING " + i + " " + s + "\r\n";
            //this is the packet used in the ping request containing the message.
            DatagramPacket pingRequest = new DatagramPacket(message.getBytes(), message.length(), server, Integer.parseInt(args[1]));
            //now we want to send the ping request to the port
            port.send(pingRequest);
            //now we want to receive the response from the server.
            DatagramPacket serverReply = new DatagramPacket(new byte[1024], 1024);
            //to setup for timeout we want to use a try catch, where we check if there is a response within 1s, else catch the exception
            try {
                //if there is a response from the server within 1s since timeout at 1s will trigger the exception
                port.receive(serverReply);
                //use the current time after message received for time-stamp-end
                Date time2 = new Date();
                //calculate the RTT from this
                long timeStampEnd = time2.getTime();
                //the round trip time is the final time subtracted from the initial time
                long RTT = timeStampEnd - timeStampStart;
                //display the ping message
                System.out.println("ping to " + args[0] + ", seq = " + i + ", rtt = " + RTT + " ms");
                averageRTT += RTT;
                validRTT++;
                //if the RTT from this iteration is larger than the maximum update maximum
                if (RTT > maximumRTT)
                    maximumRTT = RTT;
                //if the RTT from this iteration is smaller than the minimum update minimum
                if (RTT < minimumRTT)
                    minimumRTT = RTT;
                //program waits for 1 second - the amount of time for the RTT to simulate 1 second delay
                Thread.sleep(1000 - RTT);
            } catch (Exception e) {
                //time out exception
                System.out.println("ping to " + args[0] + ", seq = " + i + ", rtt = time out");
                //1 second delay between the ping
                Thread.sleep(1000);
            }
        }
        //print the average minimum and maximum RTT
        System.out.println("Average RTT = " + (averageRTT / validRTT) + " ms" + ", Minimum RTT = "
                + minimumRTT + " ms" + ", Maximum RTT = " + maximumRTT + " ms.");
    }
}
