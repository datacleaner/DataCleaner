/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetUtils;

/**
 * A collapsible, titled task pane - a lightweight replacement for SwingX's
 * {@code DCTaskPane}. The pane has a clickable title bar (with icon, title
 * and a caret indicating the state) and content that expands/collapses
 * using {@link DCCollapsiblePane}.
 */
public class DCTaskPane extends JComponent {

    private static final long serialVersionUID = 1L;

    private final JLabel _iconLabel;
    private final JLabel _titleLabel;
    private final JLabel _caretLabel;
    private final DCPanel _titleBar;
    private final DCCollapsiblePane _contentPane;

    public DCTaskPane() {
        this(null, null);
    }

    public DCTaskPane(final String title, final Icon icon) {
        setLayout(new BorderLayout());

        _iconLabel = new JLabel();
        _iconLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        if (icon != null) {
            setIcon(icon);
        }

        _titleLabel = new JLabel(title);
        _titleLabel.setFont(WidgetUtils.FONT_TABLE_HEADER);
        _titleLabel.setForeground(Color.WHITE);
        _titleLabel.setBorder(WidgetUtils.BORDER_EMPTY);

        _caretLabel = new JLabel(WidgetUtils.CHAR_CARET_DOWN);
        _caretLabel.setFont(WidgetUtils.FONT_FONTAWESOME);
        _caretLabel.setForeground(Color.WHITE);
        _caretLabel.setBorder(WidgetUtils.BORDER_EMPTY);

        _titleBar = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        _titleBar.setLayout(new BorderLayout());
        _titleBar.add(_iconLabel, BorderLayout.WEST);
        _titleBar.add(_titleLabel, BorderLayout.CENTER);
        _titleBar.add(_caretLabel, BorderLayout.EAST);
        _titleBar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        _titleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                setCollapsed(!isCollapsed());
            }
        });

        _contentPane = new DCCollapsiblePane(DCCollapsiblePane.Direction.DOWN);
        _contentPane.getContentPane().setLayout(new BorderLayout());

        super.add(_titleBar, BorderLayout.NORTH);
        super.add(_contentPane, BorderLayout.CENTER);
    }

    public String getTitle() {
        return _titleLabel.getText();
    }

    public void setTitle(final String title) {
        _titleLabel.setText(title);
    }

    public Icon getIcon() {
        return _iconLabel.getIcon();
    }

    public void setIcon(final Icon icon) {
        _iconLabel.setIcon(icon);
        _iconLabel.setVisible(icon != null);
    }

    public void setCollapsed(final boolean collapsed) {
        _contentPane.setCollapsed(collapsed);
        _caretLabel.setText(collapsed ? WidgetUtils.CHAR_CARET_RIGHT : WidgetUtils.CHAR_CARET_DOWN);
    }

    public boolean isCollapsed() {
        return _contentPane.isCollapsed();
    }

    /**
     * Gets the container that holds the content of this task pane.
     *
     * @return the content pane
     */
    public JPanel getContentPane() {
        return _contentPane.getContentPane();
    }

    @Override
    public Component add(final Component comp) {
        return getContentPane().add(comp);
    }

    @Override
    public Component add(final Component comp, final int index) {
        return getContentPane().add(comp, index);
    }

    @Override
    public void add(final Component comp, final Object constraints) {
        getContentPane().add(comp, constraints);
    }

    @Override
    public void removeAll() {
        getContentPane().removeAll();
    }
}
