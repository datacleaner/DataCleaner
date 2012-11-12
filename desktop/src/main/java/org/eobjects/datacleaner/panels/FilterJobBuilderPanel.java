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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.actions.DisplayOptionsForFilterOutcomeActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

public class FilterJobBuilderPanel extends AbstractJobBuilderPanel implements
		FilterJobBuilderPresenter {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private static final Image WATERMARK_IMAGE = imageManager
			.getImage("images/window/transformer-tab-background.png");

	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final DCPanel _outcomePanel;

	public FilterJobBuilderPanel(FilterJobBuilder<?, ?> filterJobBuilder,
			WindowContext windowContext,
			PropertyWidgetFactory propertyWidgetFactory) {
		this(WATERMARK_IMAGE, 95, 95, filterJobBuilder, windowContext,
				propertyWidgetFactory);
	}

	protected FilterJobBuilderPanel(Image watermarkImage,
			int watermarkHorizontalPosition, int watermarkVerticalPosition,
			FilterJobBuilder<?, ?> filterJobBuilder,
			WindowContext windowContext,
			PropertyWidgetFactory propertyWidgetFactory) {
		super(watermarkImage, watermarkHorizontalPosition,
				watermarkVerticalPosition, filterJobBuilder,
				propertyWidgetFactory);

		_filterJobBuilder = filterJobBuilder;

		_outcomePanel = new DCPanel();
		final Set<String> categoryNames = _filterJobBuilder.getDescriptor()
				.getOutcomeCategoryNames();
		for (final String categoryName : categoryNames) {
			final JButton outcomeButton = new JButton(categoryName,
					imageManager.getImageIcon(
							"images/component-types/filter-outcome.png",
							IconUtils.ICON_SIZE_SMALL));

			outcomeButton
					.addActionListener(new DisplayOptionsForFilterOutcomeActionListener(
							_filterJobBuilder, categoryName));
			_outcomePanel.add(outcomeButton);
		}

		final JButton helpButton = WidgetFactory
				.createSmallButton("images/widgets/help.png");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DCPanel messagePanel = new DCPanel();
				messagePanel.setLayout(new BorderLayout());
				messagePanel.add(
						new JLabel(
								imageManager
										.getImageIcon("images/help/help_requirement_mapping.png")),
						BorderLayout.WEST);
				messagePanel.add(DCLabel
						.darkMultiLine("Filter outcomes can be set as requirements<br>"
								+ "for other components, using the<br>"
								+ "\"(no requirement specified)\" buttons.<br><br>"
								+ "You can also click the categories directly to eg.<br>"
								+ "write categorized records or to map them<br>"
								+ "as requirements for existing or new components."));
				JOptionPane.showMessageDialog(FilterJobBuilderPanel.this,
						messagePanel, "Help: Filter categories / outcomes",
						JOptionPane.PLAIN_MESSAGE);
			}
		});
		_outcomePanel.add(helpButton);
	}

	protected JComponent decorate(DCPanel panel) {
		JComponent result = super.decorate(panel);
		addTaskPane(imageManager.getImageIcon(
				"images/component-types/filter-outcome.png",
				IconUtils.ICON_SIZE_SMALL),
				"This filter categorizes records as...", _outcomePanel);
		return result;
	}

	@Override
	public FilterJobBuilder<?, ?> getJobBuilder() {
		return _filterJobBuilder;
	}
}
