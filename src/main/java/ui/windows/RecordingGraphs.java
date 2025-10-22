package ui.windows;

import net.miginfocom.swing.MigLayout;
import pojos.Patient;
import pojos.Signal;
import ui.components.MyButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class RecordingGraphs extends JPanel implements ActionListener, MouseListener {
    private Application appMain;
    private PatientInfo parentPanel;
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

    public RecordingGraphs(Application appMain, PatientInfo parentPanel, Signal signal, String patientName) {
        this.appMain = appMain;
        this.signal = signal;
        this.parentPanel = parentPanel;
        titleText = patientName+"'s Recording "+signal.getDate().toString();
        initMainPanel();
    }

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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBackButton) {
            appMain.changeToPanel(parentPanel);
            //And delete this one
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
