/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.builder.AbstractBeanWithInputColumnsBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.LazyFilterOutcome;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeRequirementButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ChangeRequirementButton.class);

	private static final String NO_FILTER_TEXT = "(No filter requirement)";
	private static final ImageManager imageManager = ImageManager.getInstance();
	private static final Icon mappedFilterIcon = imageManager.getImageIcon("images/status/valid.png",
			IconUtils.ICON_SIZE_SMALL);
	private static final Icon unconfiguredFilterIcon = imageManager.getImageIcon("images/status/warning.png",
			IconUtils.ICON_SIZE_SMALL);

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AbstractBeanWithInputColumnsBuilder<?, ?, ?> _jobBuilder;

	public ChangeRequirementButton(AnalysisJobBuilder analysisJobBuilder,
			AbstractBeanWithInputColumnsBuilder<?, ?, ?> jobBuilder) {
		super(NO_FILTER_TEXT, ImageManager.getInstance().getImageIcon("images/component-types/filter.png",
				IconUtils.ICON_SIZE_SMALL));
		_analysisJobBuilder = analysisJobBuilder;
		_jobBuilder = jobBuilder;
		addActionListener(this);
		updateText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Outcome currentRequirement = _jobBuilder.getRequirement();
		logger.info("Current requirement: {}", currentRequirement);

		JPopupMenu popup = new JPopupMenu();

		JMenuItem noFilterMenuItem = new JMenuItem(NO_FILTER_TEXT);
		noFilterMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_jobBuilder.setRequirement(null);
				updateText();
			}
		});
		popup.add(noFilterMenuItem);

		List<FilterJobBuilder<?, ?>> fjbs = _analysisJobBuilder.getFilterJobBuilders();

		// if this JobBuilder is a FilterJobBuilder, remove it from the list of
		// available filters
		if (_jobBuilder instanceof FilterJobBuilder<?, ?>) {
			fjbs = new LinkedList<FilterJobBuilder<?, ?>>(fjbs);
			fjbs.remove(_jobBuilder);
		}

		for (final FilterJobBuilder<?, ?> fjb : fjbs) {
			JMenu filterMenuItem = new JMenu(fjb.getDescriptor().getDisplayName());

			if (!fjb.isConfigured()) {
				filterMenuItem.setIcon(unconfiguredFilterIcon);
				filterMenuItem.setEnabled(false);
				filterMenuItem.setToolTipText("Filter is not correctly configured");
			} else {
				FilterBeanDescriptor<?, ?> fjbDescriptor = fjb.getDescriptor();
				Set<String> categoryNames = fjbDescriptor.getCategoryNames();
				for (final String category : categoryNames) {
					JMenuItem categoryMenuItem = new JMenuItem(category);

					if (currentRequirement != null && currentRequirement instanceof FilterOutcome) {
						FilterOutcome filterOutcome = (FilterOutcome) currentRequirement;
						// put an icon on the currently configured requirement
						try {
							FilterJob filterJob = fjb.toFilterJob();

							if (filterOutcome.getFilterJob().equals(filterJob)) {
								if (filterOutcome.getCategory().equals(fjbDescriptor.getCategoryByName(category))) {
									filterMenuItem.setIcon(mappedFilterIcon);
									categoryMenuItem.setIcon(mappedFilterIcon);
								}
							}
						} catch (Exception ex) {
							System.out.println("ex: " + ex.getMessage());
							logger.warn("Filterjob matching threw exception, probably because of incomplete configuration",
									ex);
						}
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
		logger.info("updateText()");
		Outcome requirement = _jobBuilder.getRequirement();
		if (requirement == null) {
			setText(NO_FILTER_TEXT);
		} else {
			if (requirement instanceof FilterOutcome) {
				FilterOutcome filterOutcome = (FilterOutcome) requirement;
				Enum<?> category = filterOutcome.getCategory();
				if (filterOutcome instanceof LazyFilterOutcome) {
					// if possible, use the builder in stead of the job (getting
					// the job may cause an exception if the builder is not
					// correctly configured yet)
					FilterJobBuilder<?, ?> fjb = ((LazyFilterOutcome) filterOutcome).getFilterJobBuilder();
					setText(fjb.getDescriptor().getDisplayName() + ": " + category);
				} else {
					FilterJob filterJob = filterOutcome.getFilterJob();
					setText(filterJob.getDescriptor().getDisplayName() + ": " + category);
				}
			} else {
				// TODO: Other requirement types not yet supported
				setText(requirement.toString());
			}
		}

		updateParentUI();
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
		if (parent instanceof JComponent) {
			((JComponent) parent).updateUI();
		}
	}
}
