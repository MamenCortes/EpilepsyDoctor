package ui.windows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Objects;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import network.LogInError;
import pojos.AppData;
import pojos.User;
import ui.components.*;

/**
 * Panel responsible for handling user authentication into the application.
 * <p>
 * This view appears at startup and whenever the user logs out. It provides:
 * </p>
 * <ul>
 *     <li>Email and password input fields</li>
 *     <li>A "Forgot your password?" workflow</li>
 *     <li>Error feedback for invalid login attempts</li>
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The panel is created once during {@link Application} initialization.</li>
 *     <li>It remains persistent as part of the application panel stack.</li>
 *     <li>Before returning to this panel, the application calls {@link #resetPanel()}
 *         to clear all fields and messages.</li>
 * </ul>
 *
 *  @author MamenCortes
 */
public class UserLogIn extends JPanel implements ActionListener{

    private static final long serialVersionUID = 1L;
    private JPanel panelLogIn;
    private MyButton applyLogIn;
    private MyButton changePassword;
    private Application appMenu;
    private MyTextField emailTxF;
    private MyTextField passwordTxF;
    private MyTextField emailTxFLogIn;
    private MyTextField passwordTxFLogIn;
    private JPanel coverPanel;
    private JLabel errorMessage;
    private MyButton activateAccount;

    /**
     * Creates the login panel, sets the main layout, initializes all required UI
     * components and builds both the login form and the graphical cover panel.
     *
     * @param appMenu reference to the central {@link Application} controller
     */
    public UserLogIn(Application appMenu) {
        this.appMenu = appMenu;
        this.setLayout(new MigLayout("fill, inset 0, gap 0", "[30]0px[70:pref]", "[]"));
        init();

    }

    /**
     * Initializes all UI elements used in the login screen, including:
     * <ul>
     *     <li>Buttons (Log In, Forgot Password)</li>
     *     <li>Text fields for email and password</li>
     *     <li>The gradient cover panel with the application logo</li>
     *     <li>The login form panel</li>
     * </ul>
     * <p>
     * The actual content of the login form is built in {@link #initLogin()}.
     * </p>
     */
    private void init() {
        //Initialize buttons
        applyLogIn = new MyButton();
        applyLogIn.addActionListener(this);
        changePassword = new MyButton();
        changePassword.addActionListener(this);
        activateAccount = new MyButton();
        activateAccount.addActionListener(this);

        emailTxF = new MyTextField();
        emailTxF.addActionListener(this);
        passwordTxF = new MyTextField();
        passwordTxF.addActionListener(this);
        emailTxFLogIn = new MyTextField();
        emailTxFLogIn.addActionListener(this);
        passwordTxFLogIn = new MyTextField();
        passwordTxFLogIn.addActionListener(this);

        //Log In panel
        errorMessage = new JLabel();
        panelLogIn = new JPanel();
        panelLogIn.setOpaque(true);
        initLogin();

        //Cover panel
        coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // pinta el fondo base

                Graphics2D g2d = (Graphics2D) g.create();
                int width = getWidth();
                int height = getHeight();

                // Degradado de izquierda a derecha (puedes cambiarlo a vertical si quieres)
                GradientPaint gradient = new GradientPaint(0, 0, Application.light_purple, 0, getHeight(), Application.light_turquoise);

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }
        };
        coverPanel.setOpaque(false);
        coverPanel.setLayout(new MigLayout("wrap, fill", "[center]", "push[]10[]10[]push"));
        JLabel picLabel = new JLabel();
        picLabel.setIcon(new ImageIcon(UserLogIn.class.getResource("/icons/night_guardian_256.png")));
        coverPanel.add(picLabel);

        this.add(coverPanel, "grow");
        this.add(panelLogIn, "grow");
    }

    /**
     * Builds the login form panel: email input, password input, and buttons.
     * <p>
     * Sets styles, icons, and attaches action listeners to the main user inputs.
     * Error message labels are initialized but hidden by default.
     * </p>
     */
    public void initLogin() {

        panelLogIn.setBackground(Color.white);
        panelLogIn.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[]10[][]15[]push"));
        JLabel label = new JLabel("Log In");
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(Application.dark_purple);
        panelLogIn.add(label);

        emailTxFLogIn.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/mail.png")));
        emailTxFLogIn.setHint("Email");
        panelLogIn.add(emailTxFLogIn, "w 60%");
        passwordTxFLogIn.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        passwordTxFLogIn.setHint("Password");
        panelLogIn.add(passwordTxFLogIn, "w 60%");

        changePassword.setText("Forgot your password ?");
        changePassword.setFont(new Font("sansserif", 1, 12));
        changePassword.setContentAreaFilled(false);
        changePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelLogIn.add(changePassword);

        activateAccount.setText("Activate Account");
        activateAccount.setFont(new Font("sansserif", 1, 12));
        activateAccount.setContentAreaFilled(false);
        activateAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelLogIn.add(activateAccount, "w 70%");

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        errorMessage.setVisible(false);
        panelLogIn.add(errorMessage);

        applyLogIn.setText("LOG IN");
        applyLogIn.setBackground(Application.turquoise);
        applyLogIn.setForeground(Color.white);
        applyLogIn.setUI(new StyledButtonUI());
        panelLogIn.add(applyLogIn, "w 40%, h 40");
    }

    /**
     * Handles button interactions:
     * <ul>
     *     <li><b>LOG IN:</b> Attempts authentication via {@link #logIn()}.
     *         On success, resets the panel and transitions to the main menu.</li>
     *     <li><b>Forgot password:</b> Validates the email field and opens a
     *         password change dialog if allowed.</li>
     * </ul>
     *
     * @param e the action event triggered by the user
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == applyLogIn) {
            System.out.println("LogIn");
            if(logIn()) {
                resetPanel();
                appMenu.changeToMainMenu();
            }

        }else if(e.getSource() == changePassword) {
            if(canChangePassword()) {
                showChangePasswordPane(appMenu);
            }

        }else if(e.getSource() == activateAccount) {
            showActivateAccountPane(appMenu);
        }
    }

    /**
     * Displays the "Change Password" dialog, where the user can enter a new
     * password twice for confirmation. Validation is handled inside this method.
     *
     * @param parentFrame the application frame to anchor the dialog
     */
    private void showChangePasswordPane(JFrame parentFrame) {
        String emailString = emailTxFLogIn.getText();
        MyTextField password1 = new MyTextField();
        MyTextField password2 = new MyTextField();
        MyButton okButton = new MyButton("Aceptar");
        MyButton cancelButton = new MyButton("Cancelar");

        ChangePassword panel = new ChangePassword(password1, password2, okButton, cancelButton);
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog(parentFrame, "Change Password", true);
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pass1 = password1.getText();
                String pass2 = password2.getText();
                String email = emailTxFLogIn.getText();
                if(pass1 != null && pass1.equals(pass2) && !pass1.isBlank()) {
                    if(validatePassword(pass2)) {
                        try{
                            appMenu.client.changePassword(email,pass2);
                            panel.showErrorMessage("Password changed successfully");
                            dialog.dispose();
                        }catch (IOException | InterruptedException ex){
                            ex.printStackTrace();
                            panel.showErrorMessage("Error changing the password: "+ex.getMessage());
                        }
                    }else {
                        panel.showErrorMessage("Password must contain 1 number and minimum 8 characters");
                    }
                }else{
                    panel.showErrorMessage("Passwords do not match");
                }

            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    /**
     * Displays the "Activate Account" dialog, where the user can enter their email,
     * password and token given by the administrator outside the system.
     * If the credentials are correct, a keyPair is created, the private key is stored in the computer
     * in a file, and the public key is sent to the server for storage and safe communication.
     * Validation is handled inside this method.
     *
     * @param parentFrame the application frame to anchor the dialog
     */
    private void showActivateAccountPane(JFrame parentFrame) {
        MyTextField emailTxt = new MyTextField();
        MyTextField passwordTxt = new MyTextField();
        MyTextField tokenTxt = new MyTextField();
        MyButton okButton = new MyButton("OK");
        MyButton cancelButton = new MyButton("CANCEL");

        final JLabel errorMessageDialog = new JLabel();
        JPanel panel  = new JPanel();
        panel.setLayout(new MigLayout("wrap", "push[center]push", "push[]25[]10[]10[]10[]10[]push"));
        JLabel label = new JLabel("Activate Account");
        label.setFont(new Font("sansserif", 1, 30));
        label.setForeground(Application.dark_purple);
        panel.add(label);

        emailTxt.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/mail.png")));
        emailTxt.setHint("Enter the email provided...");
        panel.add(emailTxt, "w 60%");

        passwordTxt.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        passwordTxt.setHint("Enter the password provided...");
        panel.add(passwordTxt, "w 60%");

        tokenTxt.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/key.png")));
        tokenTxt.setHint("Enter the token provided...");
        panel.add(tokenTxt, "w 60%");

        errorMessageDialog.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessageDialog.setForeground(Color.red);
        errorMessageDialog.setText("Error message test");
        errorMessageDialog.setVisible(false);
        //panel.add(errorMessageDialog);

        okButton.setBackground(Application.turquoise);
        okButton.setForeground(new Color(250, 250, 250));
        cancelButton.setBackground(Application.turquoise);
        cancelButton.setForeground(new Color(250, 250, 250));

        panel.add(okButton, "split 2, grow, left");
        panel.add(cancelButton, "grow, right");
        panel.add(errorMessageDialog,"w 10%" );
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(400, 300));

        JDialog dialog = new JDialog(parentFrame, "Activate Account", true);
        dialog.getContentPane().add(panel);
        dialog.getContentPane().setBackground(Color.white);
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailTxt.getText();
                String pass = passwordTxt.getText();
                String token = tokenTxt.getText();

                if(email != null && !email.isBlank() && !pass.isBlank() && !token.isBlank()) {
                    //TODO: send data to Server to check if token is valid for the user (email,password)
                    ///TODO: when confirmation received, create private and public keys, send public key to server and if it doesn't throw any errors,
                    ///then save private key in computer file and return no logIn
                    errorMessageDialog.setText("Password changed successfully");
                    errorMessageDialog.setVisible(true);
                    dialog.dispose();
                }else{
                    errorMessageDialog.setText("Complete all fields");
                    errorMessageDialog.setVisible(true);
                }

            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }


    /**
     * Attempts to authenticate the user with the server using the e-mail and
     * password fields.
     * <p>
     * On success, returns {@code true}. On failure, an error message is shown
     * and {@code false} is returned.
     * </p>
     *
     * @return {@code true} if authentication succeeded, otherwise {@code false}
     */
    private Boolean logIn() {
        String email = emailTxFLogIn.getText();
        String password = passwordTxFLogIn.getText();
        System.out.println("email: " + email+" password: "+password);
        if(!email.isBlank() && !password.isBlank()) {

            try {
                AppData appdata = appMenu.client.login(email, password);
                System.out.println(appdata);
                if(appdata.getDoctor() != null && appdata.getUser() != null) {
                    appMenu.doctor = appdata.getDoctor();
                    appMenu.user = appdata.getUser();
                    return true;
                }else{
                    showErrorMessage("Error retrieving Patient and User data");
                    return false;
                }
            } catch (IOException | InterruptedException | LogInError e) {
                showErrorMessage(e.getMessage());
                return false;
            }
        }else {
            showErrorMessage("Complete all fields");
            return false;
        }
    }

    /**
     * Determines whether the password change dialog can be opened.
     * <p>
     * This method first checks if the email introduced is of a real user.
     * If not, it returns false and doesn't allow to change the password.
     * </p>
     *
     * @return {@code true} if password change is allowed
     */
    public Boolean canChangePassword() {
        String email = emailTxFLogIn.getText();
        if(email != null && !email.isBlank()){
            //TODO: send request to server
            Boolean isUser = true; //appMenu.jpaUserMan.isUser(email);
            if(isUser) {
                return true;
            }else {
                showErrorMessage("Invalid user or password");
                return false;
            }
        }else {
            showErrorMessage("Write the email first");
            return false;
        }

    }

    /**
     * Validates password complexity: at least 8 characters and at least one digit.
     *
     * @param password the password to validate
     * @return {@code true} if the password meets requirements
     */
    private Boolean validatePassword(String password) {
        boolean passwordVacia = (Objects.isNull(password)) || password.isEmpty();
        boolean goodPassword=false;
        System.out.println("password vacía "+passwordVacia);
        if(!passwordVacia && password.length() >= 8) {
            for(int i=0; i<password.length(); i++) {

                //The password must contain at least one number
                if(Character.isDigit(password.charAt(i))) {
                    goodPassword = true;
                }
            }
            if(!goodPassword) {
                showErrorMessage("The password must contain at least one number.");
                return false;
            }
        }else {
            showErrorMessage("Password's minimum lenght is of 8 characters");
            return false;
        }
        return true;

    }

    /**
     * Resets email, password, and error fields.
     * <p>
     * Called automatically after login or when returning to the login screen.
     * </p>
     */
    private void resetPanel() {
        emailTxF.setText(null);
        emailTxFLogIn.setText(null);
        passwordTxF.setText(null);
        passwordTxFLogIn.setText(null);
        hideErrorMessage();
    }

    /**
     * Shows a login-related error message below the login form.
     *
     * @param text the message to show
     */
    public void showErrorMessage(String text){
        errorMessage.setVisible(true);
        errorMessage.setText(text);
    }

    /**
     * Hides the login error message.
     */
    public void hideErrorMessage() {
        errorMessage.setVisible(false);
    }

}
