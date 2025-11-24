package ui.windows;

import net.miginfocom.swing.MigLayout;
import pojos.Patient;
import pojos.Signal;
import ui.components.MyButton;
import ui.components.SignalGraphPanel;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
/**
 * Panel responsible for displaying ECG and ACC graphs associated with a specific
 * patient recording. The panel also allows editing and saving comments attached
 * to the recording.
 * <p>
 * This panel is <b>not reused</b>. A fresh instance is created every time a
 * recording is opened from {@code PatientInfo}. Because of this:
 * </p>
 *
 * <h3>Lifecycle</h3>
 * <ul>
 *     <li>The panel is instantiated when a recording is selected.</li>
 *     <li>When leaving, {@link #saveComments()} attempts to persist comment changes.</li>
 *     <li>After navigating back, the panel is discarded and not shown again.</li>
 * </ul>
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>Toggle between ECG and ACC views using a {@link CardLayout}</li>
 *     <li>Shows the raw signal graphs via {@link SignalGraphPanel}</li>
 *     <li>Displays and allows modifying comments associated with this recording</li>
 * </ul>
 */
public class RecordingGraphs extends JPanel implements ActionListener, MouseListener {
    private Application appMain;
    private PatientInfo parentPanel;
    private Patient patient;
    private Signal signal;
    private final Font titleFont = new Font("sansserif", 3, 15);
    private final Color titleColor = Application.dark_purple;
    private String titleText;
    private ImageIcon icon  = new ImageIcon(getClass().getResource("/icons/ekg-monitor64_02.png"));

    //Components
    private JLabel title;
    private MyButton goBackButton;
    private JLabel errorMessage;
    private SignalGraphPanel ecgGraph;
    private SignalGraphPanel accGraph;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton ecgButton;
    private JButton accButton;
    private JTextArea commentsTextArea;
    /**
     * Creates the recording graphs panel for a specific signal and patient.
     * <p>
     * Signal data (ECG/ACC) is assumed to be already loaded and processed
     * before this panel is constructed. The UI components are derived from
     * that data.
     * </p>
     *
     * @param appMain      reference to the {@link Application} controller used
     *                     for navigation and server interaction.
     * @param parentPanel  the {@link PatientInfo} panel that opened this view.
     *                     The panel will return to this parent after closing.
     * @param signal       the recording whose data and comments will be displayed.
     * @param patient      the patient to whom the recording belongs.
     */
    public RecordingGraphs(Application appMain, PatientInfo parentPanel, Signal signal, Patient patient) {
        this.appMain = appMain;
        this.signal = signal;
        this.parentPanel = parentPanel;
        this.patient = patient;
        titleText = patient.getName()+" "+patient.getSurname()+"'s Recording "+signal.getDate().toString();
        initMainPanel();
    }

    /**
     * Builds the entire UI: header, buttons, comment area, graph panels,
     * and the card layout for switching between ECG and ACC.
     * <p>
     * Actual signal graph contents are provided by {@link SignalGraphPanel}.
     * </p>
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

        ecgButton = new MyButton("ECG");
        ecgButton.addActionListener(this);
        add(ecgButton, "cell 0 1, center, gapy 10, growx");

        accButton = new MyButton("ACC");
        accButton.addActionListener(this);
        add(accButton, "cell 0 2, center, gapy 5, growx");

        commentsTextArea = new JTextArea(signal.getComments());
        commentsTextArea.setEditable(true);
        commentsTextArea.setLineWrap(true);
        add(commentsTextArea, "cell 0 3, center, gapy 5, growx");

        TitledBorder border = BorderFactory.createTitledBorder("Comments");
        border.setTitleFont(titleFont);
        border.setTitleColor(Application.turquoise);
        commentsTextArea.setBorder(border);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        ecgGraph = new SignalGraphPanel(signal.getEcg(), signal.getFrequency(), "ECG Signal");
        accGraph = new SignalGraphPanel(signal.getAcc(), signal.getFrequency(), "ACC Signal");

        cardPanel.add(ecgGraph, "Panel1");
        cardPanel.add(accGraph, "Panel2");

        // Show the ECG first:
        cardLayout.show(cardPanel, "Panel1");
        add(cardPanel, "cell 1 1, span 1 7, grow");

        goBackButton = new MyButton("BACK TO MENU", Application.turquoise, Color.white);
        goBackButton.addActionListener(this);
        add(goBackButton, "cell 0 9, center, gapy 5, growx");
        goBackButton.setVisible(true);

        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        this.add(errorMessage, "cell 0 4, left");
        errorMessage.setVisible(false);
    }

    /**
     * Saves the modified comments back to the server, if they have changed.
     * <p>
     * This operation is executed when the user presses the "Back to Menu" button.
     * It compares the current text in the comment box with the stored comments
     * inside the {@link Signal} object. If different, a server call is executed.
     * </p>
     *
     * @throws IOException          if the server cannot be reached.
     * @throws InterruptedException if the client thread is interrupted.
     */
    private void saveComments() throws IOException, InterruptedException {
        String comments = commentsTextArea.getText();
        if(!signal.getComments().equals(comments)){
            signal.setComments(comments);
            appMain.client.saveComments(patient.getId(), signal);
        }
    }
    /**
     * Shows an error message on the panel, typically after a failed save or
     * when switching between views encounters an issue.
     *
     * @param message the message text to display
     */
    private void showErrorMessage(String message) {
        errorMessage.setVisible(true);
        errorMessage.setText(message);
    }

    /**
     * Handles the main interactions with the panel:
     * <ul>
     *     <li><b>Back to Menu:</b> Saves comments, returns to the parent
     *         {@link PatientInfo} panel.</li>
     *     <li><b>ECG:</b> Shows the ECG graph view.</li>
     *     <li><b>ACC:</b> Shows the ACC graph view.</li>
     * </ul>
     *
     * @param e the action event triggered by the user
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            try {
                //TODO: Save comments in signal
                saveComments();
                appMain.changeToPanel(parentPanel);
            }catch(IOException | InterruptedException ex){
                //TODO: show popUp dialog asking if you are sure you eant to go back without saving
                showErrorMessage("Error saving comments");
            }
            //TODO: delete this one panel?
        }else if (e.getSource() == accButton) {
            cardLayout.show(cardPanel, "Panel2");
        }else if (e.getSource() == ecgButton) {
            cardLayout.show(cardPanel, "Panel1");
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
