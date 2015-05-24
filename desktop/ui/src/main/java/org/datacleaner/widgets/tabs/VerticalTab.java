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
package org.datacleaner.widgets.tabs;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;

public class VerticalTab<C extends JComponent> implements Tab<C> {

    private final JButton _button;
    private final C _contents;

    public VerticalTab(JButton button, C component) {
        _button = button;
        _contents = component;
    }

    @Override
    public Icon getIcon() {
        return _button.getIcon();
    }

    @Override
    public void setIcon(Icon icon) {
        _button.setIcon(icon);
    }

    @Override
    public String getTooltip() {
        return _button.getToolTipText();
    }

    @Override
    public void setTooltip(String tooltip) {
        _button.setToolTipText(tooltip);
    }

    @Override
    public String getTitle() {
        return _button.getText();
    }

    @Override
    public void setTitle(String title) {
        _button.setText(title);
    }

    @Override
    public boolean isCloseable() {
        return false;
    }

    @Override
    public C getContents() {
        return _contents;
    }

    public JButton getButton() {
        return _button;
    }
}
