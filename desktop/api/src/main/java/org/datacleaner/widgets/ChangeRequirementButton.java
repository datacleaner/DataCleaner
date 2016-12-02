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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.datacleaner.job.AnyComponentRequirement;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A button that displays the {@link ComponentRequirement} of a particular
 * component that is being built.
 */
public class ChangeRequirementButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ChangeRequirementButton.class);
    private static final ImageManager imageManager = ImageManager.get();
    private static final Icon filterIcon =
            imageManager.getImageIcon(IconUtils.FILTER_OUTCOME_PATH, IconUtils.ICON_SIZE_MEDIUM);

    private final ComponentBuilder _componentBuilder;

    public ChangeRequirementButton(final ComponentBuilder componentBuilder) {
        super(ChangeRequirementMenuBuilder.NO_REQUIREMENT_TEXT, filterIcon);
        _componentBuilder = componentBuilder;
        addActionListener(this);
        updateText();
        WidgetUtils.setDefaultButtonStyle(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu();

        final ChangeRequirementMenuBuilder menuBuilder = new ChangeRequirementMenuBuilder(_componentBuilder) {
            @Override
            protected void onRequirementChanged() {
                updateText();
            }
        };
        final List<JMenuItem> menuItems = menuBuilder.createMenuItems();
        for (final JMenuItem menuItem : menuItems) {
            popup.add(menuItem);
        }

        popup.show(this, 0, getHeight());
    }

    public void updateText() {
        logger.debug("updateText()");

        final Runnable runnable = () -> {
            final ComponentRequirement requirement = _componentBuilder.getComponentRequirement();
            if (requirement == null) {
                setText(ChangeRequirementMenuBuilder.NO_REQUIREMENT_TEXT);
            } else if (AnyComponentRequirement.get().equals(requirement)) {
                setText(ChangeRequirementMenuBuilder.ANY_REQUIREMENT_TEXT);
            } else {
                setText(requirement.getSimpleName());
            }
        };
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeAndWait(runnable);
            }
        } catch (final Exception e) {
            logger.error("Failed to update ChangeRequirementButton", e);
        }
    }

    @Override
    public String toString() {
        return "ChangeRequirementButton[jobBuilder=" + LabelUtils.getLabel(_componentBuilder) + "]";
    }
}
