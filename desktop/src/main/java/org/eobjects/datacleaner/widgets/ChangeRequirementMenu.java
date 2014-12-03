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
package org.eobjects.datacleaner.widgets;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

public class ChangeRequirementMenu extends JMenu {
    private static final long serialVersionUID = 1L;

    public ChangeRequirementMenu(AbstractBeanJobBuilder<?, ?, ?> componentBuilder) {
        super("Set requirement");
        setIcon(ImageManager.get().getImageIcon(IconUtils.FILTER_IMAGEPATH));
        
        final ChangeRequirementMenuBuilder menuBuilder = new ChangeRequirementMenuBuilder(componentBuilder);
        if (menuBuilder.isFilterRequirementsAvailable()) {
            final List<JMenuItem> menuItems = menuBuilder.createMenuItems();
            for (JMenuItem menuItem : menuItems) {
                add(menuItem);
            }
        } else {
            setEnabled(false);
        }
        
    }
}
