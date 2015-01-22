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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.datacleaner.job.AnyComponentRequirement;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.LazyFilterOutcome;
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
    private static final Icon filterIcon = imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH,
            IconUtils.ICON_SIZE_MEDIUM);

    private final ComponentBuilder _componentBuilder;

    public ChangeRequirementButton(ComponentBuilder componentBuilder) {
        super(ChangeRequirementMenuBuilder.NO_REQUIREMENT_TEXT, filterIcon);
        _componentBuilder = componentBuilder;
        addActionListener(this);
        updateText();
        WidgetUtils.setDefaultButtonStyle(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu();

        final ChangeRequirementMenuBuilder menuBuilder = new ChangeRequirementMenuBuilder(_componentBuilder) {
            @Override
            protected void onRequirementChanged() {
                updateText();
            }
        };
        final List<JMenuItem> menuItems = menuBuilder.createMenuItems();
        for (JMenuItem menuItem : menuItems) {
            popup.add(menuItem);
        }

        popup.show(this, 0, getHeight());
    }

    public void updateText() {
        logger.debug("updateText()");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final ComponentRequirement requirement = _componentBuilder.getComponentRequirement();
                if (requirement == null) {
                    setText(ChangeRequirementMenuBuilder.NO_REQUIREMENT_TEXT);
                } else if (AnyComponentRequirement.get().equals(requirement)) {
                    setText(ChangeRequirementMenuBuilder.ANY_REQUIREMENT_TEXT);
                } else {
                    if (requirement instanceof FilterOutcome) {
                        final FilterOutcome filterOutcome = (FilterOutcome) requirement;
                        final Enum<?> category = filterOutcome.getCategory();
                        if (filterOutcome instanceof LazyFilterOutcome) {
                            // if possible, use the builder in stead of the job
                            // (getting the job may cause an exception if the
                            // builder is
                            // not correctly configured yet)
                            final FilterComponentBuilder<?, ?> fjb = ((LazyFilterOutcome) filterOutcome)
                                    .getFilterJobBuilder();

                            final String filterLabel = LabelUtils.getLabel(fjb);

                            setText(filterLabel + ": " + category);
                        } else {
                            final FilterJob filterJob = filterOutcome.getFilterJob();
                            setText(LabelUtils.getLabel(filterJob) + ": " + category);
                        }
                    } else {
                        // Other requirement types not yet supported
                        setText(requirement.toString());
                    }
                }

                updateParentUI();
            }
        };
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeAndWait(runnable);
            }
        } catch (Exception e) {
            logger.error("Failed to update ChangeRequirementButton", e);
        }
    }

    // hack to update the UI of the parent tab - seems there's a problem with
    // updating the JXTaskPaneContainers if a popup appear above them.
    protected void updateParentUI() {
        Container parent = getParent();
        while (parent != null) {
            Container nextParent = parent.getParent();
            if (nextParent == null) {
                break;
            }
            if (!(nextParent instanceof JComponent)) {
                break;
            }
            parent = nextParent;
        }

        updateUI();
        if (parent instanceof JComponent) {
            logger.debug("Updating parent of {}", this);
            ((JComponent) parent).updateUI();
        } else {
            logger.debug("Tried to updateParentUI, but failed to find a JComponent in parent hierarchy of {}!", this);
        }
    }

    @Override
    public String toString() {
        return "ChangeRequirementButton[jobBuilder=" + LabelUtils.getLabel(_componentBuilder) + "]";
    }
}
