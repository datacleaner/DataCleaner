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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.AnyComponentRequirement;
import org.eobjects.analyzer.job.ComponentRequirement;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.builder.AbstractBeanWithInputColumnsBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.LazyFilterOutcome;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A button that displays the {@link ComponentRequirement} of a particular
 * component that is being built.
 */
public class ChangeRequirementButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ChangeRequirementButton.class);

    private static final String NO_REQUIREMENT_TEXT = "(No requirement)";
    private static final String ANY_REQUIREMENT_TEXT = "All records";

    private static final ImageManager imageManager = ImageManager.get();

    private static final Icon selectedRequirementIcon = imageManager.getImageIcon(IconUtils.STATUS_VALID,
            IconUtils.ICON_SIZE_SMALL);
    private static final Icon unconfiguredFilterIcon = imageManager.getImageIcon(IconUtils.STATUS_WARNING,
            IconUtils.ICON_SIZE_SMALL);
    private static final Icon filterIcon = imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH,
            IconUtils.ICON_SIZE_SMALL);

    private final AbstractBeanWithInputColumnsBuilder<?, ?, ?> _jobBuilder;

    public ChangeRequirementButton(AbstractBeanWithInputColumnsBuilder<?, ?, ?> jobBuilder) {
        super(NO_REQUIREMENT_TEXT, filterIcon);
        _jobBuilder = jobBuilder;
        addActionListener(this);
        updateText();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final ComponentRequirement currentComponentRequirement = _jobBuilder.getComponentRequirement();
        logger.info("Current requirement: {}", currentComponentRequirement);

        final Collection<FilterOutcome> currentFilterOutcomes = currentComponentRequirement == null ? Collections
                .<FilterOutcome> emptyList() : currentComponentRequirement.getProcessingDependencies();

        final JPopupMenu popup = new JPopupMenu();

        final JMenuItem noFilterMenuItem = new JMenuItem(NO_REQUIREMENT_TEXT);
        noFilterMenuItem
                .setToolTipText("Do not apply any specific requirements on this component, except for those that are transitively inherited by the configuration.");
        noFilterMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _jobBuilder.setComponentRequirement(null);
                updateText();
            }
        });
        popup.add(noFilterMenuItem);

        final JMenuItem anyFilterMenuItem = new JMenuItem(ANY_REQUIREMENT_TEXT);
        anyFilterMenuItem
                .setToolTipText("Explicitly accept all records into this component, regardless of any other transitive requirements.");
        anyFilterMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _jobBuilder.setComponentRequirement(AnyComponentRequirement.get());
                updateText();
            }
        });
        if (AnyComponentRequirement.get().equals(_jobBuilder.getComponentRequirement())) {
            anyFilterMenuItem.setIcon(selectedRequirementIcon);
        }
        popup.add(anyFilterMenuItem);

        // if this JobBuilder is a FilterJobBuilder, remove it from the list of
        // available filters
        final List<FilterJobBuilder<?, ?>> fjbs;
        if (_jobBuilder instanceof FilterJobBuilder<?, ?>) {
            fjbs = new LinkedList<FilterJobBuilder<?, ?>>(_jobBuilder.getAnalysisJobBuilder().getFilterJobBuilders());
            fjbs.remove(_jobBuilder);
        } else {
            fjbs = _jobBuilder.getAnalysisJobBuilder().getFilterJobBuilders();
        }

        for (final FilterJobBuilder<?, ?> fjb : fjbs) {
            final JMenu filterMenuItem = new JMenu(LabelUtils.getLabel(fjb));

            if (!fjb.isConfigured()) {
                filterMenuItem.setIcon(unconfiguredFilterIcon);
                filterMenuItem.setEnabled(false);
                filterMenuItem.setToolTipText("Filter is not correctly configured");
            } else if (!_jobBuilder.validateRequirementSource(fjb)) {
                filterMenuItem.setEnabled(false);
                filterMenuItem.setToolTipText("Requirement not possible");
            } else {
                final FilterBeanDescriptor<?, ?> fjbDescriptor = fjb.getDescriptor();
                final Set<String> categoryNames = fjbDescriptor.getOutcomeCategoryNames();
                for (final String category : categoryNames) {
                    final JMenuItem categoryMenuItem = new JMenuItem(category);
                    try {
                        final Enum<?> outcomeCategory = fjbDescriptor.getOutcomeCategoryByName(category);
                        final FilterOutcome filterOutcome = fjb.getFilterOutcome(outcomeCategory);
                        if (currentFilterOutcomes.contains(filterOutcome)) {
                            filterMenuItem.setIcon(selectedRequirementIcon);
                            categoryMenuItem.setIcon(selectedRequirementIcon);
                        }
                    } catch (Exception ex) {
                        logger.info("Filterjob matching threw exception, probably because of incomplete configuration",
                                ex);
                    }

                    categoryMenuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _jobBuilder.setRequirement(fjb, category);
                            updateText();
                        }
                    });

                    filterMenuItem.add(categoryMenuItem);
                }

            }
            popup.add(filterMenuItem);
        }

        popup.show(this, 0, getHeight());
    }

    public void updateText() {
        logger.debug("updateText()");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final ComponentRequirement requirement = _jobBuilder.getComponentRequirement();
                if (requirement == null) {
                    setText(NO_REQUIREMENT_TEXT);
                } else if (AnyComponentRequirement.get().equals(requirement)) {
                    setText(ANY_REQUIREMENT_TEXT);
                } else {
                    if (requirement instanceof FilterOutcome) {
                        FilterOutcome filterOutcome = (FilterOutcome) requirement;
                        Enum<?> category = filterOutcome.getCategory();
                        if (filterOutcome instanceof LazyFilterOutcome) {
                            // if possible, use the builder in stead of the job
                            // (getting
                            // the job may cause an exception if the builder is
                            // not
                            // correctly configured yet)
                            FilterJobBuilder<?, ?> fjb = ((LazyFilterOutcome) filterOutcome).getFilterJobBuilder();

                            String filterLabel = LabelUtils.getLabel(fjb);

                            setText(filterLabel + ": " + category);
                        } else {
                            FilterJob filterJob = filterOutcome.getFilterJob();
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
        return "ChangeRequirementButton[jobBuilder=" + LabelUtils.getLabel(_jobBuilder) + "]";
    }
}
