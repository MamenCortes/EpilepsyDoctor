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
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles the communication between the client application and the remote server.
 * <p>
 * This class manages TCP connections, encryption (RSA + AES), authentication,
 * messaging, and asynchronous reception of data through a listening thread.
 * It also stores cryptographic keys, manages a queue of responses, and provides
 * high-level methods for login, uploading signals, changing password, retrieving
 * doctors, and more.
 * </p>
 */
public class Client {
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    private Gson gson = new Gson();
    private volatile Boolean running = false;
    private User user;
    //Estructura diseñada para comunicar threads entre sí de manera segura y sincronizada
    //permite que un thread meta mensajes en la cola (con put())
    //Y que otro thread los reciba (con take() o poll())
    //si no hay mensajes, take() se bloquea automáticamente, sin consumir CPU
    private BlockingQueue<JsonObject> responseQueue = new LinkedBlockingQueue<>();
    private KeyPair clientKeyPair;
    private PublicKey serverPublicKey;
    private SecretKey token;
    private final CountDownLatch tokenReady = new CountDownLatch(1);
    /**
     * Creates a new Client instance without establishing a connection.
     */
    public Client() {
    }
    /**
     * Connects to the server using the given IP and port.
     * Initializes I/O streams, sends introductory messages,
     * and starts the listener thread.
     *
     * @param ip   server IP address
     * @param port server port
     * @return true if the connection was successful, false otherwise
     */
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

    /**
     * Starts a background listener thread that waits for messages from the server.
     * <p>
     * This thread handles:
     * <ul>
     *     <li>Reception of encrypted and unencrypted messages</li>
     *     <li>Token exchange and key verification</li>
     *     <li>Stopping the client when instructed</li>
     *     <li>Routing messages into a blocking queue</li>
     * </ul>
     * </p>
     */
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

                    if (token == null){
                        switch (type){
                            case "SERVER_PUBLIC_KEY" : {
                                //Receive and store server's public key
                                try {
                                    String serverPublicKeyEncoded = request.get("data").getAsString();
                                    byte[] keyBytes = Base64.getDecoder().decode(serverPublicKeyEncoded);
                                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                    this.serverPublicKey = keyFactory.generatePublic(keySpec);
                                    System.out.println("Server Public Key stored successfully: "+Base64.getEncoder().encodeToString(this.serverPublicKey.getEncoded()));
                                } catch (Exception e) {
                                    System.out.println("Failed to process SERVER_PUBLIC_KEY request: " + e.getMessage());
                                    stopClient(true);
                                }
                                break;
                            }
                            case "TOKEN_REQUEST_RESPONSE": {
                                try {
                                    String encryptedToken = request.get("token").getAsString();
                                    String signatureBase64 = request.get("signature").getAsString();
                                    byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
                                    //Decrypt token with Client Private Key
                                    String token = RSAUtil.decrypt(encryptedToken, clientKeyPair.getPrivate());
                                    byte[] tokenBytes = Base64.getDecoder().decode(token);
                                    // Verify signature using Server Public Key
                                    Signature signature = Signature.getInstance("SHA256withRSA");
                                    signature.initVerify(serverPublicKey);
                                    signature.update(tokenBytes);

                                    boolean verified = signature.verify(signatureBytes);
                                    if (verified) {
                                        System.out.println("Token verified and trusted");
                                        //Reconstruction of the Secret Key on the client side
                                        javax.crypto.SecretKey secretKey = new SecretKeySpec(tokenBytes, 0, tokenBytes.length, "AES");
                                        saveToken(secretKey);
                                        System.out.println("🔑 Server's AES Token (Base64): " + Base64.getEncoder().encodeToString(this.token.getEncoded()));
                                    } else {
                                        System.out.println("Signature verification failed. Do not trust the token.");
                                        //This ensures that if the token is not received, the connection STOPS
                                        stopClient(true);
                                    }
                                } catch (Exception e) {
                                    System.out.println("Error handling TOKEN_RESPONSE: " + e.getMessage());
                                    e.printStackTrace();
                                    stopClient(true);
                                }
                                break;
                            }
                            case "ENCRYPTED_RESPONSE":{
                                String encrypted = request.get("message").getAsString();
                                String signatureBase64 = request.get("signature").getAsString();
                                //Decrypt with clients private key
                                String json = RSAUtil.decrypt(encrypted, clientKeyPair.getPrivate());
                                //Verify signature with server public key
                                Signature sig = Signature.getInstance("SHA256withRSA");
                                sig.initVerify(serverPublicKey);
                                sig.update(json.getBytes());

                                if (!sig.verify(Base64.getDecoder().decode(signatureBase64))) {
                                    System.err.println("Signature verification failed");
                                    break;
                                }

                                JsonObject decrypted = gson.fromJson(json, JsonObject.class);

                                String innerType = decrypted.get("type").getAsString();

                                if (innerType.equals("CHANGE_PASSWORD_REQUEST_RESPONSE")) {
                                    responseQueue.put(decrypted);
                                }

                                break;
                            }
                        }
                    }

                    // After the Client receives the Server's public key to encrypt the Secret key, reads responses
                    String typeDecrypted = type;
                    JsonObject decryptedRequest = request;
                    if (typeDecrypted.equals("ENCRYPTED")){
                        String encryptedData = request.get("data").getAsString();
                        String decryptedJson = encryptedData;
                        decryptedJson = AESUtil.decrypt(encryptedData, token);
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
    /**
     * Sets the RSA KeyPair for the client used to decrypt messages
     * and to verify server authenticity.
     *
     * @param keyPair the RSA key pair
     */
    public void setClientKeyPair (KeyPair keyPair){
        this.clientKeyPair = keyPair;
    }

    /**
     * Creates a TCP socket. This method is protected so it can be overridden in tests.
     *
     * @param ip   server IP
     * @param port server port
     * @return a connected Socket
     * @throws IOException if the connection fails
     */
    protected Socket createSocket(String ip, int port) throws IOException {
        return new Socket(ip, port);
    }
    /**
     * Checks whether the socket is connected to the server.
     *
     * @return true if connected, false otherwise
     */
    public Boolean isConnected(){
        if(socket == null){
            return false;
        }else {
            return socket.isConnected();
        }
    }
    /**
     * Stops the client gracefully.
     * <ul>
     *     <li>If initiated by client: sends an encrypted STOP_CLIENT message</li>
     *     <li>Releases network resources</li>
     *     <li>Notifies the UI if server initiated the disconnection</li>
     * </ul>
     *
     * @param initiatedByClient true if the client requests shutdown
     */
    public void stopClient(boolean initiatedByClient) {
        if (initiatedByClient && socket != null && !socket.isClosed()) {
            // Only send STOP_CLIENT if CLIENT requested shutdown
            Map<String, Object> message = new HashMap<>();
            message.put("type", "STOP_CLIENT");
            String jsonMessage = gson.toJson(message);
            System.out.println("\nBefore encryption, STOP_CLIENT to Server: "+jsonMessage);
            sendEncrypted(jsonMessage, out, token);
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
    /**
     * Sends an initial plain-text greeting message to the server.
     *
     * @throws IOException if communication fails
     */
    private void sendInitialMessage() throws IOException {
        System.out.println("Connection established... sending text");
        out.println("Hi! I'm a new client!\n");
    }
    /**
     * Sends an activation request to the server and waits for a response.
     *
     * @param email          the email to activate
     * @param temporaryPass  the temporary password
     * @param oneTimeToken   the single-use activation token
     * @return true if activation succeeded, false otherwise
     * @throws InterruptedException if waiting for a response is interrupted
     */
    public boolean sendActivationRequest (String email, String temporaryPass, String oneTimeToken) throws InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("temp_pass", temporaryPass);
        data.put("temp_token", oneTimeToken);

        Map<String, Object> request = new HashMap<>();
        request.put("type", "ACTIVATION_REQUEST");
        request.put("data", data);

        out.println(gson.toJson(request)); //Plain text
        out.flush();

        System.out.println("📤 ACTIVATION_REQUEST sent to server: " + request);
        boolean activationSuccess = false;
        while(true) {
            JsonObject response = responseQueue.take();
            String type = response.get("type").getAsString();

            switch(type){
                case "SERVER_PUBLIC_KEY": {
                    try {
                        String serverPublicKeyEncoded = response.get("data").getAsString();
                        byte[] keyBytes = Base64.getDecoder().decode(serverPublicKeyEncoded);
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        this.serverPublicKey = keyFactory.generatePublic(keySpec);
                        System.out.println("Server Public Key stored successfully: " +
                                Base64.getEncoder().encodeToString(this.serverPublicKey.getEncoded()));
                    } catch (Exception e) {
                        System.out.println("Failed to process SERVER_PUBLIC_KEY: " + e.getMessage());
                        stopClient(true);
                        return false;
                    }
                    break;
                }

                case "ACTIVATION_REQUEST_RESPONSE": {
                    String status = response.get("status").getAsString();
                    activationSuccess = status.equals("SUCCESS");
                    return activationSuccess;
                }
                default:
                    System.out.println("Unhandled response type: "+type);
                    break;
            }
        }
    }
    /**
     * Sends a token request message, providing the user email.
     *
     * @param email the user email
     */
    public void sendTokenRequest(String email){
        JsonObject tokenRequest = new JsonObject();
        tokenRequest.addProperty("type", "TOKEN_REQUEST");
        tokenRequest.addProperty("email",email);
        out.println(gson.toJson(tokenRequest));
        out.flush();

        System.out.println("TOKEN_REQUEST sent to the Server");
    }
    /**
     * Sends the client's public RSA key to the server for encryption initialization.
     *
     * @param publicKey the public RSA key
     * @param email     the associated email
     */
    public void sendPublicKey(PublicKey publicKey, String email) {
        String clientPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        JsonObject data = new JsonObject();
        data.addProperty("email", email);
        data.addProperty("public_key", clientPublicKey);

        JsonObject response = new JsonObject();
        response.addProperty("type", "CLIENT_PUBLIC_KEY");
        response.add("data", data);
        out.println(gson.toJson(response));
        out.flush();

        System.out.println("Sent Client's Public Key to Server");
    }
    /**
     * Logs a patient into the system using encrypted communication.
     * <p>
     * Steps:
     * <ol>
     *     <li>Retrieves RSA keys</li>
     *     <li>Requests AES token from server</li>
     *     <li>Sends encrypted LOGIN_REQUEST</li>
     *     <li>Parses LOGIN_RESPONSE</li>
     *     <li>Requests additional patient data</li>
     * </ol>
     * </p>
     *
     * @param email    the user email
     * @param password the user password
     * @return AppData containing user and patient info
     * @throws IOException          communication error
     * @throws InterruptedException waiting interruption
     * @throws LogInError           invalid login
     */
    public AppData login(String email, String password) throws IOException, InterruptedException, LogInError {
        //String message = "LOGIN;" + email + ";" + password;
        String fileEmail = email.replaceAll("[@.]", "_");
        PrivateKey privateKey = RSAKeyManager.retrievePrivateKey(fileEmail);
        System.out.println("This is the client's private key: "+ Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        PublicKey publicKey = RSAKeyManager.retrievePublicKey(fileEmail);
        System.out.println("This is the client's public key: "+Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        this.clientKeyPair = new KeyPair(publicKey,privateKey);
        sendTokenRequest(email);
        System.out.println("\nWaiting for token to arrive...");
        tokenReady.await();
        System.out.println("Token received, continuing with login process...");

        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("access_permits", "Doctor");

        Map<String, Object> message = new HashMap<>();
        message.put("type", "LOGIN_REQUEST");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, LOGIN_REQUEST to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, token);
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
                sendEncrypted(jsonMessage, out, token);

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

    /**
     * Retrieves a list of patients associated with a specific doctor.
     * <p>
     * This method constructs and sends an encrypted request of type
     * {@code REQUEST_PATIENTS_FROM_DOCTOR}, waits for the server response,
     * and parses the list of patients returned. If the server returns an error
     * status, a {@link ClientServerCommunicationError} is thrown.
     * </p>
     *
     * @param doctor_id the ID of the doctor whose patients will be retrieved
     * @return a list of {@link Patient} objects assigned to the doctor
     *
     * @throws IOException              if an I/O error occurs while sending or receiving data
     * @throws InterruptedException     if the waiting thread is interrupted
     * @throws ClientServerCommunicationError if the server responds with an error
     */
    public List<Patient> getPatientsFromDoctor(int doctor_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("doctor_id", doctor_id);
        if(user != null)data.put("user_id", user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_PATIENTS_FROM_DOCTOR");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, REQUEST_PATIENTS_FROM_DOCTOR to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, token);

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
    /**
     * Retrieves all recorded signals associated with a given patient.
     * <p>
     * Sends an encrypted {@code REQUEST_PATIENT_SIGNALS} message to the server
     * containing the patient ID and optionally the user ID. The method waits for
     * a {@code REQUEST_PATIENT_SIGNALS_RESPONSE}, validates the status, and parses
     * the list of signals returned.
     * </p>
     *
     * @param patient_id the ID of the patient whose signals will be retrieved
     * @return a list of {@link Signal} objects belonging to the patient
     *
     * @throws IOException              if an I/O error occurs during communication
     * @throws InterruptedException     if the response waiting is interrupted
     * @throws ClientServerCommunicationError if the server indicates an error
     */
    public List<Signal> getAllSignalsFromPatient (int patient_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("patient_id", patient_id);
        if(user != null)data.put("user_id", user.getId());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_PATIENT_SIGNALS");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, REQUEST_PATIENT_SIGNALS to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, token);

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
    /**
     * Retrieves a specific signal by its unique ID.
     * <p>
     * Sends an encrypted {@code REQUEST_SIGNAL} message with the signal ID and
     * waits for the server’s {@code REQUEST_SIGNAL_RESPONSE}. On success, the
     * method reconstructs both the metadata and the associated ZIP-compressed
     * signal data and builds a {@link Signal} instance using
     * {@code Signal.fromJasonWithZip()}.
     * </p>
     *
     * @param signal_id the unique identifier of the signal to retrieve
     * @return the reconstructed {@link Signal} object
     *
     * @throws IOException              if an I/O communication error occurs
     * @throws InterruptedException     if the waiting thread is interrupted
     * @throws ClientServerCommunicationError if the server returns an error
     */
    public Signal getSignalFromId (int signal_id) throws IOException, InterruptedException {
        Map<String, Object> data = new HashMap<>();
        data.put("signal_id", signal_id);
        if(user != null)data.put("user_id", user.getId());
        Map<String, Object> message = new HashMap<>();
        message.put("type", "REQUEST_SIGNAL");
        message.put("data", data);
        String jsonMessage = gson.toJson(message);
        System.out.println("\nBefore encryption, REQUEST_SIGNAL to Server: "+jsonMessage);
        sendEncrypted(jsonMessage, out, token);

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
    /**
     * Saves or updates the comments associated with a specific signal.
     * <p>
     * Sends an encrypted {@code SAVE_COMMENTS_SIGNAL} request to the server
     * containing the patient ID, signal ID, and updated comments. The method
     * waits for the server’s {@code SAVE_COMMENTS_SIGNAL_RESPONSE} and validates
     * the operation result.
     * </p>
     *
     * @param patient_id the ID of the patient who owns the signal
     * @param signal     the signal whose comments will be saved
     *
     * @throws IOException              if an error occurs while sending/receiving data
     * @throws InterruptedException     if waiting for the server response is interrupted
     * @throws ClientServerCommunicationError if the server indicates a failure
     */
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
        sendEncrypted(jsonMessage, out, token);

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

    /**
     * Safely releases network and I/O resources associated with the client connection.
     * <p>
     * This method attempts to close the provided {@link PrintWriter}, {@link BufferedReader},
     * and {@link Socket} objects. Each resource is checked for nullity before closing,
     * and exceptions during closure are caught and logged without interrupting execution.
     * </p>
     *
     * @param printWriter the PrintWriter to close (may be null)
     * @param reader      the BufferedReader to close (may be null)
     * @param socket      the Socket to close; never null when called
     */
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

    /**
     * Sends a request to update a user's password and waits for the server's response.
     * <p>
     * A JSON message of type {@code CHANGE_PASSWORD_REQUEST} is sent encrypted using AES.
     * The method then blocks until it receives a corresponding
     * {@code CHANGE_PASSWORD_REQUEST_RESPONSE}. If the response indicates failure,
     * an {@link IOException} is thrown.
     * </p>
     *
     * @param email        the email of the account whose password will be changed
     * @param newPassword  the new password to set for the account
     *
     * @throws IOException              if the server rejects the request or I/O fails
     * @throws InterruptedException     if waiting for the response is interrupted
     */
    public void changePassword(String email, String newPassword) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("new_password", newPassword);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "CHANGE_PASSWORD_REQUEST");
        message.put("data", data);

        String jsonMessage = gson.toJson(message);

        String fileEmail = email.replaceAll("[@.]", "_");
        PrivateKey privateKey =RSAKeyManager.retrievePrivateKey(fileEmail);
        PublicKey publicKey = RSAKeyManager.retrievePublicKey(fileEmail);
        this.clientKeyPair = new KeyPair(publicKey,privateKey);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(jsonMessage.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

        String encryptedMessage = RSAUtil.encrypt(jsonMessage, serverPublicKey);
        JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", "ENCRYPTED_MESSAGE");
        wrapper.addProperty("message", encryptedMessage);
        wrapper.addProperty("signature", signatureBase64);
        wrapper.addProperty("clientEmail", email);

        out.println(gson.toJson(wrapper));
        out.flush();
        //sendEncrypted(jsonMessage, out, token);

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

    /**
     * Encrypts a plain JSON message using AES and sends it to the server.
     * <p>
     * The message is encrypted with the provided AES key using {@link AESUtil#encrypt(String, SecretKey)}.
     * The encrypted payload is wrapped inside a JSON structure:
     * <pre>
     * {
     *   "type": "ENCRYPTED",
     *   "data": "<encrypted-base64-string>"
     * }
     * </pre>
     * The resulting JSON is then sent through the provided {@link PrintWriter}.
     * </p>
     *
     * @param message the plain JSON string to encrypt
     * @param out     the output writer used to send the encrypted message
     * @param AESkey  the shared AES session key used for encryption
     */
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

    /**
     * TEST-ONLY method that manually injects a token and unblocks the login latch.
     */
    public void saveToken(SecretKey testToken) {
        this.token = testToken;
        this.tokenReady.countDown();
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
}
