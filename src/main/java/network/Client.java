package network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pojos.Doctor;
import pojos.Patient;
import pojos.User;
import ui.windows.Application;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    private Application appMain;
    private Gson gson = new Gson();

    public Client(String ip, int port, Application appMain) {
        this.appMain = appMain;
        try {
            //socket = new Socket("localhost", 9009);
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            sendInitialMessage();
        }catch(IOException e){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }

    private void sendMessage(String message) throws IOException {
        if(message.equals("stop")){
            stopClient();
        }else if(message.equals("get_patient")){
            out.println(message);
            String received = in.readLine(); // read one line (the Patient string)
            System.out.println("Received from server: " + received);
            //requestPatient();
        }else if(message.equals("get_doctor")){
            requestDoctorInfo();
        }else if(message.contains("LOGIN")){
            out.println(message);
            String received = in.readLine(); // read one line (the Patient string)
            System.out.println("Received from server: " + received);
        }
        else {
        out.println(message);}
    }

    public void requestPatient() throws IOException {
        String message = "get_patient";
        out.println(message);

        String received = in.readLine(); // read one line (the Patient string)
        System.out.println("Received from server: " + received);
    }

    public void requestDoctorInfo() throws IOException {
        String message = "get_doctor";
        out.println(message);

        String received = in.readLine(); // read one line (the Patient string)
        System.out.println("Received from server: " + received);
    }

    public boolean requestLogin(String email, String password) throws IOException {
        String message = "LOGIN;" + email + ";" + password;
        out.println(message);

        String response = in.readLine();
        if (response == null) return false;

        if (response.startsWith("LOGIN_SUCCESS;")) {
            String userData = response.substring("LOGIN_SUCCESS;".length());
            System.out.println("Logged in as: " + userData);
            return true;
        } else if (response.startsWith("LOGIN_FAIL;")) {
            String reason = response.substring("LOGIN_FAIL;".length());
            System.out.println("Login failed: " + reason);
            return false;
        }
        return false;
    }

    public boolean login(String email, String password) throws IOException {
        //String message = "LOGIN;" + email + ";" + password;

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOGIN_REQUEST");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        // Read the response
        String responseLine = in.readLine();
        JsonObject response = gson.fromJson(responseLine, JsonObject.class);

        // Check response
        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            JsonObject userJson = response.getAsJsonObject("user");
            int id = userJson.get("id").getAsInt();
            String role = userJson.get("role").getAsString();
            System.out.println("Login successful!");
            System.out.println("User ID: " + id + ", Role: " + role);

            if(role.equals("Doctor")){
                User user = new User(id, email, password, role);
                appMain.user = user;
                Doctor doctor = Doctor.fromJason(response.getAsJsonObject("doctor"));
                System.out.println(doctor);
                appMain.doctor = doctor;
                return true;
            }else{
                return false;
            }
        } else {
            String errorMsg = response.get("message").getAsString();
            System.out.println("Login failed: " + errorMsg);
            return false;
        }
    }

    public void stopClient() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "STOP_CLIENT");
        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
        System.out.println("Sent: " + jsonMessage);
        //out.println("stop");
        releaseResources(out, socket);
    }

    public static void main(String args[]) throws IOException {
        System.out.println("Starting Client...");
        /*Socket socket = new Socket("localhost", 9009); //localhost refers to your own computer
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connection established... sending text");*/

        //"localhost", 9009
        Client client = new Client("localhost", 9009, new Application());


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
