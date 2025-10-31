package network;

import pojos.Patient;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    Socket socket;
    PrintWriter printWriter;
    BufferedReader bufferedReader;

    public Client( String ip, int port) {
        try {
            //socket = new Socket("localhost", 9009);
            socket = new Socket(ip, port);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            sendInitialMessage();
        }catch(IOException e){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        printWriter.println("Hi! I'm a new client!\n");
    }

    private void sendMessage(String message) throws IOException {
        if(message.equals("stop")){
            stopClient();
        }else if(message.equals("get_patient")){
            printWriter.println(message);

            String received = bufferedReader.readLine(); // read one line (the Patient string)
            System.out.println("Received from server: " + received);
            //requestPatient();
        }else if(message.equals("get_doctor")){
            requestDoctorInfo();
        }else{
        printWriter.println(message);}
    }

    public void requestPatient() throws IOException {
        String message = "get_patient";
        printWriter.println(message);

        String received = bufferedReader.readLine(); // read one line (the Patient string)
        System.out.println("Received from server: " + received);
    }

    public void requestDoctorInfo() throws IOException {
        String message = "get_doctor";
        printWriter.println(message);

        String received = bufferedReader.readLine(); // read one line (the Patient string)
        System.out.println("Received from server: " + received);
    }

    public void stopClient() {
        printWriter.println("stop");
        releaseResources(printWriter, socket);
    }

    public static void main(String args[]) throws IOException {
        System.out.println("Starting Client...");
        /*Socket socket = new Socket("localhost", 9009); //localhost refers to your own computer
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connection established... sending text");*/

        //"localhost", 9009
        Client client = new Client("localhost", 9009);


        Scanner scanner = new Scanner(System.in); // create a Scanner for console input
        System.out.print("Write whatever message you want to send");
        System.out.println("Write \"stop\" to stop the server");
        String name; // read a full line of text
        while (true) {
            name = scanner.nextLine();
            //And send it to the server
            client.sendMessage(name);
            if(name.equals("stop")){
                scanner.close();
                break;
            }
        }
        //System.out.println("Sending stop command");
        //printWriter.println("Stop");
        //releaseResources(printWriter, socket);
        //System.exit(0);
    }

    private static void releaseResources(PrintWriter printWriter, Socket socket) {
        printWriter.close();

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }
    }

}
