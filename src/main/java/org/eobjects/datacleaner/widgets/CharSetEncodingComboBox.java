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
package org.eobjects.datacleaner.widgets;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

/**
 * An editable combobox with a default set of available character set encodings
 * 
 * @author Kasper Sørensen
 */
public class CharSetEncodingComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	private static final String[] encodings;

	static {
		List<String> list = new ArrayList<String>();
		list.add("UTF-8");
		list.add("UTF-16");
		list.add("UTF-16BE");
		list.add("UTF-16LE");
		list.add("ASCII");
		for (int i = 1; i <= 16; i++) {
			list.add("ISO-8859-" + i);
		}

		for (int i = 1250; i <= 1258; i++) {
			list.add("Windows-" + i);
		}

		encodings = list.toArray(new String[list.size()]);
	}

	public CharSetEncodingComboBox() {
		super(encodings);
		setEditable(true);

		String defaultCharset = Charset.defaultCharset().name();
		setSelectedItem(defaultCharset);
	}
}
