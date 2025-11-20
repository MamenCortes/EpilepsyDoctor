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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    private Application appMain;
    private Gson gson = new Gson();
    private volatile Boolean running = false;
    //Estructura diseñada para comunicar threads entre sí de manera segura y sincronizada
    //permite que un thread meta mensajes en la cola (con put())
    //Y que otro thread los reciba (con take() o poll())
    //si no hay mensajes, take() se bloquea automáticamente, sin consumir CPU
    private BlockingQueue<JsonObject> responseQueue = new LinkedBlockingQueue<>();

    public Client(Application appMain) {
        this.appMain = appMain;
    }

    public Boolean connect(String ip, int port) {

        try {
            //socket = new Socket("localhost", 9009);
            socket = createSocket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            running = true;
            sendInitialMessage();
            startListener();
            return true;
        }catch(IOException e){
            //if(!socket.isConnected()){appMain.onServerDisconnected();}
            return false;
        }
    }

    /// Start a thread that listens for messages from the server
    /// If the server sends STOP_CLIENT, the connection is closed and also the app???
    public void startListener() {
        System.out.println("Listening for messages...");
        Thread listener = new Thread(() -> {
            try {
                String line;
                while (((line = in.readLine()) != null)&&running) {
                    //System.out.println("New message: " + line);
                    JsonObject json = gson.fromJson(line, JsonObject.class);

                    String type = json.get("type").getAsString();

                    if (type.equals("STOP_CLIENT")) {
                        System.out.println("Server requested shutdown");
                        stopClient(false);
                        break;
                    }

                    try {
                        responseQueue.put(json);
                    }catch (InterruptedException e){
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("type", "LOGIN_REQUEST_RESPONSE");
                        jsonObject.addProperty("status", "ERROR");
                        jsonObject.addProperty("message", "Error while processing login request");
                        responseQueue.add(jsonObject);
                    }

                }
            } catch (IOException ex) {
                System.out.println("Server connection closed: " + ex.getMessage());
                //In case the connection is closed without the server asking for it first
                stopClient(false);
            }
        });

        //listener.setDaemon(true); // client ends even if thread is running
        listener.start();
    }

    protected Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }

    public Boolean isConnected(){
        if(socket == null){
            return false;
        }else {
            return socket.isConnected();
        }
    }

    public void stopClient(boolean initiatedByClient) {
        if (initiatedByClient && socket != null && !socket.isClosed()) {
            // Only send STOP_CLIENT if CLIENT requested shutdown
            Map<String, Object> message = new HashMap<>();
            message.put("type", "STOP_CLIENT");
            String jsonMessage = gson.toJson(message);
            out.println(jsonMessage);
            System.out.println("Sent (client-initiated): " + jsonMessage);
        }

        System.out.println("Stopping client...");
        running = false;

        // Notify UI ONLY if the server disconnected
        if (!initiatedByClient) {
            appMain.onServerDisconnected();
        }

        releaseResources(out, in, socket);
    }

    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }

    public void login(String email, String password) throws IOException, InterruptedException {
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
        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("LOGIN_RESPONSE"));

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
                do {
                    response = responseQueue.take();
                } while (!response.get("type").getAsString().equals("REQUEST_DOCTOR_BY_EMAIL_RESPONSE"));
                System.out.println(response);
                // Check response
                status = response.get("status").getAsString();
                if (!status.equals("SUCCESS")) {
                    throw new LogInError(response.get("message").getAsString());
                }
                Doctor doctor = Doctor.fromJason(response.getAsJsonObject("doctor"));
                System.out.println(doctor);
                appMain.doctor = doctor;
            }else{
                throw new LogInError(response.get("message").getAsString());
            }
        } else {
            throw new LogInError(response.get("message").getAsString());
        }
    }

    public List<Patient> getPatientsFromDoctor(int doctor_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("doctor_id", doctor_id);
        data.put("user_id", appMain.user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_PATIENTS_FROM_DOCTOR");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        out.println(jsonMessage); // send JSON message

        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE"));
        List<Patient> patients = new ArrayList<>();

        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            JsonArray data_response = response.getAsJsonArray("patients");

            for (JsonElement element : data_response) {
                patients.add(Patient.fromJason(element.getAsJsonObject()));
            }

            System.out.println("Received " + patients.size() + " patients.");
        }else {
            throw new ClientServerCommunicationError(response.get("message").getAsString());
        }
        return patients;
    }

    public void saveComments(Integer patient_id, Signal signal) throws IOException, InterruptedException {
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
        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("SAVE_COMMENTS_SIGNAL_RESPONSE"));
        String status = response.get("status").getAsString();
        System.out.println(response.get("status").getAsString());
        if (!status.equals("SUCCESS")) {
            throw new ClientServerCommunicationError(response.get("message").getAsString());
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

    private static void releaseResources(PrintWriter printWriter, BufferedReader reader,  Socket socket) {
        if(printWriter!= null)printWriter.close();
        try{
            if(reader!= null)reader.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
        try {
            socket.close();
            System.out.println("Socket closed successfully");

        } catch (IOException ex) {
            System.out.println("Error closing socket"+ex.getMessage());
        }
    }

}
