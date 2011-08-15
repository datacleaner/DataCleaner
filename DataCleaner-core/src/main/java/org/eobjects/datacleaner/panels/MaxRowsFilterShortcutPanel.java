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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXTextField;

/**
 * A panel with a simple checkbox and textfield to enable a
 * {@link MaxRowsFilter}.
 * 
 * @author Kasper Sørensen
 */
public class MaxRowsFilterShortcutPanel extends DCPanel implements FilterJobBuilderPresenter {

	public static final String FILTER_NAME = "Limit analysis (Max rows)";

	private static final long serialVersionUID = 1L;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final JXTextField _textField;
	private final JCheckBox _checkBox;
	private final DCLabel _suffixLabel;
	private final DCLabel _prefixLabel;
	private FilterJobBuilder<MaxRowsFilter, ValidationCategory> _maxRowsFilterJobBuilder;

	/**
	 * Checks if the given filter is in fact the filter specified by this
	 * shortcut panel.
	 * 
	 * @param filterJobBuilder
	 * @return
	 */
	public static boolean isFilter(FilterJobBuilder<?, ?> filterJobBuilder) {
		return FILTER_NAME.equals(filterJobBuilder.getName())
				&& filterJobBuilder.getDescriptor().getComponentClass() == MaxRowsFilter.class;
	}

	public MaxRowsFilterShortcutPanel(AnalysisJobBuilder analysisJobBuilder) {
		this(analysisJobBuilder, null);
	}

	public MaxRowsFilterShortcutPanel(AnalysisJobBuilder analysisJobBuilder, FilterJobBuilder<?, ?> filterJobBuilder) {
		super();
		_analysisJobBuilder = analysisJobBuilder;
		_checkBox = new JCheckBox();
		_textField = WidgetFactory.createTextField();
		_textField.setEnabled(false);
		_textField.setColumns(4);
		_textField.setDocument(new NumberDocument());
		_textField.setText("1000");
		_prefixLabel = DCLabel.dark("Limit analysis to max ");
		_prefixLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				_checkBox.doClick();
			}
		});
		_prefixLabel.setEnabled(false);
		_suffixLabel = DCLabel.dark(" rows.");
		_suffixLabel.setEnabled(false);

		if (filterJobBuilder != null) {
			@SuppressWarnings("unchecked")
			FilterJobBuilder<MaxRowsFilter, ValidationCategory> maxRowFilterJobBuilder = (FilterJobBuilder<MaxRowsFilter, ValidationCategory>) filterJobBuilder;
			_maxRowsFilterJobBuilder = maxRowFilterJobBuilder;
			_checkBox.setSelected(true);
		}

		_checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean selected = _checkBox.isSelected();

				FilterJobBuilder<MaxRowsFilter, ValidationCategory> maxRowsFilterJobBuilder = getJobBuilder();

				if (selected) {
					_analysisJobBuilder.addFilter(maxRowsFilterJobBuilder);
					_analysisJobBuilder.setDefaultRequirement(maxRowsFilterJobBuilder, ValidationCategory.VALID);
				} else {
					_analysisJobBuilder.removeFilter(maxRowsFilterJobBuilder);
				}
				updateLabels();
			}
		});

		_textField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				final FilterJobBuilder<MaxRowsFilter, ValidationCategory> fjb = getJobBuilder();
				if (fjb != null) {
					try {
						int maxRows = Integer.parseInt(_textField.getText());
						fjb.setConfiguredProperty("Max rows", maxRows);
					} catch (NumberFormatException e) {
						WidgetUtils.showErrorMessage("Cannot read number",
								"The entered value could not be read as a number.", e);
					}
				}
			}
		});

		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel(ImageManager.getInstance().getImageIcon(IconUtils.MAX_ROWS_IMAGEPATH)));
		add(_checkBox);
		add(_prefixLabel);
		add(_textField);
		add(_suffixLabel);

		updateLabels();

		setBorder(WidgetUtils.BORDER_LIST_ITEM);
	}

	@Override
	public FilterJobBuilder<MaxRowsFilter, ValidationCategory> getJobBuilder() {
		if (_maxRowsFilterJobBuilder == null) {
			// Lazy initializing getter (to postpone the call to
			// DCConfiguration.
			_maxRowsFilterJobBuilder = new FilterJobBuilder<MaxRowsFilter, ValidationCategory>(_analysisJobBuilder,
					_analysisJobBuilder.getConfiguration().getDescriptorProvider()
							.getFilterBeanDescriptorForClass(MaxRowsFilter.class));
			_maxRowsFilterJobBuilder.setName(FILTER_NAME);
		}
		return _maxRowsFilterJobBuilder;
	}

	@Override
	public void applyPropertyValues() {
	}

	@Override
	public JComponent createJComponent() {
		DCLabel label = DCLabel.dark("Configured in 'Source' tab");
		label.setBorder(new EmptyBorder(4, 4, 4, 4));
		return label;
	}

	@Override
	public void onConfigurationChanged() {
	}

	@Override
	public void onRequirementChanged() {
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_checkBox.setEnabled(enabled);
	}

	public void setFilterEnabled(boolean enabled) {
		if (enabled != isFilterEnabled()) {
			_checkBox.doClick();
		}
	}

	public boolean isFilterEnabled() {
		return _checkBox.isSelected();
	}

	private void updateLabels() {
		final boolean selected = _checkBox.isSelected();
		_prefixLabel.setEnabled(selected);
		_textField.setEnabled(selected);
		_suffixLabel.setEnabled(selected);
	}
}
