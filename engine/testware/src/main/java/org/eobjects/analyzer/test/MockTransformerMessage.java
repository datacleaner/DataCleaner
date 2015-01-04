/**
 * AnalyzerBeans
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.eobjects.analyzer.test;

import org.eobjects.analyzer.beans.api.ComponentMessage;
import org.eobjects.analyzer.data.InputColumn;

public class MockTransformerMessage implements ComponentMessage {

    private final String _message;
    private final InputColumn<?> _column;

    public MockTransformerMessage(String message, InputColumn<?> column) {
        _message = message;
        _column = column;
    }

    public InputColumn<?> getColumn() {
        return _column;
    }

    public String getMessage() {
        return _message;
    }
    
    @Override
    public String toString() {
        return "MockTransformerMessage[" + _message + "]";
    }
}
