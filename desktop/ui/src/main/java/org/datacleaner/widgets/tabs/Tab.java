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

import java.awt.Component;

import javax.swing.Icon;

/**
 * Represents the state of a tab in a tabbed pane.
 *
 * @param <C>
 *            the component type that is shown in this tab's content pane
 */
public interface Tab<C extends Component> {

    public C getContents();

    public Icon getIcon();

    public void setIcon(Icon icon);

    public String getTooltip();

    public void setTooltip(String tooltip);

    public String getTitle();

    public void setTitle(String title);

    public boolean isCloseable();
}
