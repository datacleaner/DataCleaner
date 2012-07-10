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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.eobjects.analyzer.util.CharIterator;

/**
 * A {@link Document}, typically used for {@link JTextField}s, which specifies
 * that only numbers may be entered.
 */
public class NumberDocument extends PlainDocument {

    private static final long serialVersionUID = 1L;

    private final boolean _allowDecimal;
    private final boolean _allowNegative;

    public NumberDocument() {
        this(true);
    }

    public NumberDocument(boolean allowDecimal) {
        this(allowDecimal, true);
    }

    public NumberDocument(boolean allowDecimal, boolean allowNegative) {
        _allowDecimal = allowDecimal;
        _allowNegative = allowNegative;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        boolean valid = true;
        CharIterator it = new CharIterator(str);
        while (it.hasNext() && valid) {
            it.next();

            if (it.isDigit() || it.is('%')) {
                continue;
            }

            if (it.is('-') && _allowNegative) {
                continue;
            }

            if (it.is('.') && _allowDecimal) {
                continue;
            }

            valid = false;
            break;
        }
        if (valid) {
            super.insertString(offs, str, a);
        }
    }
}
