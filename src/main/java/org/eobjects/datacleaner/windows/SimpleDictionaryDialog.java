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
package org.eobjects.datacleaner.windows;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.label.MultiLineLabel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public final class SimpleDictionaryDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private final SimpleDictionary _originalDictionary;
	private final MutableReferenceDataCatalog _catalog;
	private final JXTextField _nameTextField;
	private final JXTextArea _valuesTextArea;

	public SimpleDictionaryDialog(MutableReferenceDataCatalog catalog) {
		this(null, catalog);
	}

	public SimpleDictionaryDialog(SimpleDictionary dictionary, MutableReferenceDataCatalog catalog) {
		_originalDictionary = dictionary;
		_catalog = catalog;

		_nameTextField = WidgetFactory.createTextField("Dictionary name");
		_valuesTextArea = WidgetFactory.createTextArea("Values");
		_valuesTextArea.setRows(3);
		_valuesTextArea.setBorder(WidgetUtils.BORDER_THIN);

		if (dictionary != null) {
			_nameTextField.setText(dictionary.getName());
			Collection<String> values = dictionary.getValues().getValues();
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String value : values) {
				if (first) {
					first = false;
				} else {
					sb.append('\n');
				}
				sb.append(value);
			}
			_valuesTextArea.setText(sb.toString());
		}
	}

	@Override
	protected String getBannerTitle() {
		return "Simple dictionary";
	}

	@Override
	protected int getDialogWidth() {
		return 465;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel formPanel = new DCPanel();

		int row = 0;
		WidgetUtils.addToGridBag(new JLabel("Dictionary name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

		row++;
		WidgetUtils.addToGridBag(new JLabel("Values:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_valuesTextArea, formPanel, 1, row);

		row++;
		final JButton createDictionaryButton = WidgetFactory.createButton("Save dictionary", "images/model/dictionary.png");
		createDictionaryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = _nameTextField.getText();
				if (StringUtils.isNullOrEmpty(name)) {
					JOptionPane.showMessageDialog(SimpleDictionaryDialog.this, "Please fill out the name of the dictionary");
					return;
				}

				String values = _valuesTextArea.getText();
				if (StringUtils.isNullOrEmpty(values)) {
					JOptionPane.showMessageDialog(SimpleDictionaryDialog.this, "Please fill out the values");
					return;
				}

				SimpleDictionary dict = new SimpleDictionary(name, values.split("\n"));

				if (_originalDictionary != null) {
					_catalog.removeDictionary(_originalDictionary);
				}
				_catalog.addDictionary(dict);
				SimpleDictionaryDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		buttonPanel.add(createDictionaryButton);
		WidgetUtils.addToGridBag(buttonPanel, formPanel, 0, row, 2, 1);

		final MultiLineLabel descriptionLabel = new MultiLineLabel(
				"A simple dictionary is a dictionary that you enter directly in DataCleaner. In the 'Values' field you can enter each value of the dictionary on a separate line.");
		descriptionLabel.setBorder(new EmptyBorder(10, 10, 10, 20));
		descriptionLabel.setPreferredSize(new Dimension(300, 100));

		final DCPanel mainPanel = new DCPanel();
		mainPanel.setLayout(new VerticalLayout(4));
		mainPanel.add(descriptionLabel);
		mainPanel.add(formPanel);

		JScrollPane scroll = WidgetUtils.scrolleable(mainPanel);
		return scroll;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "Simple dictionary";
	}

}
