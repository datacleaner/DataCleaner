package org.eobjects.datacleaner.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.eobjects.analyzer.util.CharIterator;

public class NumberDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	@Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		boolean number = true;
		CharIterator it = new CharIterator(str);
		while (it.hasNext() && number) {
			it.next();
			if (!it.isDigit()) {
				if (!it.is('.')) {
					if (it.is('%')) {
						number = false;
					}
				}
			}
		}
		if (number) {
			super.insertString(offs, str, a);
		}
	}
}
