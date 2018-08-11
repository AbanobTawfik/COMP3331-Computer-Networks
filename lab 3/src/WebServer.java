import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.*;

//source for sending image through sockets
//https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
public class WebServer {
    //the outputstream for the client who is connecting to website
    private static DataOutputStream output;
    //reading all files and stored them in a list for requests
    private static File folder = new File(System.getProperty("user.dir"));
    private static File[] allfiles = folder.listFiles();
    //allows us to read our http request
    private static BufferedReader http;
    //client + server sockets
    private static Socket request;
    private static ServerSocket webServer;

    public static void main(String args[]) throws IOException {
//      bootstrapping so we can access from a non static context
        WebServer w = new WebServer();
//      if the number of arguements is not 1 print error message
        if (args.length != 1)
            System.out.println("Try java WebServer Port");
        int port = Integer.parseInt(args[0]);
        //try to establish a server connected to a port
        try {
            webServer = new ServerSocket(port);
            //if io exception i.e port number doesnt work catch the exception
        } catch (IOException e) {
            System.out.println("Could not listen on that port number try a different one");
        }
        //now that our server is connected to a port we want it continually processing HTTP requests
        while (true) {
            //now we want to check for requests
            //try to accept any incoming requests
            try {
                request = webServer.accept();
                //if request could not be established return error message
            } catch (IOException e) {
                System.out.println("could not establish connection");
                continue;
            }
            //now we want to read the first line to proccess request and check if it was a get request
            String line = null;
            try {
                http = new BufferedReader(new InputStreamReader(request.getInputStream()));
                line = http.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if(null == line)
                continue;
            //if there is no get request we can ignore and wait for next request
            if (!line.contains("GET")) {
                System.out.println("Only get requests thank you sir");
                continue;
            }
            //otherwise now we can extract the name of the file requested
            String requestedFile = line.split(" ")[1].split("/")[1];
            //now we are checking if the file exists in the directory
            if (w.containsFile(requestedFile)) {
                //if file exists in directory we will handle the request
                w.handle(requestedFile);
            } else {
                //otherwise we will send error404
                w.error404();
            }
            //close connection
            try {
                request.close();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * This function will check if a file exists in the currently directory
     *
     * @param fileName the file name we are checking for in the directory
     * @return true if it exists, false otherwise
     */
    private boolean containsFile(String fileName) {
        //scan through directory
        for (int i = 0; i < allfiles.length; i++)
            //if the file name matches one in the directory we return true
            if (allfiles[i].getName().equals(fileName))
                return true;
        //otherwise return false if no files match
        return false;
    }

    /**
     * This function will handle the incoming HTTP request
     * it will first check the type of file it is, and then proccess it accordingly
     *
     * @param requestedFile the name of the file being requested by client
     * @throws IOException
     */
    public void handle(String requestedFile) throws IOException {
        //now we want to set our output stream to the client socket's output stream
        //so all data goes to client
        output = new DataOutputStream(request.getOutputStream());
        //now we want to send status code 200 + ok to signify them valid request
        output.writeBytes("HTTP/1.1 200 OK \r\n");
        //we want to attatch the date to the header incase someone wants to wireshark us xD
        //now we want to find the file which we are returning to user
        File file = null;
        for (int i = 0; i < allfiles.length; i++) {
            if (allfiles[i].getName().equals(requestedFile)) {
                file = allfiles[i];
                break;
            }
        }
        //if its a html file we write code outright in text
        if (file.getName().contains(".html")) {
            //set output mode for the clients content, text + html
            output.writeBytes("Content-type: text/html\r\n\r\n");
            //now we read through the file and write it to the client socket's output stream
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                output.writeBytes(sc.nextLine());
            }
        }
        //if its a image file
        else {
            //we want to retrieve the specific image extension, we have .jpg/.png or others
            String fileExtension = file.getName().split("/")[0].split("\\.")[1];
            //now we want to set output content type to an image of extension from above
            output.writeBytes("Content-type: image/" + fileExtension + "\r\n\r\n");
            //we want to create a buffered image of our file
            BufferedImage picture = ImageIO.read(file);
            //now we want to write our image to a byte array
            ByteArrayOutputStream pictureOutput = new ByteArrayOutputStream();
            //now our image is written to the byte array we can write directly to the client socket's output stream
            ImageIO.write(picture, fileExtension, pictureOutput);
            //now we want to write our image to client
            output.write(pictureOutput.toByteArray());
        }
        //print a success message client side user received their file
        //get the current date for header information
        Date d = new Date();
        System.out.println("Successfully sent \"" + requestedFile + "\" to, " +
                request.getInetAddress().getCanonicalHostName() + ", on " + d.toString());
    }

    /**
     * In the case of file not existing in directory we are sending an error 404 message
     *
     * @throws IOException
     */
    public void error404() throws IOException {
        //output will now set stream to client's socket output stream
        output = new DataOutputStream(request.getOutputStream());
        //write the error 404 message
        output.writeBytes("HTTP/1.1 404 Not Found Error\r\n");
        output.writeBytes("Content-type: text/html\r\n\r\n");
        System.out.println("ERROR 404");
    }
}
