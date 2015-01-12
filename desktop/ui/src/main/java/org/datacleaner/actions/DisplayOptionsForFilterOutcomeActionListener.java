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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.datacleaner.api.Analyzer;
import org.datacleaner.beans.writers.WriteDataCategory;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.SimpleComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.job.builder.FilterJobBuilder;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.extension.output.AbstractOutputWriterAnalyzer;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.widgets.DescriptorMenu;

/**
 * Action that displays output writers for a filter's outcome.
 */
public class DisplayOptionsForFilterOutcomeActionListener extends DisplayOutputWritersAction implements ActionListener {

    private static final ImageManager imageManager = ImageManager.get();

    private final FilterJobBuilder<?, ?> _filterJobBuilder;
    private final String _categoryName;

    public DisplayOptionsForFilterOutcomeActionListener(FilterJobBuilder<?, ?> filterJobBuilder, String categoryName) {
        super(filterJobBuilder.getAnalysisJobBuilder());
        _filterJobBuilder = filterJobBuilder;
        _categoryName = categoryName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final FilterOutcome filterOutcome = _filterJobBuilder.getFilterOutcome(_filterJobBuilder.getDescriptor()
                .getOutcomeCategoryByName(_categoryName));
        final ComponentRequirement requirement = new SimpleComponentRequirement(filterOutcome);

        final DescriptorMenu writeDataMenu = new DescriptorMenu(new WriteDataCategory());
        {
            List<JMenuItem> writerDataMenuItems = createMenuItems();
            for (JMenuItem menuItem : writerDataMenuItems) {
                writeDataMenu.add(menuItem);
            }
        }

        final AnalysisJobBuilder analysisJobBuilder = _filterJobBuilder.getAnalysisJobBuilder();

        // TODO: Add more items: "Dependent components" (click through),
        // "Add analyzer", "Add transformer"

        final JMenuItem setAsDefaultOutcomeMenuItem = new JMenuItem("Set as default requirement");
        setAsDefaultOutcomeMenuItem
                .setToolTipText("Makes this filter outcome the default choice for other components in the job.");

        final ComponentRequirement existingDefaultRequirement = analysisJobBuilder.getDefaultRequirement();
        if (requirement.equals(existingDefaultRequirement)) {
            setAsDefaultOutcomeMenuItem.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID,
                    IconUtils.ICON_SIZE_SMALL));
        }

        setAsDefaultOutcomeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Enum<?> category = _filterJobBuilder.getDescriptor().getOutcomeCategoryByName(_categoryName);
                FilterOutcome outcome = _filterJobBuilder.getFilterOutcome(category);
                analysisJobBuilder.setDefaultRequirement(outcome);
            }
        });

        final JPopupMenu popup = new JPopupMenu();
        popup.add(writeDataMenu);
        popup.add(setAsDefaultOutcomeMenuItem);

        final JComponent component = (JComponent) e.getSource();
        popup.show(component, 0, component.getHeight());
    }

    @Override
    protected void configure(AnalysisJobBuilder analysisJobBuilder, AnalyzerJobBuilder<?> analyzerJobBuilder) {
        Analyzer<?> analyzer = analyzerJobBuilder.getComponentInstance();
        if (analyzer instanceof AbstractOutputWriterAnalyzer) {
            LifeCycleHelper helper = new LifeCycleHelper(analysisJobBuilder.getConfiguration()
                    .getInjectionManager(null), null, true);
            helper.assignProvidedProperties(analyzerJobBuilder.getDescriptor(), analyzer);
            ((AbstractOutputWriterAnalyzer) analyzer).configureForFilterOutcome(analysisJobBuilder,
                    _filterJobBuilder.getDescriptor(), _categoryName);
        }
        analyzerJobBuilder.setRequirement(_filterJobBuilder, _categoryName);
    }

}
