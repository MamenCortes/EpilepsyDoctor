package ui.windows;

import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Main navigation panel shown after a successful login.
 * <p>
 * This panel provides entry points to the main application features:
 * viewing doctor details, searching patients, and logging out. The
 * {@code MainMenu} is created once and reused throughout the application.
 * </p>
 *
 * <h3>Lifecycle and Reuse</h3>
 * <ul>
 *     <li>This panel is persistent and not rebuilt when revisited.</li>
 *     <li>No reset is required when returning from other panels.</li>
 *     <li>When navigating to another panel, the associated panel is updated
 *         before being shown (e.g., {@code DoctorInfo.updateView}).</li>
 * </ul>
 *
 *  @author MamenCortes
 */
public class MainMenu extends MenuTemplate {
    private static final long serialVersionUID = 6050014345831062858L;
    private  ImageIcon logoIcon;
    private JButton searchPatientsBt;
    private JButton seeDoctorInfoBt;
    private JButton logOutBt;
    private Application appMenu;
    private String company_name;
    private DoctorInfo doctorInfoPanel;
    private SearchPatients searchPatientsPanel;

    /**
     * Constructs the main menu and initializes all subpanels and buttons.
     *
     * @param appMenu reference to the {@link Application} controller used to
     *                trigger panel changes and access shared data.
     */
    public MainMenu(Application appMenu) {
        //Initialize subpanels and buttons
        this.appMenu = appMenu;
        doctorInfoPanel = new DoctorInfo(appMenu);
        searchPatientsPanel = new SearchPatients(appMenu);
        addButtons();

        //Set company name and logo
        company_name = "NIGHT GUARDIAN: EPILEPSY";
        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));

        //Init panel
        this.init(logoIcon, company_name);
    }

    /**
     * Initializes and adds the navigation buttons to the panel.
     * <p>
     * This method sets labels and default colors but does not attach listeners
     * (listeners are inherited from {@code MenuTemplate}).
     * </p>
     */
    private void addButtons() {
        searchPatientsBt = new MyButton("Search Patients");
        seeDoctorInfoBt = new MyButton("See My Details");
        logOutBt = new MyButton("Log Out");
        buttons.add(seeDoctorInfoBt);
        buttons.add(searchPatientsBt);
        buttons.add(logOutBt);
    }

    /**
     * Handles button actions for navigating to other panels or logging out.
     * <p>
     * Actions:
     * <ul>
     *     <li><b>See My Details:</b> Updates doctor info panel and displays it.</li>
     *     <li><b>Search Patients:</b> Loads doctor's patients (if not already loaded),
     *         updates the list, and displays the search panel.</li>
     *     <li><b>Log Out:</b> Clears session data and returns to the login screen.</li>
     * </ul>
     * </p>
     *
     * @param e the action event triggered by a button press.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorInfoBt) {
            doctorInfoPanel.updateView(appMenu.doctor);
            appMenu.changeToPanel(doctorInfoPanel);
        }else if(e.getSource()== searchPatientsBt) {
            if(appMenu.doctor.getPatients().isEmpty()) {
                try {
                    appMenu.doctor.setPatients(appMenu.client.getPatientsFromDoctor(appMenu.doctor.getId()));
                } catch (IOException | InterruptedException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            searchPatientsPanel.updatePatientDefModel(appMenu.doctor.getPatients());
            appMenu.changeToPanel(searchPatientsPanel);
        }else if(e.getSource()== logOutBt) {
            appMenu.doctor = null;
            appMenu.user = null;
            appMenu.changeToUserLogIn();
        }

    }
}
