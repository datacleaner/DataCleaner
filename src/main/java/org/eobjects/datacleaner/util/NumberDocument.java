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
				if (!it.is('-')) {
					if (!it.is('.')) {
						if (!it.is('%')) {
							number = false;
						}
					}
				}
			}
		}
		if (number) {
			super.insertString(offs, str, a);
		}
	}
}
