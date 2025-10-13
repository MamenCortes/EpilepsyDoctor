package ui.components;

import net.miginfocom.swing.MigLayout;
import ui.Application;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ReportCell implements ListCellRenderer<String> {

    private final Color titleColor = Application.turquoise;
    private final Font titleFont = new Font("sansserif", 3, 12);
    private final Font contentFont = new Font("sansserif", 1, 12);
    private final Color contentColor = new Color(122, 140, 141);
    //private Color backgroundColor = new Color(230, 245, 241);


    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        JPanel listCell = new JPanel();
        listCell.setLayout(new MigLayout("fill, inset 20, gap 0, wrap 4", "[][]5[][]", "[][][][]"));
        listCell.setBackground(Color.white);
        Border border = javax.swing.BorderFactory.createLineBorder(Application.dark_turquoise);
        listCell.setBorder(border);

        //Name
        JLabel reportHeading = new JLabel("Report:");
        reportHeading.setForeground(titleColor);
        reportHeading.setFont(titleFont);

        JLabel reportInfo = new JLabel(value);
        reportInfo.setForeground(contentColor);
        reportInfo.setFont(contentFont);
        listCell.add(reportHeading, "grow, left");
        listCell.add(reportInfo, "grow, left");

        if(isSelected)
        {
            listCell.setBackground(Application.lighter_turquoise);
        }else {
            listCell.setBackground(Color.white);
        }
        return listCell;
    }

}
