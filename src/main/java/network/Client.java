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

    public Client(){
        try {
            socket = new Socket("localhost", 9009);
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
        printWriter.println("Header File\n\n");
        printWriter.println("Tell me, what is it you plan");
        printWriter.println("to do with your one wild");
        printWriter.println("and precious life?");
        printWriter.println("Mary Oliver");
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

    public static void main(String args[]) throws IOException {
        System.out.println("Starting Client...");
        Socket socket = new Socket("localhost", 9009); //localhost refers to your own computer
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connection established... sending text");
        printWriter.println("Header File\n\n");
        printWriter.println("Tell me, what is it you plan");
        printWriter.println("to do with your one wild");
        printWriter.println("and precious life?");
        printWriter.println("Mary Oliver");


        Scanner scanner = new Scanner(System.in); // create a Scanner for console input
        System.out.print("Write whatever message you want to send");
        System.out.println("Write \"stop\" to stop the server");
        String name; // read a full line of text
        while (true) {
            name = scanner.nextLine();
            //And send it to the server
            printWriter.println(name);
            if(name.equals("stop")){
                releaseResources(printWriter, socket, scanner);
                System.exit(0);
            }
        }
        //System.out.println("Sending stop command");
        //printWriter.println("Stop");
        //releaseResources(printWriter, socket);
        //System.exit(0);
    }

    public void stopClient(){
        printWriter.println("stop");
        releaseResources(printWriter, socket);
    }

    private static void releaseResources(PrintWriter printWriter, Socket socket, Scanner scanner) {
        printWriter.close();
        scanner.close();

        try {
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }
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
