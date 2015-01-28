package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class WelcomeQuestionPanel extends DCPanel implements MouseListener {

    private static final long serialVersionUID = 1L;
    private DCLabel _titleLabel;

    public WelcomeQuestionPanel(final String title, final String body) {
        this(null, title, body);
    }

    public WelcomeQuestionPanel(final Icon icon, final String title, final String body) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        setLayout(new GridBagLayout());
        setBorder(WidgetUtils.BORDER_LIST_ITEM);
        _titleLabel = DCLabel.dark(title);
        _titleLabel.setFont(WidgetUtils.FONT_BANNER);
        final DCLabel bodyLabel = DCLabel.darkMultiLine(body);

        final JSeparator horizontalRule = new JSeparator(JSeparator.HORIZONTAL);
        horizontalRule.setForeground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);
        horizontalRule.setBackground(WidgetUtils.BG_COLOR_ORANGE_MEDIUM);

        addMouseListener(this);
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
        add(_titleLabel, c);

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
        _titleLabel.setForeground(WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(WidgetUtils.BORDER_LIST_ITEM_HIGHLIGHTED);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        _titleLabel.setForeground(WidgetUtils.BG_COLOR_DARKEST);
        setCursor(Cursor.getDefaultCursor());
        setBorder(WidgetUtils.BORDER_LIST_ITEM);
    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        final WelcomeQuestionPanel panel = new WelcomeQuestionPanel(
                "<html>Are my <b>addresses correct</b> and <b>up-to-date</b>?</html>",
                "Use the Neopost Address Correction and Mail Suppression services on your contact list to correct your addresses and check if people have moved to new places or if they have passed away.");
        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.add(panel, BorderLayout.PAGE_START);
        frame.pack();
        frame.setVisible(true);
    }

}
