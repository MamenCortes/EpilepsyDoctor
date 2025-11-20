package network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import pojos.Doctor;
import pojos.Patient;
import pojos.Signal;
import pojos.User;
import ui.windows.Application;

import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

        client = new Client(app);

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
        Client client = spy(new Client(app));

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

    @Test
    void testLoginSuccess() throws Exception {

        // Fake socket streams
        Doctor d = new Doctor();
        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"LOGIN_RESPONSE\",\"status\":\"SUCCESS\",\"data\":{\"id\":1,\"role\":\"Doctor\"}}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client(app));

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

        Client client = spy(new Client(app));

        // Mock socket and out
        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(socket.isClosed()).thenReturn(false);

        client.connect("localhost", 9009);

        client.stopClient(true);  // client-requested shutdown

        String sent = mockOutput.toString();
        assertTrue(sent.contains("STOP_CLIENT"));
        verify(app, never()).onServerDisconnected();   // UI must NOT be notified
    }

    @Test
    void testStopClientServerConnectionError() throws Exception {

        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client(app));

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getOutputStream()).thenReturn(mockOutput);
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        client.connect("localhost", 9009);

        client.stopClient(false);  // server-side shutdown

        String sent = mockOutput.toString();
        assertFalse(sent.contains("STOP_CLIENT"));     // not client-initiated
        verify(app).onServerDisconnected();            // UI must be informed
    }

    @Test
    void testStopClientWhenServerSendsStop() throws Exception {

        ByteArrayInputStream mockInput = new ByteArrayInputStream(
                "{\"type\":\"STOP_CLIENT\"}\n".getBytes()
        );
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();

        Client client = spy(new Client(app));

        doReturn(socket).when(client).createSocket(anyString(), anyInt());
        when(socket.getInputStream()).thenReturn(mockInput);
        when(socket.getOutputStream()).thenReturn(mockOutput);

        client.connect("localhost", 9009);

        // Give the listener time to process the STOP_CLIENT
        Thread.sleep(100);

        verify(app).onServerDisconnected();     // UI notified
    }

    @Test
    void testGetPatientsFromDoctorSuccess() throws Exception {

        // Fake socket streams
        ByteArrayOutputStream mockOutput = new ByteArrayOutputStream();
        ByteArrayInputStream mockInput = new ByteArrayInputStream("".getBytes());

        Client client = spy(new Client(app));

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
                        "\"patients\":[" +p1.toJason()+
                        "," +p2.toJason()+
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

        Client client = spy(new Client(app));

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

        Client client = spy(new Client(app));

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

        Client client = spy(new Client(app));

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


}