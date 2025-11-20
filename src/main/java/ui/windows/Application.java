package ui.windows;

import network.Client;
import org.example.SymptomType;
import pojos.Doctor;
import pojos.Patient;
import pojos.User;
import ui.ECGFileReader;
import ui.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;

public class Application extends JFrame {
    public static Color darker_purple = new Color(114, 82, 153); //#725299
    public static Color dark_purple = new Color(170, 84, 204); //#AA54CC
    public static Color pink = new Color(226, 169, 241); //#E2A9F1
    public static Color purple = new Color(196, 158, 207);
    public static Color turquoise = new Color(94, 186, 176); //#5EBAB0
    public static Color light_purple = new Color(239, 232, 255); //#EFE8FF
    public static Color light_turquoise = new Color(193, 252, 244); //#C1FCF4
    //public static Color light_turquoise = new Color(213, 242, 236); //#d5f2ec
    public static Color lighter_turquoise = new Color(243, 250, 249);//#f3faf9
    public static Color darker_turquoise = new Color(73, 129, 122);
    public static Color dark_turquoise = new Color(52, 152, 143); //#34988f
    public static Map<String, Color> symptomColors;
    //UI Panels
    private ArrayList<JPanel> appPanels;
    private UserLogIn logInPanel;
    private MainMenu mainMenu;

    //network
    public Client client;
    private String serverIPAdress = "localhost";
    private int serverPort = 9009;

    //Logic
    public Doctor doctor;
    public User user;

    public static void main(String[] args) {
        Application app = new Application();
        app.showDialogIntroduceIP(app);
        //app.setVisible(true);
    }
    public Application() {
        appPanels = new ArrayList<JPanel>();
        initComponents();
        setBounds(100, 100, 602, 436);

        logInPanel = new UserLogIn(this);
        appPanels.add(logInPanel);
        logInPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        client = new Client(this);
        //initGraph();
        setContentPane(logInPanel);
        //changeToMainMenu();
        doctor = new Doctor();
        user = new User();

        symptomColors = generateSymptomColors(SymptomType.class);
    }

    public static double[] importECG() {
        String filePath = "C:/path/to/record.txt";
        filePath = "C:\\Users\\mamen\\OneDrive - Fundación Universitaria San Pablo CEU\\06_BecaPregrado\\2023-2024\\Prototipo_registros\\Prototipo_Day1_2024-02-01_11-20-02.txt";
        filePath = "C:/Users/mamen/Documents/OpenSignals (r)evolution/files/ecg-acc-mamen-11-07_12-12-54.txt";
        try {
            String date = "2024-02-01";
            int samplingFrequency = 1000; // Adjusted sampling rate = 10 Hz
            String startTime = "11:20:02";

            double[] ecg = ECGFileReader.readECGFromFile(filePath);
            System.out.println("Loaded " + ecg.length + " samples.");
            //SignalGraphPanel ecgGraph = new SignalGraphPanel(ecg, samplingFrequency);
            //setContentPane(ecgGraph);
            return ecg;

        }catch (IOException e){
            System.out.println("Error reading file");
            return null;
        }
    }

    public static double[] importACC() {
        String filePath = "C:/path/to/record.txt";
        filePath = "C:\\Users\\mamen\\OneDrive - Fundación Universitaria San Pablo CEU\\06_BecaPregrado\\2023-2024\\Prototipo_registros\\Prototipo_Day1_2024-02-01_11-20-02.txt";
        filePath = "C:/Users/mamen/Documents/OpenSignals (r)evolution/files/ecg-acc-mamen-11-07_12-12-54.txt";
        try {
            double[] ecg = ECGFileReader.readECGFromFile(filePath);
            System.out.println("Loaded " + ecg.length + " samples.");
            return ecg;

        }catch (IOException e){
            System.out.println("Error reading file");
            return null;
        }
    }

    public void initComponents() {
        setTitle("Doctor Application");
        //setSize(602, 436);
        setLayout(null);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(getClass().getResource("/icons/night_guardian_mini_500.png")).getImage());
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // handle manually

        // Window listener to stop server when closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopEverything();
            }
        });

    }

    private void stopEverything(){
        if(client != null && client.isConnected()){
            client.stopClient(true);
        }
        dispose();
        System.exit(0);
    }

    public void changeToUserLogIn() {
        hideAllPanels();
        logInPanel.setVisible(true);
        this.setContentPane(logInPanel);
    }

    public void changeToPanel(JPanel panel) {
        hideAllPanels();
        if(!appPanels.contains(panel)) {
            appPanels.add(panel);
        }
        panel.setVisible(true);
        this.setContentPane(panel);
        System.out.println("Number of panels: " + appPanels.size());
    }

    public void changeToMainMenu(){
        hideAllPanels();
        if (mainMenu == null) {
            mainMenu = new MainMenu(this);
            appPanels.add(mainMenu);
            System.out.println("Patient Panel initialized");
        }

        mainMenu.setVisible(true);
        this.setContentPane(mainMenu);
        System.out.println("Number of panels: " + appPanels.size());

    }

    public void changeToMainMenuAndRemove(JPanel panel) {
        panel.setVisible(false);
        changeToMainMenu();
        if(appPanels.contains(panel)) {
            appPanels.remove(panel);
        }
        System.out.println("Number of panels: " + appPanels.size());
    }
    private void hideAllPanels() {
        for (JPanel jPanel : appPanels) {
            if(jPanel.isVisible()) {
                jPanel.setVisible(false);
            }
        }
    }

    public static Map<String, Color> generateSymptomColors(Class<? extends Enum<?>> enumClass) {
        Map<String, Color> colorMap = new HashMap<>();
        Random random = new Random();

        Set<Color> usedColors = new HashSet<>();

        for (Enum<?> constant : enumClass.getEnumConstants()) {
            Color color;
            do {
                color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            } while (usedColors.contains(color)); // evita duplicados exactos

            usedColors.add(color);
            colorMap.put(constant.name(), color);
        }

        return colorMap;
    }

    private void showDialogIntroduceIP(JFrame parentFrame) {
        MyTextField ipTextField = new MyTextField();
        MyButton okButton = new MyButton("OK");
        MyButton cancelButton = new MyButton("Cancel");

        AskQuestionDialog askForIP = new AskQuestionDialog(ipTextField, okButton, cancelButton);
        askForIP.setBackground(Color.white);
        askForIP.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog(parentFrame, "Server IP", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.getContentPane().add(askForIP);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        askForIP.showErrorMessage("Remember to turn off the computer firewall");
        //dialog.setSize(400, 200);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipTextField.getText();
                if(ip != null && !ip.isBlank()) {
                    try {
                        if (client.connect(ip, serverPort)) {
                            parentFrame.setVisible(true);
                            dialog.dispose();
                        } else {
                            askForIP.showErrorMessage("Server IP Error");
                        }
                    }catch (Exception ex) {
                        askForIP.showErrorMessage("Could not connect to server");
                    }
                }else{
                    askForIP.showErrorMessage("Please enter an IP Address");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                stopEverything();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
                stopEverything();
            }
        });

        dialog.setVisible(true);
    }

    public void onServerDisconnected() {
        SwingUtilities.invokeLater(() -> {

            JOptionPane.showMessageDialog(
                    null,
                    "The conexion with the server was interrupted.",
                    "Conexion error",
                    JOptionPane.ERROR_MESSAGE
            );

            // Espera a que el message dialog finalice y luego lanza el siguiente diálogo
            SwingUtilities.invokeLater(() -> {
                this.showDialogIntroduceIP(this);
                System.out.println("Requested new IP address");
            });
        });
    }
}
