package ui.windows;

import ui.components.MenuTemplate;
import ui.components.MyButton;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

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

    public MainMenu(Application appMenu) {
        //super();
        this.appMenu = appMenu;
        doctorInfoPanel = new DoctorInfo(appMenu);
        searchPatientsPanel = new SearchPatients(appMenu);

        addButtons();
        company_name = "NIGHT GUARDIAN: EPILEPSY";
        //company_name = "<html>NIGHT GUARDIAN<br>EPILEPSY</html>";
        //company_name ="<html><div style='text-align: center;'>NIGHT GUARDIAN<br>EPILEPSY</div></html>";

        logoIcon = new ImageIcon(getClass().getResource("/icons/night_guardian_mini_128.png"));
        this.init(logoIcon, company_name);
    }

    private void addButtons() {
        //Default color: light purple
        searchPatientsBt = new MyButton("Search Patients");
        seeDoctorInfoBt = new MyButton("See My Details");
        logOutBt = new MyButton("Log Out");

        buttons.add(seeDoctorInfoBt);
        buttons.add(searchPatientsBt);
        buttons.add(logOutBt);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()== seeDoctorInfoBt) {
            //appMenu.changeToAddPatient();
            doctorInfoPanel.updateView(appMenu.doctor);
            appMenu.changeToPanel(doctorInfoPanel);
        }else if(e.getSource()== searchPatientsBt) {
            //appMenu.changeToSearchPatient();
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
