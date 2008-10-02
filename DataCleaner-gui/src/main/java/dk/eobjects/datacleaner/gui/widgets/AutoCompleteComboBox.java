/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.widgets;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

public class AutoCompleteComboBox extends JComboBox {

	private static final long serialVersionUID = 2581038301779889489L;

	public AutoCompleteComboBox(Object[] items) {
		super(insertEmptyString(items));
		setEditable(true);
		AutoCompleteDecorator.decorate(this);
		setSelectedItem("");
	}

	private static ComboBoxModel insertEmptyString(Object[] items) {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		if (ArrayUtils.indexOf(items, "") < 0) {
			model.addElement("");
		}
		for (int i = 0; i < items.length; i++) {
			Object object = items[i];
			model.addElement(object);
		}
		return model;
	}

	@Override
	public void setSelectedItem(Object newItem) {
		try {
			super.setSelectedItem(newItem);
		} catch (NullPointerException e) {
			addItem(newItem);
			super.setSelectedItem(newItem);
		}
	}
}