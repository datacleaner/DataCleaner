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

import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextArea;

public class MultipleStringPropertyWidget extends AbstractPropertyWidget<String[]> {

	private static final long serialVersionUID = 1L;
	private final JXTextArea _textArea;

	public MultipleStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_textArea = WidgetUtils.createTextArea(propertyDescriptor.getName());
		_textArea.setRows(3);

		String[] currentValue = (String[]) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			StringBuilder sb = new StringBuilder();
			for (String string : currentValue) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(string);
			}
			_textArea.setText(sb.toString());
		}

		_textArea.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});

		JScrollPane scroll = WidgetUtils.scrolleable(_textArea);
		scroll.setBorder(WidgetUtils.BORDER_THIN);
		add(scroll);
	}

	@Override
	public String[] getValue() {
		String text = _textArea.getText();
		if (StringUtils.isNullOrEmpty(text)) {
			return null;
		}
		return text.split("\n");
	}

	@Override
	protected void setValue(String[] value) {
		if (value == null) {
			_textArea.setText("");
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length; i++) {
			if (i!=0) {
				sb.append("\n");
			}
			sb.append(value[i]);
		}
		_textArea.setText(sb.toString());
	}

}
