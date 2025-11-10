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

public class RecordingGraphs extends JPanel implements ActionListener, MouseListener {
    private Application appMain;
    private PatientInfo parentPanel;
    private Patient patient;
    private Signal signal;
    private final Font titleFont = new Font("sansserif", 3, 15);
    private final Color titleColor = Application.dark_purple;
    private String getTitleText;
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

    public RecordingGraphs(Application appMain, PatientInfo parentPanel, Signal signal, Patient patient) {
        this.appMain = appMain;
        this.signal = signal;
        this.parentPanel = parentPanel;
        this.patient = patient;
        titleText = patient.getName()+" "+patient.getSurname()+"'s Recording "+signal.getDate().toString();
        initMainPanel();
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
        //add(ecgGraph, "cell 0 1 3 3, alignx left");

        accGraph = new SignalGraphPanel(signal.getAcc(), signal.getFrequency(), "ACC Signal");
        //add(accGraph, "cell 0 5 3 3, alignx left");

        cardPanel.add(ecgGraph, "Panel1");
        cardPanel.add(accGraph, "Panel2");

        // Mostrar un panel:
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

    private void saveComments() throws IOException {
        String comments = commentsTextArea.getText();
        if(!signal.getComments().equals(comments)){
            signal.setComments(comments);
            appMain.client.saveComments(patient.getId(), signal);
        }
    }

    private void showErrorMessage(String message) {
        errorMessage.setVisible(true);
        errorMessage.setText(message);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            try {
                //TODO: Save comments in signal
                saveComments();
                appMain.changeToPanel(parentPanel);
            }catch(IOException ex){
                //TODO: show popUp dialog asking if you are sure you eant to go back without saving
                showErrorMessage("Error saving comments");
            }
            //TODO: Reset view
            //And delete this one
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
