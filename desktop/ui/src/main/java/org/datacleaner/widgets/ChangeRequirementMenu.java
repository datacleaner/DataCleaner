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

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;

public class ChangeRequirementMenu extends JMenu {

    private static final long serialVersionUID = 1L;

    public ChangeRequirementMenu(final ComponentBuilder componentBuilder) {
        super("Set requirement");
        setIcon(ImageManager.get().getImageIcon(IconUtils.FILTER_OUTCOME_PATH, IconUtils.ICON_SIZE_SMALL));

        final ChangeRequirementMenuBuilder menuBuilder = new ChangeRequirementMenuBuilder(componentBuilder);
        if (menuBuilder.isFilterRequirementsAvailable()) {
            final List<JMenuItem> menuItems = menuBuilder.createMenuItems();
            for (final JMenuItem menuItem : menuItems) {
                add(menuItem);
            }
        } else {
            setEnabled(false);
        }

    }

    /**
     * Determines if changing requirements for a particular
     * {@link ComponentBuilder} is relevant or not. If no filters exist, it is
     * not relevant to even show the ability to set requirements.
     *
     * @param componentBuilder
     * @return
     */
    public static boolean isRelevant(final ComponentBuilder componentBuilder) {
        return !componentBuilder.getAnalysisJobBuilder().getFilterComponentBuilders().isEmpty();
    }
}
