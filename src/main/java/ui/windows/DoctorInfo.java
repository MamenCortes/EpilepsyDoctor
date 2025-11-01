package ui.windows;
import net.miginfocom.swing.MigLayout;
import pojos.Doctor;
import ui.components.MyButton;
import ui.components.MyComboBox;
import ui.components.MyTextField;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DoctorInfo extends JPanel implements ActionListener {
    private Application appMain;
    private JLabel nameHeading;
    private MyTextField name;
    private JLabel emailHeading;
    private MyTextField email;
    private JLabel phoneHeading;
    private MyTextField phoneNumber;
    private JLabel specHeading;
    private MyTextField speciality;
    private MyComboBox<String> nextStep;
    private JLabel officeHeading;
    private MyTextField office;

    private JLabel title;
    protected String titleText = " ";
    //protected JButton applyChanges;
    protected JButton goBackButton;
    protected JLabel errorMessage;
    protected JPanel formContainer;


    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;

    private Doctor doctor;

    //private JDateChooser birthDate;
    public DoctorInfo(Application appMain, Doctor doctor) {
        this.appMain = appMain;
        this.doctor = doctor;
        System.out.println(doctor);
        initDoctorInfo();

    }

    public void initDoctorInfo() {
        this.titleText = "Physician information";

        //Initialize values
        //TODO: replace with actual doctor values
        name = new MyTextField();
        //name.setText("Dr. Michal Al-Hajjar");
        name.setText(doctor.getName()+" "+doctor.getSurname());
        name.setEnabled(false); //Doesnt allow editing
        email = new MyTextField();
        email.setText(doctor.getEmail());
        email.setEnabled(false);
        phoneNumber = new MyTextField();
        phoneNumber.setText(doctor.getPhoneNumber().toString());
        phoneNumber.setEnabled(false);
        speciality = new MyTextField();
        speciality.setText(doctor.getSpeciality());
        speciality.setEnabled(false);
        office = new MyTextField();
        office.setText(doctor.getDepartment());
        office.setEnabled(false);
        formContainer = new JPanel();
        initDoctorForm();
    }

    private void initDoctorForm() {
        //this.setLayout(new MigLayout("fill, inset 15, gap 0, wrap 4, debug", "[][][][]", "[][][][][][][][][][]"));
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        //this.setBackground(Application.light_purple);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 10][grow 90]", "[][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/doctor-info64_2.png")));
        add(title, "cell 0 0 4 1, alignx left");

        //add(formContainer,  "cell 0 1 4 8, grow, gap 10");
        //place in column 0, row 1, expand to 4 columns and 8 rows. Gap 10px left and right
        add(formContainer, "cell 0 1 4 8, grow, gap 10 10");

        //add(title1, "cell 0 0, grow");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        //formContainer.add(nameHeading, "cell 0 0");
        formContainer.add(nameHeading, "grow");

        //ROW 2
        formContainer.add(name, "grow");

        //ROW 3
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");
        //add(nameText, "skip 1, grow");

        //ROW 4
        formContainer.add(email, "grow");

        //ROW 5
        specHeading = new JLabel("Speciality*");
        specHeading.setFont(contentFont);
        specHeading.setForeground(contentColor);
        formContainer.add(specHeading, "grow");
        //ROW 6
        formContainer.add(speciality, "grow");


        //ROW 7
        officeHeading = new JLabel("Office*");
        officeHeading.setFont(contentFont);
        officeHeading.setForeground(contentColor);
        formContainer.add(officeHeading, "grow");

        //ROW 8
        formContainer.add(office, "grow"); //TODO create birth date chooser

        //Add buttons
        goBackButton = new MyButton("GO BACK", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        //add(goBackButton,"cell 1 7, left, gapx 10, gapy 5");
        add(goBackButton, "cell 0 9, span, center");

        //applyChanges = new MyButton("APPLY");
        //applyChanges.addActionListener(this);

        /*errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        //this.add(errorMessage, "cell 0 8, span, left");
        this.add(errorMessage, "cell 0 1, span, center");
        errorMessage.setVisible(true);*/

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            appMain.changeToMainMenu();
        }
    }
}