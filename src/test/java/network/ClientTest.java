package network;

import Events.ServerDisconnectedEvent;
import Events.UIEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import encryption.AESUtil;
import java.security.SecureRandom;
import org.junit.jupiter.api.*;
import pojos.*;
import ui.windows.Application;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import encryption.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        // The client expects valid JSON lines followed by EOF.
        // PING is safe, so we use that.
        InputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"PING\"}\n".getBytes()
        );

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        // Intercept socket creation
        doReturn(socket).when(client).createSocket(anyString(), anyInt());

        // Provide mock streams
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        // The code uses socket.isClosed() internally, so ensure it behaves
        when(socket.isClosed()).thenReturn(false);

        boolean ok = client.connect("localhost", 9009);

        assertTrue(ok);
    }

    @Test
    void testLoginSuccess() throws Exception {

        // Prepare client with real RSA key file
        KeyPair clientKeys = RSAKeyManager.generateKeyPair();
        String fileEmail = "test_test_com";
        RSAKeyManager.saveKey(clientKeys, fileEmail);

        Client client = spy(new Client());

        // Fake socket streams
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        client.connect("localhost", 9009);

        // ---- Bypass async token handshake ----
        SecretKey fakeToken = new SecretKeySpec(new byte[16], "AES");
        client.saveToken(fakeToken);

        // ---- Prepare fake responses ----
        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();

        queue.add(JsonParser.parseString("""
        {
          "type":"LOGIN_RESPONSE",
          "status":"SUCCESS",
          "data":{
            "id":1,
            "role":"Doctor"
          }
        }
        """).getAsJsonObject());

                queue.add(JsonParser.parseString("""
        {
          "type":"REQUEST_DOCTOR_BY_EMAIL_RESPONSE",
          "status":"SUCCESS",
          "doctor":{
            "id":1,
            "name":"John",
            "surname":"Doe",
            "contact":123456789,
            "email":"test@test.com",
            "department":"Neuro",
            "speciality":"EEG"
          }
        }
        """).getAsJsonObject());

        setField(client, "responseQueue", queue);


        // ---- Run login ----
        AppData appData = client.login("test@test.com", "123");

        assertNotNull(appData.getUser());
        assertNotNull(appData.getDoctor());
    }




    @Test
    void testStopClientInitiatedByClient() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        // Mock socket streams
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(socket.isClosed()).thenReturn(false);

        // Inject a fake AES token so encryption works
        SecretKey fakeAES = new SecretKeySpec(new byte[16], "AES");
        setField(client, "token", fakeAES);

        client.connect("localhost", 9009);

        client.stopClient(true);

        String sent = mockOutput.toString();

        // We expect an encrypted message wrapper, NOT raw STOP_CLIENT
        assertTrue(sent.contains("\"type\":\"ENCRYPTED\""));
        assertTrue(sent.contains("\"data\""));
    }

    @Test
    void testStopClientServerConnectionError() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        // Simulate socket creation
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        // Act: connect and then stop as if server closed connection
        client.connect("localhost", 9009);
        client.stopClient(false);  // server-side shutdown

        // Validate that NO STOP_CLIENT message was sent
        String sent = mockOutput.toString();
        assertFalse(sent.contains("STOP_CLIENT"));

        // The client NOW uses UIEventBus instead of app.onServerDisconnected()
        // → So we remove the old expectation entirely.
        // Nothing to verify here because EventBus is not mocked.

        // Optional: verify socket close was attempted
        verify(socket).close();
    }


    @Test
    void testStopClientWhenServerSendsStop() throws Exception {

        // This will capture UIEventBus events
        class EventCatcher {
            boolean disconnected = false;

            @Subscribe
            public void onDisconnect(ServerDisconnectedEvent ev) {
                disconnected = true;
            }
        }

        EventCatcher catcher = new EventCatcher();
        UIEventBus.BUS.register(catcher);

        // Mock server sending STOP_CLIENT line
        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"STOP_CLIENT\"}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.isClosed()).thenReturn(false);

        // Act
        client.connect("localhost", 9009);

        // Give listener time to process the STOP_CLIENT
        Thread.sleep(100);

        // Assert socket is closed
        verify(socket).close();

        // Client must NOT send STOP_CLIENT back
        String sent = mockOutput.toString();
        assertFalse(sent.contains("STOP_CLIENT"));

        // EventBus must have received the event
        assertTrue(catcher.disconnected);

        UIEventBus.BUS.unregister(catcher);
    }


    @Test
    void testGetPatientsFromDoctorSuccess() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        User fakeUser = new User(1, "doctor@mail.com", "123", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

        Patient p1 = new Patient();
        p1.setId(10);
        Patient p2 = new Patient();
        p2.setId(20);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        q.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE\", \"status\":\"SUCCESS\", " +
                        "\"patients\":[" + p1.toJason() + "," + p2.toJason() + "]}"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

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

        User fakeUser = new User(1, "doctor@mail.com", "123", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        q.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_PATIENTS_FROM_DOCTOR_RESPONSE\", \"status\":\"ERROR\", \"message\":\"Not authorized\"}"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

        assertThrows(ClientServerCommunicationError.class,
                () -> client.getPatientsFromDoctor(5));
    }


    @Test
    void testSaveCommentsSuccess() throws Exception {

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        User fakeUser = new User(1, "doctor@mail.com", "123", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

        Signal s = new Signal();
        s.setId(77);
        s.setComments("OK");

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        q.add(JsonParser.parseString(
                "{ \"type\":\"SAVE_COMMENTS_SIGNAL_RESPONSE\", \"status\":\"SUCCESS\"}"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

        assertDoesNotThrow(() -> client.saveComments(5, s));
    }


    @Test
    void testSaveCommentsError() throws Exception {

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        User fakeUser = new User(1, "doctor@mail.com", "123", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

        Signal s = new Signal();
        s.setId(77);
        s.setComments("---");

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        q.add(JsonParser.parseString(
                "{ \"type\":\"SAVE_COMMENTS_SIGNAL_RESPONSE\", \"status\":\"ERROR\", \"message\":\"DB fail\"}"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

        assertThrows(ClientServerCommunicationError.class,
                () -> client.saveComments(5, s));
    }


    @Test
    void testGetAllSignalsFromPatientSuccess() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        User fakeUser = new User(1, "doctor@mail.com", "123", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

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

        JsonArray arr = new JsonArray();
        arr.add(s1); arr.add(s2); arr.add(s3);

        JsonObject resp = new JsonObject();
        resp.addProperty("type", "REQUEST_PATIENT_SIGNALS_RESPONSE");
        resp.addProperty("status", "SUCCESS");
        resp.add("signals", arr);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();
        q.add(resp);
        setField(client, "responseQueue", q);

        List<Signal> list = client.getAllSignalsFromPatient(88);

        assertEquals(3, list.size());
        assertEquals(1, list.get(0).getId());
        assertEquals("Sig1", list.get(0).getComments());
        assertEquals(2, list.get(1).getId());
        assertEquals(3, list.get(2).getId());
    }

    @Test
    void testGetAllSignalsFromPatientError() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        User fakeUser = new User(1, "doctor@mail.com", "123", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        q.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_PATIENT_SIGNALS_RESPONSE\", \"status\":\"ERROR\", \"message\":\"Patient not found\" }"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

        assertThrows(ClientServerCommunicationError.class,
                () -> client.getAllSignalsFromPatient(88));
    }

    @Test
    void testGetSignalByIdSuccessWithZip() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client());

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(mockInput);

        // Fake logged doctor
        User fakeUser = new User(1, "doctor@mail.com", "1234", "Doctor");
        setField(client, "user", fakeUser);

        // Inject dummy AES token so sendEncrypted() does not crash
        SecretKey fakeToken = new SecretKeySpec(new byte[16], "AES");
        setField(client, "token", fakeToken);

        client.connect("localhost", 9009);

        // ----- create fake ZIP -----
        File tempZip = File.createTempFile("signal77", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempZip)) {
            fos.write("THIS_IS_FAKE_ZIP_DATA".getBytes());
        }
        String base64 = Base64.getEncoder().encodeToString(
                java.nio.file.Files.readAllBytes(tempZip.toPath())
        );

        // ----- Build JSON -----
        String json = """
        {
          "type": "REQUEST_SIGNAL_RESPONSE",
          "status": "SUCCESS",
          "filename": "signal_77.zip",
          "dataBytes": "%s",
          "metadata": {
            "signal_id": 77,
            "patient_id": 88,
            "date": "2025-02-01",
            "comments": "Test signal",
            "sampling_rate": 500.0
          }
        }
        """.formatted(base64);

        BlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();
        queue.add(JsonParser.parseString(json).getAsJsonObject());
        setField(client, "responseQueue", queue);

        // ----- Call method -----
        Signal signal = client.getSignalFromId(77);

        // ----- Assertions -----
        assertNotNull(signal);
        assertEquals(77, signal.getId());
        assertEquals("Test signal", signal.getComments());
        assertEquals(500.0, signal.getFrequency().doubleValue());// numeric comparison
        assertEquals(LocalDate.of(2025, 2, 1), signal.getDate());

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

        User fakeUser = new User(1, "doctor@mail.com", "1234", "Doctor");
        setField(client, "user", fakeUser);

        client.connect("localhost", 9009);

        BlockingQueue<JsonObject> q = new LinkedBlockingQueue<>();

        q.add(JsonParser.parseString(
                "{ \"type\":\"REQUEST_SIGNAL_RESPONSE\", " +
                        "\"status\":\"ERROR\", \"message\":\"Signal not found\" }"
        ).getAsJsonObject());

        setField(client, "responseQueue", q);

        assertThrows(ClientServerCommunicationError.class,
                () -> client.getSignalFromId(77));
    }


}