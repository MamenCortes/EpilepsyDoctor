package network;

import Events.ServerDisconnectedEvent;
import Events.UIEventBus;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import encryption.*;
import pojos.*;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    //TODO: eliminate references to appMain from Client
    private Gson gson = new Gson();
    private volatile Boolean running = false;
    private User user;
    //Estructura diseñada para comunicar threads entre sí de manera segura y sincronizada
    //permite que un thread meta mensajes en la cola (con put())
    //Y que otro thread los reciba (con take() o poll())
    //si no hay mensajes, take() se bloquea automáticamente, sin consumir CPU
    private BlockingQueue<JsonObject> responseQueue = new LinkedBlockingQueue<>();
    private KeyPair keyPair;
    private PublicKey serverPublicKey;
    private SecretKey AESkey;

    public Client() {
        //generates the public and private key pair
        try {
            this.keyPair = RSAKeyManager.generateKeyPair();
        }catch (Exception ex){
            System.out.println("Error generating key pair: "+ex.getMessage());
        }
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
                    JsonObject request = gson.fromJson(line, JsonObject.class);

                    String type = request.get("type").getAsString();
                    System.out.println("\nThis is the encrypted message received from the Server: "+request);

                    // Store the server's public key
                    if (type.equals("SERVER_PUBLIC_KEY")){
                        String keyEncoded = request.get("data").getAsString();
                        byte[] keyBytes = Base64.getDecoder().decode(keyEncoded);
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                        try{
                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                            PublicKey serverPublicKey = keyFactory.generatePublic(keySpec);
                            this.serverPublicKey = serverPublicKey;

                            // Generate the AES temporary key
                            this.AESkey = AESUtil.generateAESKey();
                            System.out.println("\nAES key generated");

                            // Encrypt AES key with the server's public key
                            String encryptedAESKey = RSAUtil.encrypt(Base64.getEncoder().encodeToString(AESkey.getEncoded()), serverPublicKey);

                            // Send the encrypted AES key to the server
                            JsonObject AESkeyJson = new JsonObject();
                            AESkeyJson.addProperty("type", "CLIENT_AES_KEY");
                            AESkeyJson.addProperty("data", encryptedAESKey);
                            out.println(gson.toJson(AESkeyJson));
                            System.out.println("This is the Server's shared public RSA key: "+Base64.getEncoder().encodeToString(serverPublicKey.getEncoded()));
                            System.out.println("This is the shared secret AES key: "+Base64.getEncoder().encodeToString(AESkey.getEncoded()));
                            out.flush();


                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println("Failed to fetch server public key");
                        }

                        continue;

                    }

                    // After the Client receives the Server's public key to encrypt the Secret key, reads responses
                    String typeDecrypted = type;
                    JsonObject decryptedRequest = request;
                    if (typeDecrypted.equals("ENCRYPTED")){
                        String encryptedData = request.get("data").getAsString();
                        String decryptedJson = encryptedData;
                        decryptedJson = AESUtil.decrypt(encryptedData, AESkey);
                        System.out.println("This is the decrypted json: "+decryptedJson);
                        decryptedRequest = gson.fromJson(decryptedJson,JsonObject.class);
                        typeDecrypted = decryptedRequest.get("type").getAsString();
                    }
                    System.out.println("\nThis is the decrypted type received in Client: "+typeDecrypted);


                    if (typeDecrypted.equals("STOP_CLIENT")) {
                        System.out.println("Server requested shutdown");
                        stopClient(false);
                        break;
                    }

                    try {
                        responseQueue.put(decryptedRequest);
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
            } catch (Exception e) {
                throw new RuntimeException(e);
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
            System.out.println("\nBefore encryption, STOP_CLIENT to Server: "+jsonMessage);
            sendEncrypted(jsonMessage, out, AESkey);
            System.out.println("Sent (client-initiated): " + jsonMessage);
        }

        System.out.println("Stopping client...");
        running = false;

        // Notify UI ONLY if the server disconnected
        if (!initiatedByClient) {
            //appMain.onServerDisconnected();
            System.out.println("Server disconnected");
            UIEventBus.BUS.post(new ServerDisconnectedEvent());
        }

        releaseResources(out, in, socket);
    }

    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }

    public AppData login(String email, String password) throws IOException, InterruptedException, LogInError {
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
            System.out.println("\nBefore encryption, LOGIN_REQUEST to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, AESkey);
        // Read the response
        JsonObject response;
        do {
            response = responseQueue.take();
            if(response.get("type").getAsString().equals("ENCRYPTED")){
                throw new IOException("Dencription failed");
            }
        } while (!response.get("type").getAsString().equals("LOGIN_RESPONSE"));

        AppData appData = new AppData();
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
                //appMain.user = user;
                this.user = user;
                appData.setUser(user);

                //Request doctor data
                message.clear();
                data.clear();
                message.put("type", "REQUEST_DOCTOR_BY_EMAIL");
                data.put("user_id", user.getId());
                data.put("email", user.getEmail());
                message.put("data", data);

                jsonMessage = gson.toJson(message);
                System.out.println("\nBefore encryption, REQUEST_DOCTOR_BY_EMAIL to Server: "+jsonMessage);
                sendEncrypted(jsonMessage, out, AESkey);

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
                //appMain.doctor = doctor;
                appData.setDoctor(doctor);
                return appData;
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
        if(user != null)data.put("user_id", user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_PATIENTS_FROM_DOCTOR");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, REQUEST_PATIENTS_FROM_DOCTOR to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, AESkey);

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
public List<Signal> getAllSignalsFromPatient (int patient_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("patient_id", patient_id);
        if(user != null)data.put("user_id", user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_PATIENT_SIGNALS");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, REQUEST_PATIENT_SIGNALS to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, AESkey);

        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("REQUEST_PATIENT_SIGNALS_RESPONSE"));
        List<Signal> signals = new ArrayList<>();

        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            JsonArray data_response = response.getAsJsonArray("signals");

            for (JsonElement element : data_response) {
                signals.add(Signal.fromJason(element.getAsJsonObject()));
            }

            System.out.println("Received " + signals.size() + " signals.");
        }else {
            throw new ClientServerCommunicationError(response.get("message").getAsString());
        }
        return signals;
    }
    public Signal getSignalFromId (int signal_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("signal_id", signal_id);
        if(user != null)data.put("user_id", user.getId());
        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_SIGNAL");
        message.put("data", data);
        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, REQUEST_SIGNAL to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, AESkey);

        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("REQUEST_SIGNAL_RESPONSE"));

        String status = response.get("status").getAsString();
        if (status.equals("SUCCESS")) {
            Signal signal = null;
            try {
                JsonObject meta = response.get("metadata").getAsJsonObject();
                JsonObject metadata= new JsonObject();
                metadata.addProperty("signal_id", meta.get("signal_id").getAsInt());
                metadata.addProperty("patient_id", meta.get("patient_id").getAsInt());
                metadata.addProperty("sampling_rate", meta.get("sampling_rate").getAsDouble());
                metadata.addProperty("comments", meta.get("comments").getAsString());
                metadata.addProperty("date", meta.get("date").getAsString());
                JsonObject data_response = new JsonObject();
                data_response.add("metadata", metadata);
                data_response.addProperty("dataBytes", response.get("dataBytes").getAsString());
                data_response.addProperty("filename", response.get("filename").getAsString());
                signal = Signal.fromJasonWithZip(data_response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return signal;
        }else {
            throw new ClientServerCommunicationError(response.get("message").getAsString());
        }
    }
    public void saveComments(Integer patient_id, Signal signal) throws IOException, InterruptedException {
        System.out.println("Saving comments for patient " + patient_id);
        Map<String, Object> data = new HashMap<>();
        data.put("patient_id", patient_id);
        data.put("signal_id", signal.getId());
        data.put("comments", signal.getComments());
        if(user != null)data.put("user_id", user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "SAVE_COMMENTS_SIGNAL");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, SAVE_COMMENTS_SIGNALS to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, AESkey);

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
        Client client = new Client();


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

    public void changePassword(String email, String newPassword) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("new_password", newPassword);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "CHANGE_PASSWORD_REQUEST");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        sendEncrypted(jsonMessage, out, AESkey);

        //Waits for a response of type CHANGE_PASSWORD_RESPONSE
        JsonObject response;
        do {
            response = responseQueue.take();
        } while (!response.get("type").getAsString().equals("CHANGE_PASSWORD_REQUEST_RESPONSE"));

        // Check response
        String status = response.get("status").getAsString();
        if (!status.equals("SUCCESS")) {
            throw new IOException("Password change failed: "+response.get("message").getAsString());
        }

        System.out.println("Password successfully changed!");

    }

    public void sendEncrypted(String message, PrintWriter out, SecretKey AESkey) {
        try {
            String encryptedJson = AESUtil.encrypt(message, AESkey);
            JsonObject wrapper = new JsonObject();

            //TODO: ver si realmente el type debería ser especifico para cada case o no
            wrapper.addProperty("type", "ENCRYPTED");
            wrapper.addProperty("data", encryptedJson);

            System.out.println("\nThis is the encrypted message sent to Server :" + wrapper);

            out.println(wrapper);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
