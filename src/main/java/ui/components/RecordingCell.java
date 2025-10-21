package ui.components;

import net.miginfocom.swing.MigLayout;
import pojos.Signal;
import ui.windows.Application;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class RecordingCell implements ListCellRenderer<Signal> {

    private final Color titleColor = Application.turquoise;
    private final Font titleFont = new Font("sansserif", 3, 12);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = new Color(122, 140, 141);
    //private Color backgroundColor = new Color(230, 245, 241);


    @Override
    public Component getListCellRendererComponent(JList<? extends Signal> list, Signal value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        JPanel listCell = new JPanel();
        listCell.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 2", "[20%]5[80%]", "[][]"));
        listCell.setBackground(Color.white);
        Border border = javax.swing.BorderFactory.createLineBorder(Application.dark_turquoise);
        listCell.setBorder(border);

        //Date
        JLabel dateHeading = new JLabel("Date:");
        dateHeading.setForeground(titleColor);
        dateHeading.setFont(titleFont);

        JLabel dateValue = new JLabel(value.getDate().toString());
        dateValue.setForeground(contentColor);
        dateValue.setFont(contentFont);

        //Comments
        JLabel commentsHeading = new JLabel("Comments:");
        commentsHeading.setForeground(titleColor);
        commentsHeading.setFont(titleFont);

        //JLabel comments = new JLabel("<html><body style='width:250px'>" + text + "</body></html>");
        JLabel comments = new JLabel(value.getComments());
        comments.setForeground(contentColor);
        comments.setFont(contentFont);
        //comments.setHorizontalAlignment(SwingConstants.LEFT);

        //listCell.add(dateHeading, "grow, left");
        //listCell.add(comments, "grow, left");
        listCell.add(dateHeading, "grow, left");
        listCell.add(dateValue, "grow, left");
        listCell.add(commentsHeading, "grow, left");
        listCell.add(comments, "grow, left");

        if(isSelected)
        {
            listCell.setBackground(Application.lighter_turquoise);
        }else {
            listCell.setBackground(Color.white);
        }
        return listCell;
    }

}
