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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.SingleCharacterDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

public class MultipleCharPropertyWidget extends AbstractPropertyWidget<char[]> {

	private static final long serialVersionUID = 1L;
	private final DCPanel _textFieldPanel;

	public MultipleCharPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_textFieldPanel = new DCPanel();
		_textFieldPanel.setLayout(new VerticalLayout(2));

		char[] currentValue = (char[]) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue == null) {
			currentValue = new char[1];
		}
		updateComponents(currentValue);

		final JButton addButton = WidgetFactory.createSmallButton("images/actions/add.png");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTextComponent();
				fireValueChanged();
			}
		});

		final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int componentCount = _textFieldPanel.getComponentCount();
				if (componentCount > 0) {
					_textFieldPanel.remove(componentCount - 1);
					_textFieldPanel.updateUI();
					fireValueChanged();
				}
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		buttonPanel.setLayout(new VerticalLayout(2));
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new BorderLayout());

		outerPanel.add(_textFieldPanel, BorderLayout.CENTER);
		outerPanel.add(buttonPanel, BorderLayout.EAST);

		add(outerPanel);
	}

	public void updateComponents(char[] values) {
		_textFieldPanel.removeAll();
		if (values != null) {
			for (char value : values) {
				addTextComponent(value);
			}
		}
	}

	private JTextComponent addTextComponent() {
		final DCLabel label = DCLabel.dark("");

		final JTextArea textComponent = new JTextArea(1, 1);
		textComponent.setBorder(WidgetUtils.BORDER_THIN);
		textComponent.setDocument(new SingleCharacterDocument());
		textComponent.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				final String text = textComponent.getText();
				if (text == null || text.length() == 0) {
					label.setText("");
				} else if (text.charAt(0) == ' ') {
					label.setText("[whitespace]");
				} else if (text.charAt(0) == '\t') {
					label.setText("[tab]");
				} else if (text.charAt(0) == '\n') {
					label.setText("[newline]");
				} else if (text.charAt(0) == '\r') {
					label.setText("[carriage return]");
				} else if (text.charAt(0) == '\f') {
					label.setText("[form feed]");
				} else if (text.charAt(0) == '\b') {
					label.setText("[backspace]");
				} else {
					label.setText("" + text.charAt(0));
				}
				fireValueChanged();
			}
		});

		final DCPanel panel = new DCPanel();
		panel.setLayout(new HorizontalLayout(2));
		panel.add(textComponent);
		panel.add(label);

		_textFieldPanel.add(panel);
		_textFieldPanel.updateUI();
		return textComponent;
	}

	private void addTextComponent(char value) {
		addTextComponent().setText(value + "");
	}

	@Override
	public char[] getValue() {
		Component[] components = _textFieldPanel.getComponents();
		List<Character> list = new ArrayList<Character>();
		for (int i = 0; i < components.length; i++) {
			DCPanel panel = (DCPanel) components[i];
			JTextComponent textComponent = (JTextComponent) panel.getComponent(0);
			String text = textComponent.getText();
			if (text != null && text.length() == 1) {
				list.add(text.charAt(0));
			}
		}
		char[] result = new char[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	@Override
	protected void setValue(char[] value) {
		updateComponents(value);
	}

}
