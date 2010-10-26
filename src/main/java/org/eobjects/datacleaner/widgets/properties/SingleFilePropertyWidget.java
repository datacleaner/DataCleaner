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
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

public final class SingleFilePropertyWidget extends AbstractPropertyWidget<File> {

	private static final long serialVersionUID = 1L;

	private final JXTextField _textField;

	public SingleFilePropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);

		_textField = WidgetUtils.createTextField("Filename");

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
				JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getOpenPropertyFileDirectory());
				fileChooser.addChoosableFileFilter(FileFilters.ALL);
				fileChooser.setFileFilter(FileFilters.ALL);
				WidgetUtils.centerOnScreen(fileChooser);
				int result = fileChooser.showOpenDialog(SingleFilePropertyWidget.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					File dir = selectedFile.getParentFile();
					UserPreferences.getInstance().setOpenPropertyFileDirectory(dir);
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

}
