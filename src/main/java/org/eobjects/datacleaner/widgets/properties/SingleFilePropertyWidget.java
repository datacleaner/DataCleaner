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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

public final class SingleFilePropertyWidget extends AbstractPropertyWidget<File> {

	private static final long serialVersionUID = 1L;

	private final JXTextField _textField;

	public SingleFilePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_textField = WidgetFactory.createTextField("Filename");

		File currentValue = (File) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue.getAbsolutePath());
		}

		_textField.getDocument().addDocumentListener(new DCDocumentListener() {

			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});

		JButton browseButton = new JButton(ImageManager.getInstance().getImageIcon("images/actions/browse.png",
				IconUtils.ICON_SIZE_SMALL));
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getConfiguredFileDirectory());
				fileChooser.addChoosableFileFilter(FileFilters.ALL);
				fileChooser.setFileFilter(FileFilters.ALL);
				WidgetUtils.centerOnScreen(fileChooser);
				int result = fileChooser.showOpenDialog(SingleFilePropertyWidget.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					File dir = selectedFile.getParentFile();
					UserPreferences.getInstance().setConfiguredFileDirectory(dir);
					_textField.setText(selectedFile.getAbsolutePath());
				}
			}
		});

		DCPanel panel = new DCPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		panel.add(_textField);
		panel.add(browseButton);
		add(panel);
	}

	@Override
	public File getValue() {
		String text = _textField.getText();
		if (StringUtils.isNullOrEmpty(text)) {
			return null;
		}
		File file = new File(text);
		return file;
	}

	@Override
	protected void setValue(File value) {
		if (value == null) {
			_textField.setText("");
			return;
		}
		_textField.setText(value.getAbsolutePath());
	}
}
