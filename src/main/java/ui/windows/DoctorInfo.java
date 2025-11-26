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

/**
 * Panel that displays detailed information about the currently logged-in doctor.
 * <p>
 * This view is non-editable and is used strictly for displaying profile data.
 * It is reused across the application's lifecycle. Whenever the panel becomes
 * visible again, the controller must call {@link #updateView(Doctor)} to refresh
 * displayed values.
 * </p>
 *
 * <h3>Lifecycle and Reuse</h3>
 * <ul>
 *     <li>The panel is instantiated once in {@code MainMenu}.</li>
 *     <li>When navigated to, {@code updateView()} is always invoked to reload
 *         the doctor’s information.</li>
 *     <li>When navigating back to the main menu, {@link #resetView()} clears
 *         all displayed fields.</li>
 * </ul>
 *
 *  @author MamenCortes
 */
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
    protected JButton goBackButton;
    protected JLabel errorMessage;
    protected JPanel formContainer;


    //Format variables: Color and Font
    private final Color titleColor = Application.dark_purple;
    private final Font titleFont = new Font("sansserif", Font.BOLD, 25);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = Application.dark_turquoise;

    /**
     * Creates the doctor information panel, initializes layout and UI components.
     * <p>
     * Actual values are not set at construction time; instead they are assigned
     * when {@link #updateView(Doctor)} is called.
     * </p>
     *
     * @param appMain reference to the {@link Application} controller used for
     *                navigation and data access.
     */
    public DoctorInfo(Application appMain) {
        this.appMain = appMain;
        initDoctorInfo();

    }
    /**
     * Initializes the panel structure and fields for displaying doctor data.
     * <p>
     * This method only builds the UI; values remain empty until a doctor is
     * provided via {@link #updateView(Doctor)}.
     * </p>
     */
    public void initDoctorInfo() {
        this.titleText = "Physician information";

        //Initialize variables
        name = new MyTextField();
        name.setEnabled(false); //Doesnt allow editing
        email = new MyTextField();
        email.setEnabled(false);
        phoneNumber = new MyTextField();
        phoneNumber.setEnabled(false);
        speciality = new MyTextField();
        speciality.setEnabled(false);
        office = new MyTextField();
        office.setEnabled(false);
        formContainer = new JPanel();
        initDoctorForm();
    }

    /**
     * Initializes the form layout and UI components for displaying doctor data:
     * name, email, phone number, specialty, and office.
     */
    private void initDoctorForm() {
        this.setLayout(new MigLayout("fill", "[][][][]", "[][][][][][][][][][]"));
        this.setBackground(Color.white);
        formContainer.setBackground(Color.white);
        formContainer.setLayout(new MigLayout("fill, inset 10, gap 5, wrap 2", "[grow 10][grow 90]", "[][][][][]push"));

        //Add Title
        title = new JLabel(titleText);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(titleColor);
        title.setFont(titleFont);
        title.setAlignmentY(LEFT_ALIGNMENT);
        title.setIcon(new ImageIcon(getClass().getResource("/icons/doctor-info64_2.png")));
        add(title, "cell 0 0 4 1, alignx left");
        add(formContainer, "cell 0 1 4 8, grow, gap 10 10");

        //ROW 1
        //Name and surname
        nameHeading = new JLabel("Name and Surname:*");
        nameHeading.setFont(contentFont);
        nameHeading.setForeground(contentColor);
        formContainer.add(nameHeading, "grow");

        //ROW 2
        formContainer.add(name, "grow");

        //ROW 3
        emailHeading = new JLabel("Email*");
        emailHeading.setFont(contentFont);
        emailHeading.setForeground(contentColor);
        formContainer.add(emailHeading, "grow");
        formContainer.add(email, "grow");

        //ROW 4
        phoneHeading = new JLabel("Phone Number*");
        phoneHeading.setFont(contentFont);
        phoneHeading.setForeground(contentColor);
        formContainer.add(phoneHeading, "grow");
        formContainer.add(phoneNumber, "grow");

        //ROW 5
        specHeading = new JLabel("Speciality*");
        specHeading.setFont(contentFont);
        specHeading.setForeground(contentColor);
        formContainer.add(specHeading, "grow");
        formContainer.add(speciality, "grow");

        //ROW 7
        officeHeading = new JLabel("Hospital/Department*");
        officeHeading.setFont(contentFont);
        officeHeading.setForeground(contentColor);
        formContainer.add(officeHeading, "grow");
        formContainer.add(office, "grow"); //TODO create birth date chooser

        //Add buttons
        goBackButton = new MyButton("GO BACK", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 9, span, center");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            resetView();
            appMain.changeToMainMenu();
        }
    }

    /**
     * Clears all doctor information fields.
     * <p>
     * This is called automatically when navigating back to the main menu to
     * ensure the panel does not retain outdated data.
     * </p>
     */
    private void resetView() {
        name.setText("");
        email.setText("");
        phoneNumber.setText("");
        speciality.setText("");
        office.setText("");
    }

    /**
     * Updates the panel fields with the provided doctor information.
     * <p>
     * This method must be called every time the panel becomes visible to ensure
     * correct and up-to-date data is displayed.
     * </p>
     *
     * @param doctor the doctor whose information will be displayed.
     */
    public void updateView(Doctor doctor) {
        name.setText(doctor.getName()+" "+doctor.getSurname());
        email.setText(doctor.getEmail());
        phoneNumber.setText(doctor.getPhoneNumber().toString());
        speciality.setText(doctor.getSpeciality());
        office.setText(doctor.getDepartment());
    }
}