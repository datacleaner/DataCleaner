/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

import org.eobjects.datacleaner.util.NumberDocument;

/**
 * A combo box for selecing header lines for eg. CSV or fixed width files.
 * 
 * @author Kasper Sørensen
 */
public class HeaderLineComboBox extends DCComboBox<Integer> {

	private static final long serialVersionUID = 1L;

	public HeaderLineComboBox() {
		super();
		JTextComponent headerLineNumberText = (JTextComponent) getEditor().getEditorComponent();
		headerLineNumberText.setDocument(new NumberDocument());
		setEditable(true);
		setModel(new DefaultComboBoxModel(new Integer[] { 0, 1 }));
		setSelectedItem(1);
		setRenderer(new DCListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof Integer) {
					Integer i = (Integer) value;
					if (i <= 0) {
						value = "No header";
					}
				}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}

		});
	}
}
