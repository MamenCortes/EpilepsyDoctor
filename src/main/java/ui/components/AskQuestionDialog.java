package ui.components;

import net.miginfocom.swing.MigLayout;
import ui.windows.Application;
import javax.swing.*;
import java.awt.*;
/**
 * A dialog panel displaying a question and two action buttons.
 * It arranges the components using MigLayout and applies custom colors.
 *
 * @author MamenCortes
 */
public class AskQuestionDialog extends JPanel {
    private JLabel errorMessage;
    /**
     * Creates a new AskQuestionDialog containing the provided question text
     * along with OK and Cancel buttons.
     *
     * @param ipTextField the text of the question to display
     * @param okbutton the confirmation button
     * @param cancelbutton the cancellation button
     */
    public AskQuestionDialog(MyTextField ipTextField, MyButton okbutton, MyButton cancelbutton) {
        this.setLayout(new MigLayout("wrap, fill, inset 20", "push[center]push", "push[]25[]10[]20[]push"));
        JLabel label = new JLabel("Introduce server IP Address:");
        label.setFont(new Font("sansserif", 1, 25));
        label.setForeground(Application.dark_purple);
        this.add(label);

        MyTextField iptxtField = ipTextField;
        iptxtField.setPrefixIcon(new ImageIcon(getClass().getResource("/icons/pass.png")));
        iptxtField.setHint("Enter new IP Address...");
        this.add(iptxtField, "w 60%");


        errorMessage = new JLabel();
        errorMessage.setFont(new Font("sansserif", Font.BOLD, 12));
        errorMessage.setForeground(Color.red);
        errorMessage.setText("Error message test");
        errorMessage.setVisible(false);


        MyButton okButton = okbutton;
        //okButton.setText("OK");
        okButton.setBackground(Application.turquoise);
        okButton.setForeground(new Color(250, 250, 250));
        MyButton cancelButton = cancelbutton;
        //cancelButton.setText("CANCEL");
        cancelButton.setBackground(Application.turquoise);
        cancelButton.setForeground(new Color(250, 250, 250));

        this.add(okButton, "split 2, grow, left");
        this.add(cancelButton, "grow, right");
        this.add(errorMessage,"w 10%" );

    }
    /**
     * Displays an error message to the user.
     *
     * @param text the message to display
     */
    public void showErrorMessage(String text) {
        errorMessage.setVisible(true);
        errorMessage.setText(text);
    }


}