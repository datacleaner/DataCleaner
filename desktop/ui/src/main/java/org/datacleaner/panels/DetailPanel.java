package org.datacleaner.panels;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

//TODO: Consider a more descriptive class name?
public class DetailPanel extends DCPanel {
    
    private static final Color COLOR_NORMAL = WidgetUtils.BG_COLOR_LESS_BRIGHT;
    private static final Color COLOR_HOVER = WidgetUtils.BG_COLOR_BRIGHTEST;

    private static final long serialVersionUID = 1L;

    public DetailPanel(final String title, final String body) {
        this(null, title, body);
    }

    public DetailPanel(final Icon icon, final String title, final String body) {
        super(WidgetUtils.BG_SEMI_TRANSPARENT_BRIGHT);
        setLayout(new GridBagLayout());
        setBorder(WidgetUtils.BORDER_LIST_ITEM);
        final DCLabel titleLabel = DCLabel.bright(title);
        final DCLabel bodyLabel = DCLabel.brightMultiLine(body);
        if (icon == null) {
            titleLabel.setFont(WidgetUtils.FONT_UBUNTU_PLAIN.deriveFont(30f));
            bodyLabel.setFont(WidgetUtils.FONT_UBUNTU_PLAIN.deriveFont(18f));
        } else {
            titleLabel.setFont(WidgetUtils.FONT_BANNER);
            bodyLabel.setFont(WidgetUtils.FONT_HEADER2);
        }
        titleLabel.setForeground(COLOR_NORMAL);
        bodyLabel.setForeground(COLOR_NORMAL);

        final JSeparator horizontalRule = new JSeparator(JSeparator.HORIZONTAL);
        horizontalRule.setForeground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);
        horizontalRule.setBackground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                titleLabel.setForeground(COLOR_HOVER);
                bodyLabel.setForeground(COLOR_HOVER);
                setBorder(WidgetUtils.BORDER_LIST_ITEM_HIGHLIGHTED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                titleLabel.setForeground(COLOR_NORMAL);
                bodyLabel.setForeground(COLOR_NORMAL);
                setBorder(WidgetUtils.BORDER_LIST_ITEM);
            }
        });

        GridBagConstraints c = new GridBagConstraints();

        Insets questionInsets = new Insets(5, 5, 5, 5);

        if (icon != null) {
            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 3;
            c.insets = questionInsets;
            add(new JLabel(icon), c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        c.insets = questionInsets;
        c.anchor = GridBagConstraints.LINE_START;
        add(titleLabel, c);

        if (icon == null) {
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1.0;
            c.insets = questionInsets;
            c.anchor = GridBagConstraints.LINE_START;
            add(horizontalRule, c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        c.insets = questionInsets;
        c.anchor = GridBagConstraints.LINE_START;
        add(bodyLabel, c);
    }
}
