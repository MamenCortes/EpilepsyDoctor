package network;

import Events.ServerDisconnectedEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import pojos.Doctor;
import pojos.Patient;
import pojos.Signal;
import pojos.User;
import ui.windows.Application;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//TODO: check tests when Paula finishes encryption
public class ClientTest {

    Client client;
    Application app;
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    @BeforeEach
    void setup() throws Exception {
        app = mock(Application.class);
        socket = mock(Socket.class);

        // Mock IO streams
        in = mock(BufferedReader.class);
        out = mock(PrintWriter.class);

        client = new Client();

        setField(client, "socket", socket);
        setField(client, "in", in);
        setField(client, "out", out);
        setField(client, "running", true);
    }

    // Utility reflection method
    private static void setField(Object obj, String name, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    void testConnectSuccess() throws Exception {

        InputStream mockInput = new ByteArrayInputStream("{\"type\":\"PING\"}\n".getBytes());
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        // Cliente es un spy para interceptar new Socket(...)
        Client client = spy(new Client());

        // Cuando connect llame a new Socket(ip,port) → devolver mockSocket
        doReturn(socket).when(client).createSocket(anyString(), anyInt());

        // Mockear streams del socket
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        // Ejecutar
        boolean ok = client.connect("localhost", 9009);

        // Validaciones
        assertTrue(ok);
    }

    //TODO
    @Test
    void testLoginSuccess() throws Exception {

        // Fake socket streams
        Doctor d = new Doctor();
        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Doctor\"}}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        // Start connection
        client.connect("localhost", 9009);

        // Fill queue manually simulating server
        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_DOCTOR_BY_EMAIL_RESPONSE");
        response.addProperty("status", "SUCCESS");
        response.add("doctor", d.toJason());
        q.add(JsonParser.parseString(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Doctor\"}}"
        ).getAsJsonObject());
        q.add(response);

        setField(client, "responseQueue", q);

        client.login("test@test.com", "123");

        assertNotNull(app.user);
        assertNotNull(app.doctor);
    }


    @Test
    void testStopClientInitiatedByClient() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        // Mock socket and out
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(socket.isClosed()).thenReturn(false);

        client.connect("localhost", 9009);

        client.stopClient(true);  // client-requested shutdown

        String sent = mockOutput.toString();
        assertTrue(sent.contains("STOP_CLIENT"));
        verify(app, never()).onServerDisconnected(new ServerDisconnectedEvent());   // UI must NOT be notified
    }

    @Test
    void testStopClientServerConnectionError() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        client.connect("localhost", 9009);

        client.stopClient(false);  // server-side shutdown

        String sent = mockOutput.toString();
        assertFalse(sent.contains("STOP_CLIENT"));     // not client-initiated
        verify(app).onServerDisconnected(new ServerDisconnectedEvent());            // UI must be informed
    }

    @Test
    void testStopClientWhenServerSendsStop() throws Exception {

        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"STOP_CLIENT\"}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        client.connect("localhost", 9009);

        // Give the listener time to process the STOP_CLIENT
        Thread.sleep(100);

        verify(app).onServerDisconnected(new ServerDisconnectedEvent());     // UI notified
    }

    @Test
    void testGetPatientsFromDoctorSuccess() throws Exception {

        // Fake socket streams
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        // Mock socket creation
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        // Fake logged user
        User fakeUser = new User(1, "doctor@mail.com", "1234", "Doctor");
        app.user = fakeUser;

        // Connect
        client.connect("localhost", 9009);

        Patient p1 = new Patient();
        p1.setId(10);
        Patient p2 = new Patient();
        p2.setId(20);
        // Prepare response queue
        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE\", " +
                        "\"status\":\"SUCCESS\", " +
                        "\"patients\":[" + p1.toJason() +
                        "," + p2.toJason() +
                        "]}"
        ).getAsJsonObject());

        // Inject queue
        setField(client, "responseQueue", queue);

        List<Patient> patients = client.getPatientsFromDoctor(5);

        assertEquals(2, patients.size());
        assertEquals(10, patients.get(0).getId());
        assertEquals(20, patients.get(1).getId());
    }

    @Test
    void testGetPatientsFromDoctorError() throws Exception {

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        // Fake logged user
        app.user = new User(1, "doctor@mail.com", "1234", "Doctor");

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE\", " +
                        "\"status\":\"ERROR\", " +
                        "\"message\":\"Not authorized\" }"
        ).getAsJsonObject());

        setField(client, "responseQueue", queue);

        assertThrows(ClientServerCommunicationError.class,
                () -> client.getPatientsFromDoctor(5));
    }

    @Test
    void testSaveCommentsSuccess() throws Exception {

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        // Fake logged user
        app.user = new User(1, "doctor@mail.com", "1234", "Doctor");

        client.connect("localhost", 9009);

        // Fake signal
        Signal signal = new Signal();
        signal.setId(77);
        signal.setComments("My notes");

        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(JsonParser.parseString(
                "{ \"type\":\"SAVE_COMMENTS_SIGNAL_RESPONSE\", " +
                        "\"status\":\"SUCCESS\" }"
        ).getAsJsonObject());

        setField(client, "responseQueue", queue);

        assertDoesNotThrow(() -> client.saveComments(5, signal));
    }

    @Test
    void testSaveCommentsError() throws Exception {

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        // Fake logged user
        app.user = new User(1, "doctor@mail.com", "1234", "Doctor");

        client.connect("localhost", 9009);

        Signal signal = new Signal();
        signal.setId(77);
        signal.setComments("Error test");

        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(JsonParser.parseString(
                "{ \"type\":\"SAVE_COMMENTS_SIGNAL_RESPONSE\", " +
                        "\"status\":\"ERROR\", \"message\":\"DB fail\" }"
        ).getAsJsonObject());

        setField(client, "responseQueue", queue);

        assertThrows(ClientServerCommunicationError.class,
                () -> client.saveComments(5, signal));
    }

    @Test
    void testGetAllSignalsFromPatientSuccess() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        User fakeUser = new User(1, "doctor@mail.com", "1234", "Doctor");
        app.user = fakeUser;

        client.connect("localhost", 9009);

        // --- Fake signals metadata ---
        JsonObject s1 = new JsonObject();
        s1.addProperty("signal_id", 1);
        s1.addProperty("date", "2025-02-01");
        s1.addProperty("comments", "Sig1");
        s1.addProperty("sampling_rate", 500.0);
        s1.addProperty("patient_id", 88);

        JsonObject s2 = new JsonObject();
        s2.addProperty("signal_id", 2);
        s2.addProperty("date", "2025-02-02");
        s2.addProperty("comments", "Sig2");
        s2.addProperty("sampling_rate", 1000.0);
        s2.addProperty("patient_id", 88);

        JsonObject s3 = new JsonObject();
        s3.addProperty("signal_id", 3);
        s3.addProperty("date", "2025-02-03");
        s3.addProperty("comments", "Sig3");
        s3.addProperty("sampling_rate", 2000.0);
        s3.addProperty("patient_id", 88);

        JsonArray signalsArray = new JsonArray();
        signalsArray.add(s1);
        signalsArray.add(s2);
        signalsArray.add(s3);
        JsonObject response = new JsonObject();
        response.addProperty("type", "REQUEST_PATIENT_SIGNALS_RESPONSE");
        response.addProperty("status", "SUCCESS");
        response.add("signals", signalsArray);
        // Prepare response queue
        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(response);

        setField(client, "responseQueue", queue);

        List<Signal> signals = client.getAllSignalsFromPatient(88);

        assertEquals(3, signals.size());

        assertEquals(1, signals.get(0).getId());
        assertEquals("Sig1", signals.get(0).getComments());

        assertEquals(2, signals.get(1).getId());
        assertEquals("Sig2", signals.get(1).getComments());

        assertEquals(3, signals.get(2).getId());
        assertEquals("Sig3", signals.get(2).getComments());
    }
    @Test
    void testGetAllSignalsFromPatientError() throws Exception {

        // Fake socket streams
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        // Mock socket creation
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        // Fake logged user
        User fakeUser = new User(1, "doctor@mail.com", "1234", "Doctor");
        app.user = fakeUser;

        // Connect
        client.connect("localhost", 9009);

        // --- Fake ERROR response ---
        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_PATIENT_SIGNALS_RESPONSE\", " +
                        "\"status\":\"ERROR\", " +
                        "\"message\":\"Patient not found\" }"
        ).getAsJsonObject());

        // Inject queue
        setField(client, "responseQueue", queue);

        // --- EXPECT EXCEPTION ---
        assertThrows(ClientServerCommunicationError.class,
                () -> client.getAllSignalsFromPatient(88));
    }
    @Test
    void testGetSignalByIdSuccessWithZip() throws Exception {

        // Fake streams
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        // Logged doctor
        app.user = new User(1, "doctor@mail.com", "1234", "Doctor");

        client.connect("localhost", 9009);

        // ---- 1) Crear ZIP temporal ----
        File tempZip = File.createTempFile("signal77", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempZip)) {
            fos.write("THIS_IS_FAKE_ZIP_DATA".getBytes());
        }
        byte[] bytes = java.nio.file.Files.readAllBytes(tempZip.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);

        // ---- 2) Crear JSON con ZIP ----
        String json = """
        {
          "type": "REQUEST_SIGNAL_BY_ID_RESPONSE",
          "status": "SUCCESS",
          "compression": "zip-base64",
          "filename": "signal_77.zip",
          "data": "%s",
          "metadata": {
            "signal_id": 77,
            "date": "2025-02-01",
            "comments": "Test signal",
            "sampling_rate": 500.0,
            "patient_id": 88
          }
        }
        """.formatted(base64);

        JsonObject responseJson = JsonParser.parseString(json).getAsJsonObject();

        // ---- 3) Insert into responseQueue ----
        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(responseJson);
        setField(client, "responseQueue", queue);

        // ---- 4) Execute method ----
        Signal signal = client.getSignalFromId(77);

        // ---- 5) Assertions ----
        assertNotNull(signal);
        assertEquals(77, signal.getId());
        assertEquals("Test signal", signal.getComments());
        assertEquals("500.0", signal.getFrequency().toString());
        assertEquals(LocalDate.of(2025, 2, 1), signal.getDate());

        // ZIP must exist on disk (created from Base64)
        assertNotNull(signal.getZipFile());
        assertTrue(signal.getZipFile().exists());
    }

    @Test
    void testGetSignalByIdError() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        app.user = new User(1, "doctor@mail.com", "1234", "Doctor");

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();

        queue.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_SIGNAL_BY_ID_RESPONSE\", " +
                        "\"status\":\"ERROR\", \"message\":\"Signal not found\" }"
        ).getAsJsonObject());

        setField(client, "responseQueue", queue);

        // Expect exception
        assertThrows(ClientServerCommunicationError.class,
                () -> client.getSignalFromId(77));
    }

}