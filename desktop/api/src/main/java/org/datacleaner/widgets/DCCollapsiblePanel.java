/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

/**
 * A panel that has a title, a small collapse/expand button and a label, which
 * is used to show/hide a panel underneath it.
 */
public class DCCollapsiblePanel {

    private static final Icon _expandedIcon =
            ImageManager.get().getImageIcon(IconUtils.ACTION_ADD_DARK, IconUtils.ICON_SIZE_SMALL);
    private static final Icon _collapsedIcon =
            ImageManager.get().getImageIcon(IconUtils.ACTION_REMOVE_DARK, IconUtils.ICON_SIZE_SMALL);

    private final JXCollapsiblePane _collapsiblePane;
    private final DCLabel _label;
    private final Supplier<? extends JComponent> _componentRef;
    private String _collapsedText;
    private String _expandedText;
    private boolean _rendered = false;

    public DCCollapsiblePanel() {
        this(null, null, true, null);
    }

    public DCCollapsiblePanel(final String collapsedText, final String expandedText, final boolean collapsed,
            final Supplier<? extends JComponent> componentRef) {
        _collapsedText = collapsedText;
        _expandedText = expandedText;
        _collapsiblePane = WidgetFactory.createCollapsiblePane(Direction.DOWN);
        _componentRef = componentRef;
        if (collapsed) {
            _label = DCLabel.dark(collapsedText);
            collapse();
        } else {
            _label = DCLabel.dark(expandedText);
            expand();
        }
        _label.setFont(WidgetUtils.FONT_HEADER2);
        _label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        _label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                toggle();
            }
        });
    }

    public DCPanel toPanel() {
        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_label, BorderLayout.NORTH);
        panel.add(_collapsiblePane, BorderLayout.CENTER);
        return panel;
    }

    public void toggle() {
        if (isCollapsed()) {
            expand();
        } else {
            collapse();
        }
    }

    public void collapse() {
        setCollapsed(true);
    }

    public void expand() {
        if (!_rendered) {
            _rendered = true;
            setComponent(_componentRef.get());
        }
        setCollapsed(false);
    }

    public boolean isCollapsed() {
        return _collapsiblePane.isCollapsed();
    }

    public void setCollapsed(final boolean collapsed) {
        if (collapsed) {
            _label.setText(getCollapsedText());
            _label.setIcon(_collapsedIcon);
        } else {
            _label.setText(getExpandedText());
            _label.setIcon(_expandedIcon);
        }
        _collapsiblePane.setCollapsed(collapsed);
        _collapsiblePane.updateUI();
    }

    public String getCollapsedText() {
        return _collapsedText;
    }

    public void setCollapsedText(final String collapsedText) {
        _collapsedText = collapsedText;
    }

    public String getExpandedText() {
        return _expandedText;
    }

    public void setExpandedText(final String expandedText) {
        _expandedText = expandedText;
    }

    public Component getComponent() {
        final Container contentPane = _collapsiblePane.getContentPane();
        if (contentPane.getComponentCount() == 0) {
            return null;
        }
        final DCPanel innerPanel = (DCPanel) contentPane.getComponent(0);
        return innerPanel.getComponent(0);
    }

    public void setComponent(final Component component) {
        final Container contentPane = _collapsiblePane.getContentPane();
        contentPane.removeAll();
        if (component != null) {
            contentPane.add(component);
        }
    }
}
