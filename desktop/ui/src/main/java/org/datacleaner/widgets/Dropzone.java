package org.datacleaner.widgets;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;

public class Dropzone extends DCPanel{
    private static final long serialVersionUID = 1L;

    public Dropzone() {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        setBorder(new CompoundBorder(BorderFactory.createDashedBorder(WidgetUtils.BG_COLOR_DARK), new EmptyBorder(10, 10, 10, 10)));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        DCLabel dropFileLabel = DCLabel.dark("Drop file");
        dropFileLabel.setFont(WidgetUtils.FONT_BANNER);
        dropFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(dropFileLabel);
        
        add(Box.createVerticalStrut(10));
        
        JButton orClickButton = new JButton("(or click)");
        WidgetUtils.setPrimaryButtonStyle(orClickButton);
        orClickButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(orClickButton);
    }
}
