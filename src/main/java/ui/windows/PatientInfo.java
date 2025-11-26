package ui.windows;

import net.miginfocom.swing.MigLayout;
import pojos.SymptomType;
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

/**
 * Panel that displays all information related to a specific patient.
 * <p>
 * This view is created <b>each time a patient is selected</b> in
 * {@link SearchPatients}. It includes three subviews accessed via a
 * {@link CardLayout}:
 * </p>
 *
 * <h3>Subpanels</h3>
 * <ul>
 *     <li><b>Patient details:</b> personal and demographic data</li>
 *     <li><b>Recordings history:</b> list of ECG/ACC recordings with access to each</li>
 *     <li><b>Symptoms calendar:</b> monthly calendar showing symptom occurrences</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>A fresh instance is created when the doctor opens a patient's file.</li>
 *     <li>When navigating back to the main menu, the panel is removed and discarded.</li>
 *     <li>Recordings list and symptoms table are initialized during construction.</li>
 *     <li>Recordings may be refreshed through {@link #updateSignalRecordingsList(List)}.</li>
 * </ul>
 *
 * <h3>Navigation</h3>
 * <ul>
 *     <li>“DETAILS” switches to the patient information card.</li>
 *     <li>“RECORDINGS” switches to the recordings history card.</li>
 *     <li>“SYMPTOMS” opens the symptoms calendar subview.</li>
 *     <li>“BACK TO MENU” removes this panel and returns to the main menu.</li>
 * </ul>
 *
 * @author MamenCortes
 * @author MartaSanchezdelHoyo
 */
public class PatientInfo extends JPanel implements ActionListener, MouseListener {
    private static final long serialVersionUID = -2213334704230710767L;
    private Application appMain;
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
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

    /**
     * Constructs the patient information panel for a given patient.
     * <p>
     * The constructor initializes:
     * <ul>
     *     <li>The title with the patient's full name</li>
     *     <li>Three subpanels (details, recordings, symptoms)</li>
     *     <li>The card layout used to switch between these subviews</li>
     * </ul>
     * Patient-specific data (recordings, symptoms) is retrieved from the
     * {@link Application#client} during the initialization of subpanels.
     *
     * @param appMain the central {@link Application} controller
     * @param patient the patient whose information will be displayed
     */
    public PatientInfo(Application appMain, Patient patient) {
        this.patient = patient;
        this.appMain = appMain;
        titleText = patient.getName()+" "+patient.getSurname();
        colors = Application.symptomColors;
        System.out.println(patient.getSymptoms().toString());
        initMainPanel();
        initPatientDetailsPanel();
        initRecordingsHistoryPanel();
        initSymptomsCalendarPanel();
    }

    /**
     * Initializes the main container panel including:
     * <ul>
     *     <li>the title with patient name</li>
     *     <li>the navigation buttons</li>
     *     <li>the card layout container where subpanels are displayed</li>
     * </ul>
     */
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

        //Show the first panel:
        cardLayout.show(cardPanel, "Panel1");
        add(cardPanel, "cell 1 1, span 1 7, grow");
    }

    /**
     * Initializes the patient details subpanel displaying demographic fields
     * such as name, surname, date of birth, email, gender, and phone number.
     * <p>
     * All fields are non-editable and reflect the information stored in the
     * provided {@link Patient} instance.
     * </p>
     */
    private void initPatientDetailsPanel() {
        patientDetailsPanel.setBackground(Color.white);
        patientDetailsPanel.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 50][grow 50]", "[][][][][][][]push"));
        patientDetailsPanel.setBorder(BorderFactory.createLineBorder(Application.turquoise, 2));

        MyTextField name = new MyTextField();
        name.setText(patient.getName());
        name.setEnabled(false); //Doesnt allow editing
        MyTextField surname = new MyTextField();
        surname.setText(patient.getSurname());
        surname.setEnabled(false);
        MyTextField email = new MyTextField();
        email.setText(patient.getEmail());
        email.setEnabled(false);
        MyTextField phoneNumber = new MyTextField();
        phoneNumber.setText(Integer.toString(patient.getPhoneNumber()));
        phoneNumber.setEnabled(false);
        MyTextField sex = new MyTextField();
        sex.setText(patient.getGender());
        sex.setEnabled(false);
        MyTextField birthDate = new MyTextField();
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

    }

    /**
     * Initializes the recordings history subpanel which displays:
     * <ul>
     *     <li>The list of all signal recordings for the patient</li>
     *     <li>A search field to filter recordings by date</li>
     *     <li>A button to open a selected recording</li>
     * </ul>
     * <p>
     * Recordings are retrieved from the patient
     * {@link Application#client#getAllSignalsFromPatient(int)}.
     * </p>
     */
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

        recordingsDefListModel = new DefaultListModel<Signal>();
        //TODO: ask server for recordings
        List<Signal> signalRecordings = patient.getRecordings();
        /*try {
            signalRecordings = appMain.client.getAllSignalsFromPatient(patient.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading recordings from server",
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        }*/

        // === POPULATE LIST ===
        if (!signalRecordings.isEmpty()) {
            for (Signal s : signalRecordings) {
                recordingsDefListModel.addElement(s);
            }
        }else{
            showErrorMessage("No signal recordings found!");
        }
        recordingsList = new JList<Signal>(recordingsDefListModel);
        recordingsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordingsList.setCellRenderer(new RecordingCell());
        recordingsList.addMouseListener(this);
        scrollPane1.setViewportView(recordingsList);

        scrollPane1.setPreferredSize(this.getPreferredSize());
        recordingsHistoryPanel.add(scrollPane1, "cell 0 3, span 2 2, grow");
    }

    /**
     * Updates the recordings list with the provided signals.
     * <p>
     * This method is used when filtering the recordings by date.
     * If the list is empty, an error message is shown and the "OPEN FILE"
     * button is hidden.
     * </p>
     *
     * @param list list of signals matching the current filter
     */
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

    /**
     * Initializes the symptoms calendar subpanel, which displays a calendar
     * view of the current month and highlights days on which the patient
     * has reported symptoms.
     * <p>
     * The calendar is generated dynamically each month using {@link #updateTable(int)}.
     * </p>
     */
    private void initSymptomsCalendarPanel() {
        symptomsCalendarPanel.setBackground(Color.white);
        symptomsCalendarPanel.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[20%][80%]", "[5%][60%][35%]"));
        symptomsCalendarPanel.setBorder(BorderFactory.createLineBorder(Application.purple, 2));

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

        // Add color-coded symptoms
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

        // Create scrollpanel for the legend and add to main panel
        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBackground(Color.white);
        legendScroll.setBorder(null);
        legendScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        legendScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        symptomsCalendarPanel.add(legendScroll, "cell 0 2, span 2 1, grow, gapy 5");

        // Populate table for the initially selected month
        updateTable(monthComboBox.getSelectedIndex() + 1);
    }

    /**
     * Updates the symptoms calendar table for a given month.
     * <p>
     * Each day cell shows:
     * <ul>
     *     <li>The day number</li>
     *     <li>Colored boxes representing symptom types reported that day</li>
     * </ul>
     * Colors are derived from {@link Application#symptomColors}.
     *
     * @param month the month index (1–12)
     */
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
                        for (SymptomType type : s.getSymptoms()) {
                            if (symptomsPart.length() > 0) symptomsPart.append(",");
                            symptomsPart.append(type.name());
                        }
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

    /**
     * Renderer used to display colored symptom indicators inside
     * the calendar table cells.
     */
    private static class SymptomCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
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

            return this;
        }
    }

    /**
     * Displays an error message inside the panel.
     *
     * @param message the text to display
     */
    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }

    /**
     * Hides any previously displayed error message.
     */
    private void hideErrorMessage() {
        errorMessage.setVisible(false);
    }

    /**
     * Handles logic when selecting a recording:
     * <ul>
     *     <li>Validates a recording is selected</li>
     *     <li>Loads full ECG/ACC data if required</li>
     *     <li>Opens a new {@link RecordingGraphs} panel</li>
     * </ul>
     */
    private void hanleOpenRecording(){}

    /**
     * Handles all button interactions:
     * <ul>
     *     <li><b>BACK TO MENU:</b> removes this panel and returns to the main menu</li>
     *     <li><b>DETAILS:</b> switches to the patient details subpanel</li>
     *     <li><b>RECORDINGS:</b> switches to the recordings history subpanel</li>
     *     <li><b>SYMPTOMS:</b> switches to the symptoms calendar subpanel</li>
     *     <li><b>OPEN FILE:</b> loads the full recording and opens {@link RecordingGraphs}</li>
     *     <li><b>SEARCH:</b> filters recordings by date</li>
     * </ul>
     *
     * @param e the action event triggered by the user
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            appMain.changeToMainMenuAndRemove(this);
        }else if(e.getSource() == patientDetailsButton){
            cardLayout.show(cardPanel, "Panel1");
        }else if(e.getSource() == recordingsHistoryButton){
            cardLayout.show(cardPanel, "Panel2");
        }else if(e.getSource() == symptomsCalendarButton){
            cardLayout.show(cardPanel, "Panel3");
        }else if(e.getSource() == searchButton) {
            if(patient.getRecordings().isEmpty()) {
                showErrorMessage("No patient recordings found!");
                return;
            }
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
        }else if(e.getSource() == openRecordingButton){
            Signal signal = recordingsList.getSelectedValue();
            if(signal == null) {
                showErrorMessage("No signal Selected");
                return;
            }
            //TODO ventana de espera cuando se esta descargando el file
           // image.setIcon(uploadingGif);
            //showFeedbackMessage(errorMessage2,"Downloading signal from server...");
            SwingWorker<Signal, Void> worker = new SwingWorker<>() {
                @Override
                protected Signal doInBackground() throws Exception {
                    System.out.println("📥 Entré en doInBackground");
                    // 1) Pedir señal completa al servidor (con ZIP)
                    Signal fullSignal = null;
                    try {
                        fullSignal = appMain.client.getSignalFromId(signal.getId());
                    } catch (IOException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("📤 Señal recibida del servidor");
                    // 2) Leer el contenido real del ZIP temporal
                    Signal temp = ECGFileReader.readSignalFromZip(fullSignal.getZipFile(), signal.getFrequency());
                    fullSignal.setEcg(temp.getEcg());
                    fullSignal.setAcc(temp.getAcc());
                    fullSignal.setFrequency(temp.getFrequency());
                    return fullSignal;
                }

                @Override
                protected void done() {
                    try {
                        Signal fullSignal = get();
                        //TODO quitar la ventana de descarga cuando se haya descargado finalmente
                       // image.setIcon(null);
                       // showFeedbackMessage(errorMessage2, "Signal ready!");
                        // TODO enseñar la señal en una nueva ventana
                        appMain.changeToPanel(new RecordingGraphs(appMain, PatientInfo.this , fullSignal,patient));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showErrorMessage("Error downloading or reading signal");
                        return; // evitar ventana vacía
                    }

                }

            }; worker.execute();
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
