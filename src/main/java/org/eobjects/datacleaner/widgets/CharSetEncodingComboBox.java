package org.eobjects.datacleaner.widgets;

import javax.swing.JComboBox;

/**
 * An editable combobox with a default set of available character set encodings
 * 
 * @author Kasper Sørensen
 */
public class CharSetEncodingComboBox extends JComboBox {

	private static final long serialVersionUID = 1L;

	public CharSetEncodingComboBox() {
		super(new String[] { "UTF-8", "ASCII", "CP1252" });
		setEditable(true);
	}
}
