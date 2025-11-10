package network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pojos.Doctor;
import pojos.Patient;
import pojos.Signal;
import pojos.User;
import ui.windows.Application;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    private Application appMain;
    private Gson gson = new Gson();

    public Client(Application appMain) {
        this.appMain = appMain;
    }

    public Boolean connect(String ip, int port) {

        try {
            //socket = new Socket("localhost", 9009);
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            sendInitialMessage();
            return true;
        }catch(IOException e){
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }

    }

    public Boolean isConnected(){
        if(socket == null){
            return false;
        }else {
            return socket.isConnected();
        }
    }

    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }

    public boolean login(String email, String password) throws IOException {
        //String message = "LOGIN;" + email + ";" + password;

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("access_permits", "Doctor");

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOGIN_REQUEST");
        message.put("data", data);

        /*{type: LOGIN_REQUEST
        * data: {email: email@.., password: passr}*/

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message
        System.out.println(jsonMessage);
        // Read the response
        String responseLine = in.readLine();
        JsonObject response = gson.fromJson(responseLine, JsonObject.class);

        // Check response
        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            JsonObject userJson = response.getAsJsonObject("data");
            int id = userJson.get("id").getAsInt();
            String role = userJson.get("role").getAsString();
            System.out.println("Login successful!");
            System.out.println("User ID: " + id + ", Role: " + role);

            if(role.equals("Doctor")){
                User user = new User(id, email, password, role);
                appMain.user = user;

                //Request doctor data
                message.clear();
                data.clear();
                message.put("type", "REQUEST_DOCTOR_BY_EMAIL");
                data.put("user_id", user.getId());
                data.put("email", user.getEmail());
                message.put("data", data);

                jsonMessage = gson.toJson(message);
                out.println(jsonMessage); // send JSON message

                // Read the response
                responseLine = in.readLine();
                response = gson.fromJson(responseLine, JsonObject.class);
                System.out.println(responseLine);
                // Check response
                status = response.get("status").getAsString();
                if (status.equals("SUCCESS")) {
                    Doctor doctor = Doctor.fromJason(response.getAsJsonObject("doctor"));
                    System.out.println(doctor);
                    appMain.doctor = doctor;
                    return true;
                }
                return false;
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

    public List<Patient> getPatientsFromDoctor(int doctor_id) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("doctor_id", doctor_id);
        data.put("user_id", appMain.user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_PATIENTS_FROM_DOCTOR");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        String line = in.readLine();
        JsonObject response = gson.fromJson(line, JsonObject.class);
        List<Patient> patients = new ArrayList<>();

        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            JsonArray data_response = response.getAsJsonArray("patients");

            for (JsonElement element : data_response) {
                patients.add(Patient.fromJason(element.getAsJsonObject()));
            }

            System.out.println("Received " + patients.size() + " patients.");
        }
        return patients;
    }

    public void saveComments(Integer patient_id, Signal signal) throws IOException {
        System.out.println("Saving comments for patient " + patient_id);
        Map<String, Object> data = new HashMap<>();
        data.put("patient_id", patient_id);
        data.put("signal_id", signal.getId());
        data.put("comments", signal.getComments());
        data.put("user_id", appMain.user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "SAVE_COMMENTS_SIGNAL");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage);
        System.out.println("Sent: " + jsonMessage);

        //wait for response
        String line = in.readLine();
        JsonObject response = gson.fromJson(line, JsonObject.class);
        String status = response.get("status").getAsString();
        System.out.println(response.get("status").getAsString());
        if (!status.equals("SUCCESS")) {
            throw new IOException();
        }
    }

    public static void main(String args[]) throws IOException {
        System.out.println("Starting Client...");
        /*Socket socket = new Socket("localhost", 9009); //localhost refers to your own computer
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connection established... sending text");*/

        //"localhost", 9009
        //Client client = new Client("localhost", 9009, new Application());
        Client client = new Client(new Application());


        Scanner scanner = new Scanner(System.in); // create a Scanner for console input
        System.out.print("Write whatever message you want to send");
        System.out.println("Write \"stop\" to stop the server");
        String name; // read a full line of text
        while (true) {
            name = scanner.nextLine();
            //And send it to the server
            //client.sendMessage(name);
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
