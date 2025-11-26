package ui.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.stream.Collectors;
import net.miginfocom.swing.MigLayout;
import pojos.Patient;
import ui.components.MyButton;
import ui.components.MyTextField;
import ui.components.PatientCell;
import javax.swing.*;

/**
 * Panel that allows the doctor to browse and search through their list of patients.
 * <p>
 * This view is created once by {@code MainMenu} and reused. It supports:
 * <ul>
 *     <li>Displaying all assigned patients</li>
 *     <li>Filtering patients by surname</li>
 *     <li>Opening a patient's detailed record</li>
 * </ul>
 * </p>
 *
 * <h3>Lifecycle and Reuse</h3>
 * <ul>
 *     <li>The panel is persistent and does not recreate its components.</li>
 *     <li>Before being shown, {@link #updatePatientDefModel(List)} is always called
 *         to refresh the displayed patient list.</li>
 *     <li>When returning to the main menu, {@link #resetPanel()} clears filters,
 *         search text, and list contents.</li>
 * </ul>
 *
 *  @author MamenCortes
 */
public class SearchPatients extends JPanel implements ActionListener, MouseListener {

    private static final long serialVersionUID = -2213334704230710767L;
    private Application appMain;
    protected final Font titleFont = new Font("sansserif", 3, 15);
    protected final Color titleColor = Application.dark_purple;
    protected JLabel title;
    protected String titleText = " Search Patients ";
    protected ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/patient-info64-2.png"));
    protected JScrollPane scrollPane1;
    protected String searchText = "Search By Surname";
    protected MyTextField searchByTextField;
    protected MyButton searchButton;
    protected MyButton resetListButton;
    protected MyButton openFormButton;
    protected JLabel errorMessage;
    protected MyButton goBackButton;
    protected JList<Patient> patientJList;
    protected DefaultListModel<Patient> patientsDefListModel;
    protected List<Patient> allPatients;

    /**
     * Constructs the search panel and initializes UI components.
     *
     * @param appMain reference to the central {@link Application} controller,
     *                used for data access and panel navigation.
     */
    public SearchPatients(Application appMain) {
        this.appMain = appMain;
        initMainPanel();
    }

    /**
     * Initializes the main layout, search controls, patient list,
     * and the buttons for resetting, searching, opening files,
     * and returning to the main menu.
     * <p>
     * This method only initializes UI structure. Patient data is loaded later
     * through {@link #updatePatientDefModel(List)}.
     * </p>
     */
    private void initMainPanel() {
        this.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 3", "[grow 5]5[grow 5]5[grow 40][grow 40]", "[][][][][][][][][][]"));
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
        JLabel searchTitle = new JLabel(searchText);
        searchTitle.setFont(titleFont);
        searchTitle.setForeground(Application.darker_purple);
        add(searchTitle, "cell 0 1 2 1, alignx center, grow");

        searchByTextField = new MyTextField("ex. Doe...");
        searchByTextField.setBackground(Application.lighter_turquoise);
        searchByTextField.setHint("ex. Doe");
        add(searchByTextField, "cell 0 2 2 1, alignx center, grow");

        resetListButton = new MyButton("RESET");
        resetListButton.addActionListener(this);
        add(resetListButton, "cell 0 3, left, gapy 5, grow");

        searchButton = new MyButton("SEARCH");
        searchButton.addActionListener(this);
        add(searchButton, "cell 1 3, right, gapy 5, grow");

        openFormButton = new MyButton("OPEN FILE");
        openFormButton.addActionListener(this);
        add(openFormButton, "cell 0 4, center, gapy 5, span 2, grow");
        openFormButton.setVisible(true);

        goBackButton = new MyButton("BACK TO MENU", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 7, center, gapy 5, span 2, grow");
        goBackButton.setVisible(true);

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 5, span 2, left");
        errorMessage.setVisible(false);

        scrollPane1 = new JScrollPane();
        scrollPane1.setOpaque(false);
        scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        patientsDefListModel = new DefaultListModel<Patient>();
        patientJList = new JList<Patient>(patientsDefListModel);
        patientJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientJList.setCellRenderer(new PatientCell());
        patientJList.addMouseListener(this);
        scrollPane1.setViewportView(patientJList);

        scrollPane1.setPreferredSize(this.getPreferredSize());

        add(scrollPane1,  "cell 2 1 2 6, grow, gap 10");
    }

    /**
     * Updates the patient list with the provided set of patients.
     * <p>
     * This method is called every time before showing the panel to ensure
     * that the displayed list reflects the latest data received from the server.
     * </p>
     * <p>
     * If the list is empty, an error message is shown and the
     * "OPEN FILE" button is hidden.
     * </p>
     *
     * @param patients list of patients assigned to the doctor.
     */
    protected void updatePatientDefModel(List<Patient> patients) {
        if(patients == null || patients.isEmpty()) {
            showErrorMessage("No patients found!");
            openFormButton.setVisible(false);
        }else{
            if(allPatients == null) {
                allPatients = patients;
            }
            openFormButton.setVisible(true);
        }

        patientsDefListModel.removeAllElements();
        for (Patient r : patients) {
            patientsDefListModel.addElement(r);

        }
    }

    /**
     * Displays an error message in the panel.
     *
     * @param message the message text to display.
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
     * Resets the panel to its initial state.
     * <p>
     * This is invoked when navigating back to the main menu and clears:
     * <ul>
     *     <li>search text</li>
     *     <li>stored patient list reference</li>
     *     <li>displayed patient list</li>
     * </ul>
     * </p>
     */
    private void resetPanel(){
        hideErrorMessage();
        searchByTextField.setText("");
        allPatients = null;
        patientsDefListModel.clear();
    }

    /**
     * Handles button interactions:
     * <ul>
     *     <li><b>BACK TO MENU:</b> Resets the panel and returns to main menu.</li>
     *     <li><b>OPEN FILE:</b> Opens the selected patient's information panel.</li>
     *     <li><b>SEARCH:</b> Filters the patient list by surname.</li>
     *     <li><b>RESET:</b> Restores the original patient list.</li>
     * </ul>
     *
     * @param e the triggered action event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == goBackButton) {
            resetPanel();
            appMain.changeToMainMenu();
        }else if(e.getSource() == openFormButton){
            Patient patient = patientJList.getSelectedValue();
            if(patient == null) {
                showErrorMessage("No patient Selected");
            }else {
                showErrorMessage("Selected patient: " + patient.getName()+" "+patient.getSurname());
                resetPanel();
                appMain.changeToPanel(new PatientInfo(appMain, patient));
            }
        }if(e.getSource() == searchButton) {
            if(allPatients.isEmpty()) {
                showErrorMessage("No patients found!");
                return;
            }
            errorMessage.setVisible(false);
            String input = searchByTextField.getText();
            System.out.println(input);
            String search = searchByTextField.getText().trim().toLowerCase();

            List<Patient> filteredPatients = allPatients.stream()
                    .filter(p -> p.getSurname().toLowerCase().contains(search))
                    .collect(Collectors.toList());

            updatePatientDefModel(filteredPatients);
            if(filteredPatients.isEmpty()) {
                showErrorMessage("No patient found");
                openFormButton.setVisible(false);
            }else {
                openFormButton.setVisible(true);
            }

        }else if(e.getSource() == resetListButton){
            updatePatientDefModel(allPatients);
            if(allPatients.isEmpty()) {
                showErrorMessage("No patient found");
                openFormButton.setVisible(false);
            }else {
                openFormButton.setVisible(true);
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
