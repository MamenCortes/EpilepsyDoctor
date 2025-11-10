package ui.windows;

import net.miginfocom.swing.MigLayout;
import pojos.Patient;
import pojos.Report;
import pojos.Signal;
import ui.ECGFileReader;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;
import ui.components.RecordingCell;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PatientInfo extends JPanel implements ActionListener, MouseListener {
    private static final long serialVersionUID = -2213334704230710767L;
    private Application appMain;
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
    //private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;
    protected JLabel title;
    protected String titleText;
    protected ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/patient-info64-2.png"));
    protected MyTextField searchByTextField;
    protected MyButton patientDetailsButton;
    protected MyButton recordingsHistoryButton;
    protected MyButton symptomsCalendarButton;
    protected JLabel errorMessage;
    protected MyButton goBackButton;
    private MyButton searchButton;
    private MyButton openRecordingButton;
    private JPanel patientDetailsPanel;
    private JPanel recordingsHistoryPanel;
    private JList<Signal> recordingsList;
    private DefaultListModel<Signal> recordingsDefListModel;
    private JPanel symptomsCalendarPanel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Map<String, Color> colors;
    private JTable table;
    private JPanel legendPanel;
    private MyComboBox<String> monthComboBox;
    private final Patient patient;

    //TODO: implement search algorithms
    public PatientInfo(Application appMain, Patient patient) {
        this.patient = patient;
        this.appMain = appMain;
        titleText = patient.getName()+" "+patient.getSurname();
        colors = Application.symptomColors;
        initMainPanel();
        initPatientDetailsPanel();
        initRecordingsHistoryPanel();
        initSymptomsCalendarPanel();
    }

    private void initMainPanel() {
        this.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 3", "[20%]5[80%]", "[][][][]push[][][][][][]"));
        this.setBackground(Color.white);
        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(new Font("sansserif", Font.BOLD, 25));
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(icon);
        add(title, "cell 0 0 3 1, alignx left");

        //Initialize search panel
        //add(searchTitle, "cell 0 1 2 1, alignx center, grow");
        //add(searchByTextField, "cell 0 2 2 1, alignx center, grow");

        patientDetailsButton = new MyButton("DETAILS");
        patientDetailsButton.addActionListener(this);
        add(patientDetailsButton, "cell 0 1, center, gapy 10, growx");

        recordingsHistoryButton = new MyButton("RECORDINGS");
        recordingsHistoryButton.addActionListener(this);
        add(recordingsHistoryButton, "cell 0 2, center, gapy 5, growx");

        symptomsCalendarButton = new MyButton("SYMPTOMS");
        symptomsCalendarButton.addActionListener(this);
        add(symptomsCalendarButton, "cell 0 3, center, gapy 5, growx");


        goBackButton = new MyButton("BACK TO MENU", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 7, center, gapy 5, growx");
        goBackButton.setVisible(true);

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 4, left");
        errorMessage.setVisible(false);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        patientDetailsPanel = new JPanel();
        recordingsHistoryPanel = new JPanel();
        symptomsCalendarPanel = new JPanel();

        cardPanel.add(patientDetailsPanel, "Panel1");
        cardPanel.add(recordingsHistoryPanel, "Panel2");
        cardPanel.add(symptomsCalendarPanel, "Panel3");

        // Mostrar un panel:
        cardLayout.show(cardPanel, "Panel1");
        add(cardPanel, "cell 1 1, span 1 7, grow");

        //showPatients(appMain.patientMan.searchPatientsBySurname("Blanco"));
        //showDoctors(createRandomDoctors());
    }

    private void initPatientDetailsPanel() {
        patientDetailsPanel.setBackground(Color.white);
        patientDetailsPanel.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 50][grow 50]", "[][][][][][][]push"));
        patientDetailsPanel.setBorder(BorderFactory.createLineBorder(Application.turquoise, 2));
        //Patient info
        MyTextField name = new MyTextField();
        //name.setText("Jane");
        name.setText(patient.getName());
        name.setEnabled(false); //Doesnt allow editing
        MyTextField surname = new MyTextField();
        //surname.setText("Doe");
        surname.setText(patient.getSurname());
        surname.setEnabled(false);
        MyTextField email = new MyTextField();
        //email.setText("jane.doe@gmail.com");
        email.setText(patient.getEmail());
        email.setEnabled(false);
        MyTextField phoneNumber = new MyTextField();
        //phoneNumber.setText("123456789");
        phoneNumber.setText(Integer.toString(patient.getPhoneNumber()));
        phoneNumber.setEnabled(false);
        MyTextField sex = new MyTextField();
        //sex.setText("Non Binary");
        sex.setText(patient.getGender());
        sex.setEnabled(false);
        MyTextField birthDate = new MyTextField();
        //birthDate.setText("1999-11-11");
        birthDate.setText(patient.getDateOfBirth().toString());
        birthDate.setEnabled(false);

        //ROW 1
        //Name and surname
        JLabel nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        patientDetailsPanel.add(nameHeading, "cell 0 0");
        //add(nameText, "skip 1, grow");

        JLabel surnameHeading = new JLabel("Surname*");
        surnameHeading.setFont(contentFont);
        surnameHeading.setForeground(contentColor);
        patientDetailsPanel.add(surnameHeading, "grow");

        //ROW 2
        patientDetailsPanel.add(name, "grow");
        patientDetailsPanel.add(surname, "grow");

        //ROW 3
        JLabel sexHeading = new JLabel("Sex*");
        sexHeading.setFont(contentFont);
        sexHeading.setForeground(contentColor);
        patientDetailsPanel.add(sexHeading, "grow");

        JLabel birthDateHeading = new JLabel("Date of Birth*");
        birthDateHeading.setFont(contentFont);
        birthDateHeading.setForeground(contentColor);
        patientDetailsPanel.add(birthDateHeading, "grow");

        //ROW 4
        patientDetailsPanel.add(sex, "grow");
        patientDetailsPanel.add(birthDate,  "grow"); //TODO create birth date chooser

        //ROW 5
        JLabel emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        patientDetailsPanel.add(emailHeading, "grow");

        JLabel phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        patientDetailsPanel.add(phoneHeading, "grow");

        //ROW 5
        patientDetailsPanel.add(email, "grow");
        patientDetailsPanel.add(phoneNumber, "grow");

        //add(patientDetailsPanel, "cell 1 1, span 1 7, grow");

    }

    private void initRecordingsHistoryPanel() {
        recordingsHistoryPanel.setBackground(Color.white);
        recordingsHistoryPanel.setLayout(new MigLayout("fill, inset 10, gap 5", "[grow 50][grow 50]", "[5%][5%][5%][80%]"));
        recordingsHistoryPanel.setBorder(BorderFactory.createLineBorder(Application.turquoise, 2));

        //Initialize search panel
        JLabel searchTitle = new JLabel("Search By Date");
        searchTitle.setFont(titleFont);
        searchTitle.setForeground(Application.darker_purple);
        recordingsHistoryPanel.add(searchTitle, "cell 0 0 2 1, alignx center, grow");

        searchByTextField = new MyTextField();
        searchByTextField.setBackground(Application.lighter_turquoise);
        searchByTextField.setHint("YYYY-MM-DD");
        recordingsHistoryPanel.add(searchByTextField, "cell 0 1 2 1, alignx center, grow");

        searchButton = new MyButton("SEARCH");
        searchButton.addActionListener(this);
        recordingsHistoryPanel.add(searchButton, "cell 0 2, right, gapy 5, grow");

        openRecordingButton = new MyButton("OPEN FILE");
        openRecordingButton.addActionListener(this);
        recordingsHistoryPanel.add(openRecordingButton, "cell 1 2, center, gapy 5, span 2, grow");

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setOpaque(false);
        scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane1.setViewportView(gridPanel);

        recordingsDefListModel = new DefaultListModel<Signal>();
        //TODO: ask server for recordings
        ArrayList<Signal> signalRecordings = patient.getRecordings();
        if(!signalRecordings.isEmpty()) {
            for (Signal r : signalRecordings) {
                recordingsDefListModel.addElement(r);

            }
        }

        recordingsList = new JList<Signal>(recordingsDefListModel);
        recordingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordingsList.setCellRenderer(new RecordingCell());
        recordingsList.addMouseListener(this);
        scrollPane1.setViewportView(recordingsList);

        scrollPane1.setPreferredSize(this.getPreferredSize());
        recordingsHistoryPanel.add(scrollPane1, "cell 0 3, span 2 2, grow");
    }

    public void updateSignalRecordingsList(List<Signal> list){
        if(list == null || list.isEmpty()) {
            showErrorMessage("No signal found!");
            openRecordingButton.setVisible(false);
        }else{
            openRecordingButton.setVisible(true);
        }
        recordingsDefListModel.removeAllElements();
        for (Signal r : list) {
            recordingsDefListModel.addElement(r);

        }
    }

    private void initSymptomsCalendarPanel() {
        symptomsCalendarPanel.setBackground(Color.white);
        symptomsCalendarPanel.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[20%][80%]", "[5%][60%][35%]"));
        symptomsCalendarPanel.setBorder(BorderFactory.createLineBorder(Application.purple, 2));

        //
        JLabel monthHeading = new JLabel("Select a month:");
        monthHeading.setFont(titleFont);
        monthHeading.setForeground(Application.darker_purple);
        symptomsCalendarPanel.add(monthHeading, "cell 0 0, alignx center, grow");

        String[] months = Arrays.stream(Month.values())
                .map(Month::name)
                .toArray(String[]::new);
        monthComboBox = new MyComboBox<>();
        monthComboBox.setModel(new DefaultComboBoxModel<>(months));
        monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthComboBox.addActionListener(e -> updateTable(monthComboBox.getSelectedIndex() + 1));
        symptomsCalendarPanel.add(monthComboBox, "cell 1 0, alignx center, grow");
        //showPatients(appMain.patientMan.searchPatientsBySurname("Blanco"));
        //showDoctors(createRandomDoctors());

        // Initial table
        table = new JTable();
        table.setRowHeight(65);
        symptomsCalendarPanel.add(new JScrollPane(table), "cell 0 1, span 2 1, grow");

        // Legend panel (contenido desplazable)
        legendPanel = new JPanel();
        legendPanel.setBackground(Color.white);
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        // Create a white line border
        TitledBorder border = BorderFactory.createTitledBorder("Legend");
        border.setTitleFont(contentFont);
        border.setTitleColor(Application.turquoise);
        legendPanel.setBorder(border);
        //legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));

        // Añadimos los síntomas con sus colores
        for (Map.Entry<String, Color> entry : colors.entrySet()) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            item.setBackground(Color.white);

            JPanel colorBox = new JPanel();
            colorBox.setBackground(entry.getValue());
            colorBox.setPreferredSize(new Dimension(20, 20));

            JLabel label = new JLabel(" " + entry.getKey() + " ");
            item.add(colorBox);
            item.add(label);

            legendPanel.add(item);
        }

        // Crear scrollpane para la leyenda
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBackground(Color.white);
        legendScroll.setBorder(null);
        //legendScroll.setPreferredSize(new Dimension(200, 120)); // ajusta tamaño según necesites
        legendScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        legendScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Añadir el scrollpane en el layout principal
        symptomsCalendarPanel.add(legendScroll, "cell 0 2, span 2 1, grow, gapy 5");

        // Populate table for the initially selected month
        updateTable(monthComboBox.getSelectedIndex() + 1);
    }

    private void updateTable(int month) {
        int year = LocalDate.now().getYear();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        DayOfWeek firstWeekday = firstDay.getDayOfWeek();

        String[] columns = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String[][] data = new String[6][7]; // max 6 weeks

        int dayCounter = 1;
        int startCol = firstWeekday.getValue() % 7; // Sunday=0

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        outer:
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && col < startCol) continue;
                if (dayCounter > daysInMonth) break outer;

                LocalDate currentDate = LocalDate.of(year, month, dayCounter);
                StringBuilder cellText = new StringBuilder();
                cellText.append(dayCounter); // siempre mostramos el número del día

                // Buscar síntomas de este día
                StringBuilder symptomsPart = new StringBuilder();
                for (Report s : patient.getSymptoms()) {
                    LocalDate symptomDate = s.getDate();
                    if (symptomDate.equals(currentDate)) {
                        if (symptomsPart.length() > 0) symptomsPart.append(",");
                        symptomsPart.append(s.getSymptom().name()); // si es enum
                    }
                }

                if (symptomsPart.length() > 0) {
                    cellText.append(":").append(symptomsPart);
                }

                data[row][col] = cellText.toString();
                dayCounter++;
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns);
        table.setModel(model);

        // Custom cell renderer para mostrar número + cuadros de colores
        table.setDefaultRenderer(Object.class, new PatientInfo.SymptomCellRenderer(colors));
    }

    // Custom renderer
    static class SymptomCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final Map<String, Color> symptomColors;
        private JLabel dayLabel;

        public SymptomCellRenderer(Map<String, Color> symptomColors) {
            this.symptomColors = symptomColors;
            setOpaque(true);
            setLayout(new BorderLayout());
            dayLabel = new JLabel();
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 12f));
            dayLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            removeAll();
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());

            dayLabel = new JLabel();
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 12f));
            dayLabel.setHorizontalAlignment(SwingConstants.LEFT);

            if (value != null && !value.toString().isEmpty()) {
                String cellText = value.toString();
                String[] parts = cellText.split(":", 2);
                String dayPart = parts[0];
                String symptomPart = parts.length > 1 ? parts[1] : "";

                dayLabel.setText(dayPart);
                add(dayLabel, BorderLayout.NORTH);

                if (!symptomPart.isEmpty()) {
                    String[] symptoms = symptomPart.split(",");
                    JPanel symptomsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
                    for (String symptom : symptoms) {
                        JPanel box = new JPanel();
                        box.setBackground(symptomColors.getOrDefault(symptom, Color.LIGHT_GRAY));
                        box.setPreferredSize(new Dimension(15, 15));
                        symptomsPanel.add(box);
                    }
                    symptomsPanel.setBackground(Color.WHITE);
                    add(symptomsPanel, BorderLayout.CENTER);
                    setToolTipText(String.join(", ", symptoms));
                }
            }

            /*if (isSelected) {
                setBackground(new Color(200, 220, 255));
            }*/

            return this;
        }
    }

    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    private void hideErrorMessage() {
        errorMessage.setVisible(false);
    }


    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            appMain.changeToMainMenuAndRemove(this);
            //appMain.changeToMainMenu();
        }else if(e.getSource() == patientDetailsButton){
            cardLayout.show(cardPanel, "Panel1");
        }else if(e.getSource() == recordingsHistoryButton){
            cardLayout.show(cardPanel, "Panel2");
        }else if(e.getSource() == symptomsCalendarButton){
            cardLayout.show(cardPanel, "Panel3");
        }else if(e.getSource() == openRecordingButton){
            Signal signal = recordingsList.getSelectedValue();
            if(signal == null) {
                showErrorMessage("No signal Selected");
            }else {
                //showErrorMessage("Selected signal: " + patient.getName()+" "+patient.getSurname());
                //TODO: request real signal to the server si no se ha pedido ya
                //signal.setEcg(Application.importECG());
                //signal.setAcc(Application.importACC());
                //signal.setFrequency(100);
                try {
                    Signal temp = ECGFileReader.readSignalFromFile("C:/Users/mamen/Documents/OpenSignals (r)evolution/files/ecg-acc-mamen-11-07_12-12-54.txt");
                    signal.setEcg(temp.getEcg());
                    signal.setAcc(temp.getAcc());
                    signal.setFrequency(temp.getFrequency());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                //resetPanel();
                appMain.changeToPanel(new RecordingGraphs(appMain, this, signal, patient));
                //appMain.changeToAdmitPatient(patient);
            }
        }if(e.getSource() == searchButton) {
            errorMessage.setVisible(false);
            String input = searchByTextField.getText();
            System.out.println(input);
            List<Signal> filteredRecordings = patient.getRecordings().stream()
                    .filter(p -> p.getDate().toString().contains(input))
                    .collect(Collectors.toList());

            updateSignalRecordingsList(filteredRecordings);
            if(filteredRecordings.isEmpty()) {
                showErrorMessage("No Signals found");
                openRecordingButton.setVisible(false);
            }else {
                openRecordingButton.setVisible(true);
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
